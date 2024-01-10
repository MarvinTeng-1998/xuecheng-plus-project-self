package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 * @descrition: 教学计划Service
 * @author: dengbin
 * @create: 2024-01-09 03:08
 **/
public interface TeachPlanService {

    /*
     * @Description: 根据课程id查询课程计划
     * @Author: dengbin
     * @Date: 9/1/24 03:09
     * @param courseId: 
     * @return: java.util.List<com.xuecheng.content.model.dto.TeachPlanDto>
     **/
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);

    /*
     * @Description: 添加修改课程计划
     * @Author: dengbin
     * @Date: 10/1/24 16:57
     * @param saveTeachplanDto: 
     * @return: void
     **/
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /*
     * @Description: 删除课程计划
     * @Author: dengbin
     * @Date: 10/1/24 20:17
     * @param teachPlanId:
     * @return: void
     **/
    public void deleteTeachPlan(Long teachPlanId);

    /*
     * @Description: 下移课程计划
     * @Author: dengbin
     * @Date: 10/1/24 21:36
     * @param teachPlanId:
     * @return: void
     **/
    public void moveDownTeachPlan(Long teachPlanId);

    /*
     * @Description: 上移课程计划
     * @Author: dengbin
     * @Date: 10/1/24 21:45
     * @param teachPlanId:
     * @return: void
     **/
    void moveUpTeachPlan(Long teachPlanId);
}
