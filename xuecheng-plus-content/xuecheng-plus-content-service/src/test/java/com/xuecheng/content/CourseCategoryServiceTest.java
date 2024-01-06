package com.xuecheng.content;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @TODO:
 * @author: dengbin
 * @create: 2024-01-06 23:25
 **/
@SpringBootTest
public class CourseCategoryServiceTest {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testCourseCategoryService(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
