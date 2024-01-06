package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: CourseCategoryService的实现
 * @author: dengbin
 * @create: 2024-01-06 22:54
 **/
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> selectTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 找到每个节点的子节点，最终封装完成
        // 先将list转为map，key是节点的id，values是CourseCategoryTreeDto对象，目的就是为了方便从map获取节点。并且通过filter去除id
        // stream流实现的是浅拷贝，也就是在Map中的value本质还是这个List中的对象
        Map<String, CourseCategoryTreeDto> courseCategoryMap = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(
                        CourseCategory::getId,
                        value -> value,
                        (key1, key2) -> key2)
                );

        // 定义一个List作为最终返回的List
        List<CourseCategoryTreeDto> result = new ArrayList<>();

        // 从头遍历List<CourseCategoryTreeDto>，一边遍历一边找子节点放在父节点的childrenTreeNotes中
        courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .forEach(item -> {
                    // 向list中写入元素
                    if(item.getParentid().equals(id)){
                        result.add(item);
                    }else{
                        // 找到节点的父节点
                        CourseCategoryTreeDto courseCategoryTreeDto = courseCategoryMap.get(item.getParentid());
                        // 到每个节点的子节点放在父节点的childrenTreeNodes中
                        if(courseCategoryTreeDto.getChildrenTreeNodes() == null){
                            courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<>());
                        }
                        courseCategoryTreeDto.getChildrenTreeNodes().add(item);
                    }
                });
        return result;
    }

}
