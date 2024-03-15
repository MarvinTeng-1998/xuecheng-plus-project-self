package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @description: 课程发布服务
 * @author: dengbin
 * @create: 2024-03-06 19:39
 **/
public interface CoursePublishService {

    /*
     * @Description: 获取课程预览信息
     * @Author: dengbin
     * @Date: 6/3/24 19:40
     * @param courseId: 课程ID
     * @return: com.xuecheng.content.model.dto.CoursePreviewDto
     **/
     CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /*
     * @Description: 课程提交审核
     * @Author: dengbin
     * @Date: 7/3/24 22:06
     * @param companyId: 机构ID
     * @param courseId: 课程ID
     * @return: void
     **/
     void commitAudit(Long companyId, Long courseId);

    /*
     * @Description: 课程发布
     * @Author: dengbin
     * @Date: 8/3/24 16:04
     * @param companyId: 机构ID
     * @param courseId: 课程ID
     * @return: void
     **/
     void publish(Long companyId, Long courseId);

    /*
     * @Description: 生成课程静态页面
     * @Author: dengbin
     * @Date: 13/3/24 10:21
     * @param courseId:
     * @return: java.io.File
     **/
     File generateCourseHtml(long courseId);

     /*
      * @Description: 上传课程静态文件
      * @Author: dengbin
      * @Date: 13/3/24 10:33
      * @param courseId:
      * @param file:
      * @return: void
      **/
     void uploadCourseHtml(Long courseId, File file);

     /*
      * @Description: 查询课程发布信息
      * @Author: dengbin
      * @Date: 15/3/24 22:37
      * @param courseId:
      * @return: com.xuecheng.content.model.po.CoursePublish
      **/
    CoursePublish getCoursePublish(Long courseId);
}
