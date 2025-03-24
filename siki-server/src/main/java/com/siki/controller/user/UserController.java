package com.siki.controller.user;

import com.siki.constant.JwtClaimsConstant;
import com.siki.dto.UserLoginDTO;
import com.siki.entity.User;
import com.siki.properties.JwtProperties;
import com.siki.result.Result;
import com.siki.service.UserService;
import com.siki.utils.JwtUtil;
import com.siki.vo.UserLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user/user")
@ApiOperation("用户管理")
@Slf4j
public class UserController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("sms")
    public Result sendCode(@RequestBody Map<String, String> params, HttpSession session) {
        String phone = params.get("phone");
        //发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO loginForm, HttpSession session){
        //实现登录功能
        User user = userService.login(loginForm, session);
        //生成token
        //生成jwt令牌
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
