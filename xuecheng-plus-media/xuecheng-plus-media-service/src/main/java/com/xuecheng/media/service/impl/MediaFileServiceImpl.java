package com.xuecheng.media.service.impl;

import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.utils.IoUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件Service
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaProcessMapper mediaProcessMapper;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /*
     * @Description: 上传文件
     * @Author: dengbin
     * @Date: 26/1/24 22:43
     * @param companyId:
     * @param uploadFileParamsDto:
     * @param localFilePath:
     * @return: com.xuecheng.media.model.dto.UploadFileResultDto
     **/
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String fileMd5 = getFileMd5(file);
        String mimeType = getMimeType(extension);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 + extension;
        boolean b = addMediaFilesToMinio(localFilePath, mimeType, bucket_files, objectName);
        uploadFileParamsDto.setFileSize(file.length());
        // 这里要进行两部分的数据处理，一部分是加入到MediaFile数据库中，一部分是加到MediaFileProcess中
        // 解决事务失效的问题：将自己注入进去来解决事务失效的问题，因为这个函数是没有开启事务的。
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /*
     * @Description: 添加文件基础信息到数据库
     * @Author: dengbin
     * @Date: 14/1/24 11:57
     * @param companyId:
     * @param fileMd5:
     * @param uploadFileParamsDto:
     * @param bucketFiles:
     * @param objectName:
     * @return: com.xuecheng.media.model.po.MediaFiles
     **/
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucketFiles + "/" + objectName);
            mediaFiles.setBucket(bucketFiles);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            // 保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败，{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息到数据库失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

            // 记录待处理任务
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    /*
     * @Description: 添加待处理任务
     * @Author: dengbin
     * @Date: 6/2/24 14:17
     * @param mediaFiles:
     * @return: void
     **/
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 判断如果是Avi视频才写入待处理任务
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);// 失败次数默认是0
            // 向MediaProcess库中插入记录
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    /*
     * @Description: 检查文件数据库中是否有
     * @Author: dengbin
     * @Date: 16/1/24 00:09
     * @param fileMd5:
     * @return: com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     **/
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {

        // 先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles != null) {
            // 桶
            String bucket = mediaFiles.getBucket();
            // ObjectName
            String filePath = mediaFiles.getFilePath();
            // 如果数据库存在再查询 minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            // 查询远程服务获取到一个流对象
            try {
                FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
                if (filterInputStream != null) {
                    // 文件已经存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 文件不存在
        return RestResponse.success(false);
    }

    /*
     * @Description: 检查分块文件目前有多少块
     * @Author: dengbin
     * @Date: 16/1/24 00:09
     * @param fileMd5:
     * @param chunkIndex:
     * @return: com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     **/
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块存储路径是：md5前两位为两个目录，chunk存储分块文件
        // 根据md5得到分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        // 如果数据库存在再查询minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();

        // 查询远程服务获取到一个流对象
        try {
            FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
            if (filterInputStream != null) {
                // 文件已经存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RestResponse.success(false);
    }

    /*
     * @Description: 上传文件分块
     * @Author: dengbin
     * @Date: 17/1/24 01:33
     * @param fileMd5: 文件Md5
     * @param chunk: 文件分块序号
     * @param localChunkFilePath: 本地分块文件地址
     * @return: com.xuecheng.base.model.RestResponse
     **/
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        // 分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        // 获取文件的数据类型, 分块文件的数据类型是：MediaType.APPLICATION_OCTET_STREAM
        String mimeType = getMimeType(null);
        // 将分块文件上传到minio
        boolean b = addMediaFilesToMinio(localChunkFilePath, mimeType, bucket_video, chunkFilePath);

        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }

        // 上传成功
        return RestResponse.success(true);
    }

    /*
     * @Description: 合并分块文件
     * @Author: dengbin
     * @Date: 17/1/24 02:20
     * @param companyId:
     * @param fileMd5:
     * @param chunkTotal:
     * @param uploadFileParamsDto:
     * @return: com.xuecheng.base.model.RestResponse
     **/
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        // 获取分块文件地址
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath + i)
                        .build())
                .collect(Collectors.toList());

        String filename = uploadFileParamsDto.getFilename();
        String extName = filename.substring(filename.lastIndexOf("."));

        // 合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);

        try {
            ObjectWriteResponse response = minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(mergeFilePath)
                    .sources(sourceObjectList)
                    .build());
            log.debug("合并文件成功：{}", mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "合并文件失败。");
        }

        // 验证Md5
        // 下载合并后的文件
        File file = downloadFromMinIO(bucket_video, mergeFilePath);
        // 计算合并后文件的md5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // 计算合并完文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);

            // 比较原始文件的md5和合并文件的md5
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error("校验合并文件md5值不一致，原始文件:{}，合并文件:{}", fileMd5, mergeFileMd5);
                return RestResponse.validfail(false, "文件校验失败");
            }
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        // 将文件信息入库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, mergeFilePath);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        // 清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    @Override
    public File downloadFileFromMinIO(String bucket, String objectName) {
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            // 创建临时文件
            minioFile = File.createTempFile("minio", "merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(inputStream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /*
     * @Description: 清理分块文件
     * @Author: dengbin
     * @Date: 18/1/24 01:05
     * @param chunkFileFolderPath:
     * @param chunkTotal:
     * @return: void
     **/
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        List<DeleteObject> deleteObjects =
                Stream.iterate(0, i -> ++i)
                        .limit(chunkTotal)
                        .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                        .collect(Collectors.toList());
        try {
            RemoveObjectsArgs removeObjectsArgs =
                    RemoveObjectsArgs.builder()
                            .bucket(bucket_video)
                            .objects(deleteObjects)
                            .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件夹失败，objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            log.error("清除分块文件夹失败，chunkFileFolderPath:{}", chunkFileFolderPath);
        }
    }

    /*
     * @Description: 从Minio中下载文件
     * @Author: dengbin
     * @Date: 18/1/24 00:48
     * @param bucket: 桶名称
     * @param objectName: 文件名称
     * @return: java.io.File
     **/
    private File downloadFromMinIO(String bucket, String objectName) {
        // 临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            // 创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /*
     * @Description: 上传文件到Minio
     * @Author: dengbin
     * @Date: 14/1/24 11:49
     * @param localFilePath:
     * @param mimeType:
     * @param bucketFiles:
     * @param objectName:
     * @return: boolean
     **/
    public boolean addMediaFilesToMinio(String localFilePath, String mimeType, String bucketFiles, String objectName) {
        try {
            UploadObjectArgs bucket = UploadObjectArgs.builder()
                    .bucket(bucketFiles)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .object(objectName)
                    .build();
            minioClient.uploadObject(bucket);
            log.debug("上传文件到minio成功，bucket:{},objectName:{}", bucketFiles, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucketFiles, objectName, e.getMessage(), e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }

    /*
     * @Description: 得到文件分块后存放的路径
     * @Author: dengbin
     * @Date: 17/1/24 02:27
     * @param fileMd5: 文件的Md5
     * @param extName: 文件的拓展名
     * @return: java.lang.String
     **/
    private String getFilePathByMd5(String fileMd5, String extName) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extName;
    }

    /*
     * @Description: 得到分块文件的目录
     * @Author: dengbin
     * @Date: 16/1/24 00:24
     * @param fileMd5:
     * @return: java.lang.String
     **/
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    /*
     * @Description: 获取文件的默认存储目录路径 年/月/日
     * @Author: dengbin
     * @Date: 14/1/24 11:04
     * @return: java.lang.String
     **/
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date()).replace("-", "/") + "/";
    }

    /*
     * @Description: 获取文件的Md5
     * @Author: dengbin
     * @Date: 14/1/24 11:10
     * @param file:
     * @return: java.lang.String
     **/
    private String getFileMd5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * @Description: 获取流类型
     * @Author: dengbin
     * @Date: 14/1/24 11:23
     * @param extension:
     * @return: java.lang.String
     **/
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
}
