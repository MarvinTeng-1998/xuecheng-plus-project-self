package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author marvin
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /*
     * @Description: 查询课程计划的树形结构
     * @Author: dengbin
     * @Date: 8/1/24 23:28
     * @param courseId:
     * @return: java.util.List<com.xuecheng.content.model.dto.TeachPlanDto>
     **/
    List<TeachPlanDto> selectTreeNodes(Long courseId);
}
