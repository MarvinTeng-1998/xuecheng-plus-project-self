package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @descprition: 课程分类节点DTO
 * @author: dengbin
 * @create: 2024-01-06 19:47
 **/
@Data
public class CourseCategoryTreeDto extends CourseCategory implements java.io.Serializable{
        List<CourseCategory> childrenTreeNodes;
}
