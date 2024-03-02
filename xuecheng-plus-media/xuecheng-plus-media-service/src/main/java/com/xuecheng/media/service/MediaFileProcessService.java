package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @descprition: 媒资文件处理服务
 * @author: dengbin
 * @create: 2024-03-02 12:36
 **/
public interface MediaFileProcessService {

    /*
     * @Description: 获取媒资文件List
     * @Author: dengbin
     * @Date: 2/3/24 12:37
     * @param shardIndex: 分片索引
     * @param shardTotal: 分片总数
     * @param count: 任务数
     * @return: java.util.List<com.xuecheng.media.model.po.MediaProcess>
     **/
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

}
