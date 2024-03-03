package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 媒资文件处理服务实现
 * @author: dengbin
 * @create: 2024-03-02 12:39
 **/
@Slf4j
public class MediaFileProcessServinceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {

        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySharedIndex(shardIndex, shardTotal, count);
        return mediaProcesses;

    }

    @Override
    public boolean startTask(long id) {

        int result = mediaProcessMapper.startTask(id);
        return result < 0 ? false : true;

    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {

        // 查出任务，如果不存在则直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }

        // 更新出错误的任务
        LambdaQueryWrapper<MediaProcess> queryWrapperById = new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        if ("3".equals(status)) {
            MediaProcess errorMediaProcess = new MediaProcess();
            errorMediaProcess.setStatus("3");
            errorMediaProcess.setErrormsg(errorMsg);
            errorMediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcessMapper.update(errorMediaProcess, queryWrapperById);
            log.debug("更新任务处理状态为失败，任务信息：{}", errorMediaProcess);
            return;
        }

        // 任务处理成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            // 更新媒资文件中访问的url地址
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        // 处理成功，更新任务过程的url和转台
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        // 添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 删除MediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }
}
