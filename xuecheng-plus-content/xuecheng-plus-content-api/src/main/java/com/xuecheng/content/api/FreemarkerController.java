package com.xuecheng.content.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @description: 模版测试
 * @author: dengbin
 * @create: 2024-03-06 17:14
 **/
@RestController
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "小明");
        modelAndView.setViewName("test");
        return modelAndView;
    }

}
