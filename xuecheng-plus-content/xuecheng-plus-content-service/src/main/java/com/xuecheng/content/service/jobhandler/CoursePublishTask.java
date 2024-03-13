package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.search.po.CourseIndex;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @description: 课程发布任务处理
 * @author: dengbin
 * @create: 2024-03-08 20:17
 **/
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    SearchServiceClient searchServiceClient;


    // 任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        // 参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    /*
     * @Description: 课程发布任务处理
     * @Author: dengbin
     * @Date: 8/3/24 20:18
     * @param mqMessage: 消息
     * @return: boolean
     **/
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        // 课程静态化
        generateCourseHtml(mqMessage, courseId);
        // 课程索引
        saveCourseIndex(mqMessage, courseId);
        // 课程缓存
        saveCourseCache(mqMessage, courseId);
        return true;
    }

    /*
     * @Description: 生成课程静态化页面并上传文件系统
     * @Author: dengbin
     * @Date: 8/3/24 20:20
     * @param mqMessage:
     * @param courseId:
     * @return: void
     **/
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程静态化，课程id:{}", courseId);
        // 消息ID
        Long id = mqMessage.getId();
        // 消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("课程静态化已处理直接返回，课程id:{}", courseId);
            return;
        }
        // 生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        // 上传静态化页面
        if (file != null) {
            coursePublishService.uploadCourseHtml(courseId, file);
            // 保存第一阶段状态
            mqMessageService.completedStageOne(id);
        }
    }

    /*
     * @Description: 将课程信息缓存至Redis
     * @Author: dengbin
     * @Date: 9/3/24 01:04
     * @param mqMessage: 消息
     * @param courseId: 课程ID
     * @return: void
     **/
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息，课程id:{}", courseId);
        // 消息id
        Long id = mqMessage.getId();
        // 消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo > 0) {
            log.debug("课程索引已处理直接返回，课程id:{}", courseId);
            return;
        }

        boolean result = saveCourseIndex(courseId);
        if (result) {
            // 保存消息状态
            mqMessageService.completedStageTwo(id);
        }
    }

    /*
     * @Description: 保存添加的课程信息到ES索引中
     * @Author: dengbin
     * @Date: 13/3/24 15:00
     * @param courseId:
     * @return: boolean
     **/
    private boolean saveCourseIndex(long courseId) {
        // 取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        // 拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        // 远程调用
        boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
            XueChengPlusException.cast("添加索引失败！");
        }
        return add;
    }

    /*
     * @Description: 保存课程索引信息
     * @Author: dengbin
     * @Date: 9/3/24 01:06
     * @param mqMessage: 消息
     * @param courseId: 课程ID
     * @return: void
     **/
    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息，课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
