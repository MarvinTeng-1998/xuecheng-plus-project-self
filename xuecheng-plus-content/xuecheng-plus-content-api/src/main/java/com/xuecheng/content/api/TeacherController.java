package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @decription: 教师管理
 * @author: dengbin
 * @create: 2024-01-10 22:01
 **/
@RestController
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @ApiOperation("查询所有教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable("courseId") Long courseId) {
        return teacherService.getCourseTeacher(courseId);
    }

    @ApiOperation("添加教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        return teacherService.addCourseTeacher(courseTeacher, companyId);
    }

    @ApiOperation("修改教师")
    @PutMapping("/courseTeacher")
    public CourseTeacher modifyTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        return teacherService.modifyTeacher(courseTeacher, companyId);
    }

    @ApiOperation("删除教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable("courseId") Long courseId,
                              @PathVariable("teacherId") Long teacherId){
        Long companyId = 1232141425L;
        teacherService.deleteTeacher(courseId, teacherId, companyId);
    }
}
