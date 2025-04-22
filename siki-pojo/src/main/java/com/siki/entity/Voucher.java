package com.siki.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    //代金券结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    //剩余库存
    private Integer stock;

    //优惠券状态 1-上架 2-下架 3-过期
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}
