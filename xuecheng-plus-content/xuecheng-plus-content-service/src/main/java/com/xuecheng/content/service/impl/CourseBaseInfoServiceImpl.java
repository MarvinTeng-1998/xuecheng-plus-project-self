package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: dengbin
 * @create: 2024-01-05 23:41
 **/
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        // 根据课程名模糊查询
        queryWrapper.like(!StringUtils.isBlank(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());

        // 根据课程审核状态查询
        queryWrapper.eq(!StringUtils.isBlank(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());

        // 根据课程发布状态查询
        queryWrapper.eq(!StringUtils.isBlank(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamsDto.getPublishStatus());

        // 创建分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();

        // 封装分页查询结果
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        // // 参数的合法校验
        // if (StringUtils.isBlank(dto.getName())) {
        //     XueChengPlusException.cast("课程名称为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getMt())) {
        //     XueChengPlusException.cast("课程分类为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getSt())) {
        //     XueChengPlusException.cast("课程分类为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getGrade())) {
        //     XueChengPlusException.cast("课程等级为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getTeachmode())) {
        //     XueChengPlusException.cast("教育模式为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getUsers())) {
        //     XueChengPlusException.cast("适应人群为空");
        // }
        //
        // if (StringUtils.isBlank(dto.getCharge())) {
        //     XueChengPlusException.cast("收费规则为空");
        // }

        // 向课程基本表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());

        // 审核状态默认为未提交
        courseBaseNew.setAuditStatus("202002");

        // 发布状态为未发布
        courseBaseNew.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            XueChengPlusException.cast("添加课程失败");
        }

        // 向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);

        // 课程的ID
        Long courseId = courseBaseNew.getId();
        courseMarket.setId(courseId);
        saveCourseMarket(courseMarket);

        // 从数据库查询课程信息的详细部分，包含两部分
        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto getCourseById(Long courseId) {
        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在！");
        }
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        BeanUtils.copyProperties(editCourseDto, courseBase);
        // 修改时间记录
        courseBase.setChangeDate(LocalDateTime.now());
        // 修改课程基本信息
        int i = courseBaseMapper.updateById(courseBase);
        if (i <= 0) {
            XueChengPlusException.cast("课程信息修改失败");
        }
        // 修改课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        courseMarket.setId(courseId);
        int n = saveCourseMarket(courseMarket);
        if (n <= 0) {
            XueChengPlusException.cast("课程营销信息更新失败");
        }
        return getCourseBaseInfo(courseId);
    }

    /*
     * @Description: 查询保存后的信息
     * @Author: dengbin
     * @Date: 8/1/24 02:27
     * @param courseId:
     * @return: com.xuecheng.content.model.dto.CourseBaseInfoDto
     **/
    private CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 通过CourseCategoryMapper查询到分类信息，将分类名称放在CourseBaseInfoDto对象中
        String mt = courseBase.getMt();
        String st = courseBase.getSt();
        CourseCategory courseCategory = courseCategoryMapper.selectById(mt);
        CourseCategory subCourseCategory = courseCategoryMapper.selectById(st);
        courseBaseInfoDto.setMtName(courseCategory.getName());
        courseBaseInfoDto.setMt(mt);
        courseBaseInfoDto.setStName(subCourseCategory.getName());
        courseBaseInfoDto.setSt(st);

        return courseBaseInfoDto;
    }

    /*
     * @Description: 保存营销信息
     * @Author: dengbin
     * @Date: 8/1/24 02:17
     * @param courseMarket:
     * @return: int
     **/
    private int saveCourseMarket(CourseMarket courseMarketNew) {

        // 参数的合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            XueChengPlusException.cast("收费规则为空");
        }

        // 如果课程收费，价格没有填写也需要抛出异常
        if ("201001".equals(charge)) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
                XueChengPlusException.cast("课程的价格不能为空并且必须大于0");
            }
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarket == null) {
            // 插入数据库
            return courseMarketMapper.insert(courseMarketNew);
        } else {
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            return courseMarketMapper.updateById(courseMarket);
        }
    }
}
