package com.xuecheng.base.exception;

import com.xuecheng.base.enums.CommonError;

/**
 * @自定义异常类型
 * @author: dengbin
 * @create: 2024-01-08 11:10
 **/
public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    public XueChengPlusException() {

    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public static void cast(String message) {
        throw new XueChengPlusException(message);
    }

    public static void cast(CommonError error) {
        throw new XueChengPlusException(error.getErrMessage());
    }
}
