package com.xuecheng.media.service.impl;

import com.alibaba.nacos.common.http.param.MediaType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
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

    @Autowired
    MediaFileService mediaFileService;


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
        // 解决事务失效的问题
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
        }
        return mediaFiles;
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
    private boolean addMediaFilesToMinio(String localFilePath, String mimeType, String bucketFiles, String objectName) {
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
