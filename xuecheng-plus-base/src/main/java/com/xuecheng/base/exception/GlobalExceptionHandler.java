package com.xuecheng.base.exception;


import com.xuecheng.base.enums.CommonError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 异常全局处理器
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
    @ExceptionHandler({XueChengPlusException.class})
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
        e.printStackTrace();
        if (e.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("没有操作此功能的权限");
        }
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    /*
     * @Description: 参数校验异常抛出
     * @Author: dengbin
     * @Date: 8/1/24 19:19
     * @param e:
     * @return: com.xuecheng.base.exception.RestErrorResponse
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse validateArgumentsException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(item ->
                errors.add(item.getDefaultMessage())
        );

        String errMessage = StringUtils.join(errors, ",");
        log.error("系统异常：{}", errMessage);
        return new RestErrorResponse(errMessage);
    }

}
