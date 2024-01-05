package com.xuecheng.system.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @description: 跨域配置管理
 * @author: dengbin
 * @create: 2024-01-06 04:05
 **/
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许所有来源跨域访问
        corsConfiguration.addAllowedOrigin("*");
        // 允许跨越发送cookie
        corsConfiguration.setAllowCredentials(true);
        // 放行全部原始头信息
        corsConfiguration.addAllowedHeader("*");
        // 允许所有请求方法跨域访问
        corsConfiguration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
