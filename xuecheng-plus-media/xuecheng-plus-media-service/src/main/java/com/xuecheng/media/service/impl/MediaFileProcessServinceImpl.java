package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @description: 媒资文件处理服务实现
 * @author: dengbin
 * @create: 2024-03-02 12:39
 **/
public class MediaFileProcessServinceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {

        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySharedIndex(shardIndex, shardTotal, count);
        return mediaProcesses;

    }
}
