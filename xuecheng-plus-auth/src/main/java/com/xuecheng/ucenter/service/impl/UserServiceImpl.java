package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 用户查询服务实现类，用来修改SpringSecurity中的UserService
 * @author: dengbin
 * @create: 2024-03-14 00:27
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper menuMapper;

    /*
     * @Description: 查询用户信息组成用户身份信息 UserJson to UserDetails
     * @Author: dengbin
     * @Date: 14/3/24 00:32
     * @param s: AuthParamsDto类型的json数据
     * @return: org.springframework.security.core.userdetails.UserDetails
     **/
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;

        try {
            // 将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求参数不符合项目要求：{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        // 认证方法
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);
        return getUserPrincipal(user);

    }

    /*
     * @Description: 查询用户信息
     * @Author: dengbin
     * @Date: 14/3/24 19:48
     * @param user: 用户id 主键
     * @return: org.springframework.security.core.userdetails.UserDetails
     **/
    private UserDetails getUserPrincipal(XcUserExt user) {
        String password = user.getPassword();
        // 查询用户权限
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size() <= 0) {
            // 用户权限，如果不加则报Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        } else {
            xcMenus.forEach(menu -> {
                permissions.add(menu.getCode());
            });
        }
        // 将用户权限放在XcUserExt中
        user.setPermissions(permissions);

        // 为了安全在令牌中不放密码
        user.setPassword(null);
        // 将user对象转json
        String userString = JSON.toJSONString(user);
        String[] authorities = permissions.toArray(new String[0]);
        // 创建userDetails对象
        return User.withUsername(userString).password(password).authorities(authorities).build();
    }
}
