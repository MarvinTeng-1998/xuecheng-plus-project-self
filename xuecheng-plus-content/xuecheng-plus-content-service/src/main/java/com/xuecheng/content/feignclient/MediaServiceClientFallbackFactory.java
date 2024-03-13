package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 服务熔断，拿到熔断的异常信息
 * @author: dengbin
 * @create: 2024-03-13 10:14
 **/
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    // 拿到了熔断的异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            // 发生熔断，上游服务就会调用此方法来实现降级逻辑
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.debug("远程调用上传文件的接口发生熔断:{}", throwable.toString(), throwable);
                return null;
            }
        };
    }

}
