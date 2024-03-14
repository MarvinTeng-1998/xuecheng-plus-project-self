package com.xuecheng.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 从Sercurity中获取用户信息
 * @author: dengbin
 * @create: 2024-03-14 16:34
 **/
@Slf4j
public class SecurityUtil {

    public static XcUser getUser() {
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            if (principalObject instanceof String) {
                // 取出用户身份信息
                String principal = principalObject.toString();
                XcUser xcUser = JSON.parseObject(principal, XcUser.class);
                return xcUser;
            }
        } catch (Exception e) {
            log.error("获取当前用户信息出错:{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Data
    public static class XcUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;
        private String nickname;
        private String wxUnionid;
        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

    }
}
