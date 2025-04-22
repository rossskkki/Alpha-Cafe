package com.siki.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoucherPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String title;
    
}
