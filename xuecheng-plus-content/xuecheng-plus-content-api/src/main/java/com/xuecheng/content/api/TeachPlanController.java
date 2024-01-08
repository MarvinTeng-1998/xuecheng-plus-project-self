package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 课程计划Controller
 * @author: dengbin
 * @create: 2024-01-08 23:24
 **/
@RestController
public class TeachPlanController {

    @Autowired
    private TeachPlanService teachPlanService;

    /*
     * @Description: 查询课程计划树形结构
     * @Author: dengbin
     * @Date: 8/1/24 23:27
     * @param courseId:
     * @return: com.xuecheng.content.model.dto.TeachPlanDto
     **/
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachPlanService.findTeachPlanTree(courseId);
    }
}
