package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description: 课程计划信息模型类
 * @author: dengbin
 * @create: 2024-01-08 23:20
 **/
@Data
@ToString
public class TeachPlanDto extends Teachplan {

    // 与媒资关联的信息
    private TeachplanMedia teachplanMedia;

    // 小章节list
    private List<TeachPlanDto> teachPlanTreeNodes;

}
