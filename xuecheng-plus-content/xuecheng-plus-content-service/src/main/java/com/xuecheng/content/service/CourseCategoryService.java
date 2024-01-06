package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @description: 课程分类Service
 * @author: dengbin
 * @create: 2024-01-06 22:53
 **/
public interface CourseCategoryService {
     List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
