package com.siki.service;

import com.siki.dto.UserLoginDTO;
import com.siki.entity.User;

public interface UserService {
    /**
     *  微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
