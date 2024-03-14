package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @description: 认证service
 * @author: dengbin
 * @create: 2024-03-14 19:12
 **/
public interface AuthService {

    /*
     * @Description: 认证方法
     * @Author: dengbin
     * @Date: 14/3/24 19:13
     * @param authParamsDto: 认证参数
     * @return: com.xuecheng.ucenter.model.dto.XcUserExt
     **/
    XcUserExt execute(AuthParamsDto authParamsDto);
}
