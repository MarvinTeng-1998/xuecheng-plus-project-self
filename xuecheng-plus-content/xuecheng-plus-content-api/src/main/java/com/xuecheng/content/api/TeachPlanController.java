package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /*
     * @Description: 修改课程计划，添加课程计划
     * @Author: dengbin
     * @Date: 10/1/24 16:55
     * @param saveTeachplanDto:
     * @return: void
     **/
    @ApiOperation("修改课程计划，添加课程计划")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachPlanService.saveTeachplan(saveTeachplanDto);
    }

    /*
     * @Description: 删除课程计划
     * @Author: dengbin
     * @Date: 10/1/24 20:17
     * @param teachPlanId:
     * @return: void
     **/
    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable("teachPlanId") Long teachPlanId){
        teachPlanService.deleteTeachPlan(teachPlanId);
    }

    @ApiOperation("课程计划下移")
    @PostMapping("/teachplan/movedown/{teachPlanId}")
    public void moveDownTeachPlan(@PathVariable("teachPlanId") Long teachPlanId){
        teachPlanService.moveDownTeachPlan(teachPlanId);
    }

    @ApiOperation("课程计划上移")
    @PostMapping("/teachplan/moveup/{teachPlanId}")
    public void moveUpTeachPlan(@PathVariable("teachPlanId") Long teachPlanId){
        teachPlanService.moveUpTeachPlan(teachPlanId);
    }

}
