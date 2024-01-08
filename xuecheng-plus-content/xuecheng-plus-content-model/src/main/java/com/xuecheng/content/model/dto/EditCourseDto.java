package com.xuecheng.content.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: dengbin
 * @create: 2024-01-08 22:00
 **/
@Data
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程id", required = true)
    @JsonProperty("id")
    private Long courseId;

}
