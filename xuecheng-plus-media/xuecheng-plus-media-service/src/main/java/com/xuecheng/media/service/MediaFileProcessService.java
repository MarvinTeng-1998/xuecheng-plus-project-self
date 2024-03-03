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

    /*
     * @Description: 开启一个任务
     * @Author: dengbin
     * @Date: 3/3/24 16:20
     * @param id: 任务id
     * @return: boolean true 开启任务成功 false 开启任务失败
     **/
    boolean startTask(long id);

    /*
     * @Description: 保存任务结果
     * @Author: dengbin
     * @Date: 3/3/24 16:23
     * @param taskId: 任务id
     * @param status: 任务状态
     * @param fileId: 文件id
     * @param url: url
     * @param errorMsg: 错误信息
     * @return: void
     **/
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
