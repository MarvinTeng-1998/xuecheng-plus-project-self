package com.xuecheng.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @description: 自定义DaoAuthenticationProvider类，进行认证模式的定义，我们统一了认证入口，有一些认证方式不需要校验密码
 * @author: dengbin
 * @create: 2024-03-14 18:57
 **/
@Component
@Slf4j
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    @Autowired
    public void serUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    /*
     * @Description: 屏蔽原有的密码认证方式，使用新的方式
     * @Author: dengbin
     * @Date: 14/3/24 18:59
     * @param userDetails:
     * @param authentication:
     * @return: void
     **/
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }
}
