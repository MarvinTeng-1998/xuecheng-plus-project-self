package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @TODO:
 * @author: dengbin
 * @create: 2024-03-14 22:53
 **/
@Component
@Slf4j
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常：{}", throwable.getMessage());
                return false;
            }
        };
    }
}
