package com.siki.dto;

import lombok.Data;

@Data
public class VoucherAddDTO {
    private Long id;

    //代金券名称
    private String title;

    //代金券描述
    private String subTitle;

    //代金券使用说明
    private String rules;

    //代金券金额
    private Long actualValue;

    //支付金额
    private Long payValue;

    //代金券开始时间
    private Long beginTime;

    //代金券结束时间
    private Long endTime;

    //剩余库存
    private Integer stock;
}
