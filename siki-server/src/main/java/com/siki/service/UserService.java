package com.siki.service;

import javax.servlet.http.HttpSession;

import com.siki.dto.UserLoginDTO;
import com.siki.entity.User;
import com.siki.result.Result;
import com.siki.vo.UserLoginVO;

public interface UserService {
    /**
     * 发送验证码
     * @param phone
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 登录
     * @param loginForm
     * @param session
     * @return
     */
    User login(UserLoginDTO loginForm, HttpSession session);

    void updateIcon(Long userId, String filepath);

    User getById(Long userId);

    void updateById(Long userId, String name);
}
