package com.xuecheng.content;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author: dengbin
 * @create: 2024-01-09 03:05
 **/
@SpringBootTest
public class TeachPlanMapperTest {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    public void testTeachPlanMapper(){
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }
}
