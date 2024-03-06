package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.RestErrorResponse;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: TeachPlanService的实现
 * @author: dengbin
 * @create: 2024-01-09 03:14
 **/

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        if (id != null) {
            // 表示修改子层级
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            // 表示添加新的层级
            // 查询当前层级应该在的顺序，并加一
            int count = getTeachCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            Teachplan teachPlan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachPlan);
            teachPlan.setOrderby(count + 1);
            teachplanMapper.insert(teachPlan);
        }
    }

    @Override
    @Transactional
    public void deleteTeachPlan(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        Long parentId = teachplan.getParentid();
        if (parentId != 0) {
            teachplanMapper.deleteById(teachPlanId);
            LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
            teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);
        } else {
            // 查询这个parentId下还有没有subTeachPlan
            int teachCount = getTeachCount(teachplan.getCourseId(), teachPlanId);
            if (teachCount > 0) {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            } else {
                teachplanMapper.deleteById(teachPlanId);
            }
        }
    }

    @Override
    @Transactional
    public void moveDownTeachPlan(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        int orderby = teachplan.getOrderby() + 1;
        teachplan.setOrderby(orderby);

        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        teachplanLambdaQueryWrapper.eq(Teachplan::getOrderby, orderby);
        Teachplan teachPlanDown = teachplanMapper.selectOne(teachplanLambdaQueryWrapper);
        teachPlanDown.setOrderby(orderby - 1);

        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachPlanDown);
    }

    @Override
    @Transactional
    public void moveUpTeachPlan(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        int orderby = teachplan.getOrderby() - 1;
        teachplan.setOrderby(orderby);

        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        teachplanLambdaQueryWrapper.eq(Teachplan::getOrderby, orderby);
        Teachplan teachPlanDown = teachplanMapper.selectOne(teachplanLambdaQueryWrapper);
        teachPlanDown.setOrderby(orderby + 1);

        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachPlanDown);
    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        Long courseId = teachplan.getCourseId();

        // 1. 先删除原有记录
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,
                bindTeachplanMediaDto.getTeachplanId()));

        // 2. 添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setTeachplanId(bindTeachplanMediaDto.getTeachplanId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    /*
     * @Description: 获取最新的排序号
     * @Author: dengbin
     * @Date: 10/1/24 17:24
     * @param courseId:
     * @param parentId:
     * @return: int
     **/
    private int getTeachCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }

}
