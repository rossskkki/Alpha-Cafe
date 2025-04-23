package com.siki.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherAddOrderDTO {
    private Long id;

    private Long voucherId;

    private Integer payType;
}
