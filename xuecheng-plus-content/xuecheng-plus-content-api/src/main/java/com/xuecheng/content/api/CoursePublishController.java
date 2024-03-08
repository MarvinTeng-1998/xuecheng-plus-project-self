package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @description: 课程发布Controller
 * @author: dengbin
 * @create: 2024-03-06 19:19
 **/

@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    /*
     * @Description: 课程预览发布
     * @Author: dengbin
     * @Date: 6/3/24 19:21
     * @param courseId: 课程ID
     * @return: org.springframework.web.servlet.ModelAndView
     **/
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        // 查询课程信息作为模型数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /*
     * @Description: 提交课程审核
     * @Author: dengbin
     * @Date: 7/3/24 22:02
     * @param courseId: 课程ID
     * @return: void
     **/
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    /*
     * @Description: 课程发布
     * @Author: dengbin
     * @Date: 8/3/24 16:02
     * @param courseId:
     * @return: void
     **/
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }
}
