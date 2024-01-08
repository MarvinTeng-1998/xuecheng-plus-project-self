package com.xuecheng.base.exception;

import com.xuecheng.base.enums.CommonError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @TODO:
 * @author: dengbin
 * @create: 2024-01-08 11:16
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * @Description: 自定义异常的抛出
     * @Author: dengbin
     * @Date: 8/1/24 11:21
     * @param e:
     * @return: com.xuecheng.base.exception.RestErrorResponse
     **/
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        log.error("系统异常：{}", e.getErrMessage(), e);
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }

    /*
     * @Description: 系统异常的抛出
     * @Author: dengbin
     * @Date: 8/1/24 11:22
     * @param e:
     * @return: com.xuecheng.base.exception.RestErrorResponse
     **/
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
