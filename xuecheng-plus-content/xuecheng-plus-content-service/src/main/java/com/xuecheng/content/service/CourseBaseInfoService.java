package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.stereotype.Service;

/**
 * @TODO:
 * @author: dengbin
 * @create: 2024-01-05 22:55
 **/
public interface CourseBaseInfoService {

    /*
     * @Description: 课程分页查询
     * @Author: dengbin
     * @Date: 5/1/24 23:40
     * @param pageParams:
     * @param queryCourseParamsDto:
     * @return: com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     **/
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
