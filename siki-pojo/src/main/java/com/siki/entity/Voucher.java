package com.siki.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher implements Serializable{
    private static final long serialVersionUID = 1L;

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

    //优惠券状态 1-上架 2-下架 3-过期
    private Integer status;

    //创建时间
    private Long createTime;
}
