package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description: 教师Service
 * @author: dengbin
 * @create: 2024-01-10 22:03
 **/
public interface TeacherService {
    /*
     * @Description: 根据课程Id获取所有的教师
     * @Author: dengbin
     * @Date: 10/1/24 22:03
     * @param courseId:
     * @return: java.util.List<com.xuecheng.content.model.po.CourseTeacher>
     **/
    List<CourseTeacher> getCourseTeacher(Long courseId);

    /*
     * @Description: 添加教师
     * @Author: dengbin
     * @Date: 10/1/24 22:27
     * @param courseTeacher:
     * @return: com.xuecheng.content.model.po.CourseTeacher
     **/
    CourseTeacher addCourseTeacher(CourseTeacher courseTeacher, Long companyId);

    /*
     * @Description: 修改教师
     * @Author: dengbin
     * @Date: 10/1/24 23:43
     * @param courseTeacher:
     * @param companyId:
     * @return: com.xuecheng.content.model.po.CourseTeacher
     **/
    CourseTeacher modifyTeacher(CourseTeacher courseTeacher, Long companyId);

    /*
     * @Description: 删除教师
     * @Author: dengbin
     * @Date: 10/1/24 23:52
     * @param courseId:
     * @param teacherId:
     * @return: void
     **/
    void deleteTeacher(Long courseId, Long teacherId, Long companyId);
}
