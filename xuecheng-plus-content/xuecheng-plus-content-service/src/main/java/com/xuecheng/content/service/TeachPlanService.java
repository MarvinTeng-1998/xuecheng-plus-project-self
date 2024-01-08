package com.xuecheng.content.service;

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
}
