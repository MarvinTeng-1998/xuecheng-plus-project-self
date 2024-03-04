package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /*
     * @Description: 上传文件
     * @Author: dengbin
     * @Date: 14/1/24 00:46
     * @param companyId: 机构Id
     * @param uploadFileParamsDto: 上传文件信息
     * @param localFilePath: 文件磁盘路径
     * @return: com.xuecheng.media.model.dto.UploadFileResultDto 文件信息
     **/
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    /*
     * @Description: 添加媒体文件到数据库
     * @Author: dengbin
     * @Date: 16/1/24 00:08
     * @param companyId:
     * @param fileMd5:
     * @param uploadFileParamsDto:
     * @param bucketFiles:
     * @param objectName:
     * @return: com.xuecheng.media.model.po.MediaFiles
     **/
    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName);

    /*
     * @Description: 检查文件是否存在
     * @Author: dengbin
     * @Date: 16/1/24 00:06
     * @param fileMd5:
     * @return: com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     **/
    RestResponse<Boolean> checkFile(String fileMd5);

    /*
     * @Description: 检查分块是否存在
     * @Author: dengbin
     * @Date: 16/1/24 00:07
     * @param fileMd5:
     * @param chunkIndex:
     * @return: com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     **/
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /*
     * @Description: 上传分块文件
     * @Author: dengbin
     * @Date: 17/1/24 01:32
     * @param fileMd5: 文件MD5
     * @param chunk: 文件序号
     * @param localChunkFilePath: 本地分块文件地址
     * @return: com.xuecheng.base.model.RestResponse
     **/
    RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    /*
     * @Description: 合并分块文件
     * @Author: dengbin
     * @Date: 17/1/24 02:20
     * @param companyId: 企业ID
     * @param fileMd5: 文件Md5
     * @param chunkTotal: 分块总数
     * @param uploadFileParamsDto: 上传文件参数
     * @return: com.xuecheng.base.model.RestResponse
     **/
    RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /*
     * @Description: 从MinIO中下载文件
     * @Author: dengbin
     * @Date: 3/3/24 18:30
     * @param bucket: 桶名称
     * @param objectName: 文件名称
     * @return: java.io.File
     **/
    File downloadFileFromMinIO(String bucket, String objectName);

    /*
     * @Description: 添加媒体文件到Minio
     * @Author: dengbin
     * @Date: 4/3/24 18:36
     * @param localFilePath: 本地文件地址
     * @param mimeType: 媒体文件格式
     * @param bucketFiles: 桶文件地址
     * @param objectName: 文件名字
     * @return: boolean
     **/
    boolean addMediaFilesToMinio(String localFilePath, String mimeType, String bucketFiles, String objectName);
}
