package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description: 课程预览数据模型
 * @author: dengbin
 * @create: 2024-03-06 19:37
 **/
@Data
@ToString
public class CoursePreviewDto {

    // 课程基本信息，课程营销信息
    CourseBaseInfoDto courseBase;

    // 课程计划信息
    List<TeachPlanDto> teachplans;

}
