package com.siki.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateInfoDTO implements Serializable {
    private String nickName;
    private String phone;
}
