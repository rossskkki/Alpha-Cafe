package com.siki.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {
    //地址簿id
//    private Long addressBookId;
    //付款方式
    private int payMethod;
    //备注
    private String remark;
    //预计送达时间
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime estimatedDeliveryTime;

    //预计完成时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedFinishedTime;

    //配送状态  1立即送出  0选择具体时间
//    private Integer deliveryStatus;

    //点单状态  1立即下单  0预约下单
    private Integer orderStatus;

    //餐具数量
//    private Integer tablewareNumber;
    //餐具数量状态  1按餐量提供  0选择具体数量
//    private Integer tablewareStatus;
    //打包费
//    private Integer packAmount;
    //总金额
    private BigDecimal amount;
}
