package com.xuecheng.content.feignclient;

import com.xuecheng.search.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description: 搜索服务客户端回调工厂
 * @author: dengbin
 * @create: 2024-03-13 14:50
 **/
@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public boolean add(CourseIndex courseIndex) {
                throwable.printStackTrace();
                log.debug("调用搜索发生熔断走降级方法，熔断异常：{}", throwable.getMessage());

                return false;
            }
        };
    }
}
