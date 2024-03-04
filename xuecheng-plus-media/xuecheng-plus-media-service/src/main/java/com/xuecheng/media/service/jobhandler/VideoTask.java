package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description: 任务处理
 * @author: dengbin
 * @create: 2024-03-03 18:07
 **/
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;

        try {
            // cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            // 一次处理的视频数不要超过CPU核心数
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理的视频条数：{}", size);
            if (size < 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 启动Size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 将待处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                Long taskId = mediaProcess.getId();
                // 抢占任务
                boolean b = mediaFileProcessService.startTask(taskId);
                if (!b) {
                    return;
                }
                log.debug("开始执行任务{}", mediaProcess);

                // 获取桶相关参数
                String bucket = mediaProcess.getBucket();
                // 存储路径
                String filePath = mediaProcess.getFilePath();
                // 原始视频的md5值
                String fileId = mediaProcess.getFileId();
                // 原始文件名称
                String filename = mediaProcess.getFilename();
                // 将要处理的文件下载到服务器上
                File originalFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                if (originalFile == null) {
                    log.debug("待处理的文件下载失败,originialFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                    return;
                }

                // 处理结束的视频文件
                File mp4File = null;
                try {
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("创建mp4临时文件失败");
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                    return;
                }

                // 视频处理结果
                String result = "";
                try {
                    // 开始处理视频
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4File.getName(),mp4File.getAbsolutePath());
                    result = videoUtil.generateMp4();
                } catch (Exception e) {
                    log.error("处理视频文件：{}，出错：{}", mediaProcess.getFilePath(), e.getMessage());
                    e.printStackTrace();
                }

                if (!"success".equals(result)) {
                    // 记录错误信息
                    log.error("处理视频失败，视频地址：{}，错误信息：{}", bucket + filePath, result);
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                    return;
                }

                // 将mp4上传至minio
                // mp4在minio的存储路径
                String objectName = getFilePath(fileId, ".mp4");
                // 访问url
                String url = "/" + bucket + "/" + objectName;
                try {
                    mediaFileService.addMediaFilesToMinio(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                    log.debug("上传视频成功，视频地址：{}", bucket + objectName);
                } catch (Exception e) {
                    log.error("上传视频失败或入库失败，视频地址：{}, 错误信息：{}", bucket + objectName, e.getMessage());
                    // 最终还是失败了
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理视频后上传或入库失败");
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        // 等待，给一个充裕的超时时间，防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /*
     * @Description: 获取文件在minio上的地址
     * @Author: dengbin
     * @Date: 4/3/24 18:30
     * @param fileMd5: 视频文件的md5
     * @param fileExt: 视频文件的结尾
     * @return: java.lang.String
     **/
    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
