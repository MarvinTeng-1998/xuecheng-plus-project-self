package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Mr.M
 * @version 1.0
 * @description 安全管理配置
 * @date 2022/9/26 20:53
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码为明文方式
        // return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }

    // 配置安全拦截机制
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/r/**").authenticated()// 访问/r开始的请求需要认证通过
                .anyRequest().permitAll()// 其它请求全部放行
                .and()
                .formLogin().successForwardUrl("/login-success");// 登录成功跳转到/login-success
    }

    /*
     * @Description: 重写AuthenticationManagerBuilder，使用自定义的daoAuthenticationProvider
     * @Author: dengbin
     * @Date: 14/3/24 19:00
     * @param auth:
     * @return: void
     **/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }

    // 这里可以看到每次密文都不一样， 但是每次进行匹配的值都是一样的
    public static void main(String[] args) {
        String password = "111111";
        // BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // for (int i = 0; i < 10; i++) {
        //     String hashpass = passwordEncoder.encode(password);
        //     System.out.println(hashpass);
        //     boolean matches = passwordEncoder.matches(password, hashpass);
        //     System.out.println(matches);
        // }
        String passwordForm = "$2a$10$0pt7WlfTbnPDTcWtp/.2Mu5CTXvohnNQhR628qq4RoKSc0dGAdEgm";
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.matches(password, passwordForm));
    }

}
