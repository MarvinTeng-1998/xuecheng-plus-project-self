package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

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
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /*
     * @Description: 课程提交审核
     * @Author: dengbin
     * @Date: 7/3/24 22:06
     * @param companyId: 机构ID
     * @param courseId: 课程ID
     * @return: void
     **/
    public void commitAudit(Long companyId, Long courseId);

    /*
     * @Description: 课程发布
     * @Author: dengbin
     * @Date: 8/3/24 16:04
     * @param companyId: 机构ID
     * @param courseId: 课程ID
     * @return: void
     **/
    public void publish(Long companyId, Long courseId);
}
