package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @description: 分页查询分页参数
 * @author: dengbin
 * @create: 2024-01-04 21:42
 **/
@Data
@ToString
public class PageParams {

    private Long pageNo = 1L;

    private Long pageSize = 30L;

    public PageParams() {
    }

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
