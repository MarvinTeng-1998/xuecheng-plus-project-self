package com.xuecheng.content.feignclient;

import com.xuecheng.content.model.dto.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: 搜索服务远程接口
 * @author: dengbin
 * @create: 2024-03-13 14:49
 **/
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    public boolean add(@RequestBody CourseIndex courseIndex);

}
