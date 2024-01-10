package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: 课程老师查询
 * @author: dengbin
 * @create: 2024-01-10 22:04
 **/
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("只允许向机构自己的课程中添加老师");
        }
        courseTeacherMapper.insert(courseTeacher);
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public CourseTeacher modifyTeacher(CourseTeacher courseTeacher, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("只允许向机构自己的课程中进行老师修改");
        }
        CourseTeacher courseTeacherOld = courseTeacherMapper.selectById(courseTeacher.getId());
        BeanUtils.copyProperties(courseTeacher, courseTeacherOld);
        courseTeacherMapper.updateById(courseTeacherOld);
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("只允许向机构自己的课程中删除教师信息");
        }
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId, courseId);
        wrapper.eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(wrapper);
    }
}
