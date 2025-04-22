package com.siki.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    //代金券结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    //剩余库存
    private Integer stock;
}
