package com.siki.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

//    //员工id
//    private Long empId;

    //旧密码
    private String oldPwd;

    //新密码
    private String newPwd;

}
