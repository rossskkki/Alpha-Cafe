package com.siki.controller.user;

import com.siki.constant.JwtClaimsConstant;
import com.siki.constant.MessageConstant;
import com.siki.context.BaseContext;
import com.siki.dto.UserLoginDTO;
import com.siki.dto.UserUpdateInfoDTO;
import com.siki.entity.User;
import com.siki.properties.JwtProperties;
import com.siki.result.Result;
import com.siki.service.UserService;
import com.siki.utils.AliOssUtil;
import com.siki.utils.JwtUtil;
import com.siki.vo.UserLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private AliOssUtil aliOssUtil;

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
                .nickName(user.getNickName())
                .phone(user.getPhone())
                .icon(user.getIcon())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 上传头像
     * @return 无
     */
    @PostMapping("/uploadIcon")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);
        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成新的文件名
            String objectName = UUID.randomUUID().toString() + extension;
            String filepath = aliOssUtil.upload(file.getBytes(), objectName);
            Long userId = BaseContext.getCurrentId();
            userService.updateIcon(userId, filepath);
            return Result.success(filepath);
        } catch (IOException e) {
            log.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

    /**
     * 修改用户信息
     * @param info 用户信息
     * @return 无
     */
    @PutMapping("/update")
    @ApiOperation("修改用户信息")
    public Result update(@RequestBody UserUpdateInfoDTO info){
        Long userId = BaseContext.getCurrentId();
        userService.updateById(userId, info);
        return Result.success();
    }

    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<User> info(){
        log.info("获取用户信息");
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        return Result.success(user);
    }
}
