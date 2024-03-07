package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 媒资文件管理开放
 * @author: dengbin
 * @create: 2024-03-06 22:53
 **/
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

    /*
     * @Description: 获取媒资文件链接
     * @Author: dengbin
     * @Date: 6/3/24 23:56
     * @param mediaId:
     * @return: com.xuecheng.base.model.RestResponse<java.lang.String>
     **/
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlauUrlByMediaId(@PathVariable("mediaId") String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
