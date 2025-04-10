package com.siki.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.siki.constant.MessageConstant;
import com.siki.context.BaseContext;
import com.siki.dto.UserDTO;
import com.siki.dto.UserLoginDTO;
import com.siki.dto.UserUpdateInfoDTO;
import com.siki.entity.User;
import com.siki.exception.LoginFailedException;
import com.siki.mapper.UserMapper;
import com.siki.properties.WeChatProperties;
import com.siki.result.Result;
import com.siki.utils.HttpClientUtil;
import com.siki.utils.RegexUtils;
import com.siki.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import static com.siki.constant.RedisConstants.*;
import static com.siki.constant.SystemConstants.USER_NICK_NAME_PREFIX;

@Service
@Slf4j
public class UserServiceImpl implements com.siki.service.UserService {
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
//
//    /**
//     * 微信登录
//     * @param userLoginDTO
//     * @return
//     */
//    public User wxLogin(UserLoginDTO userLoginDTO) {
//        //调用微信接口服务
//        String openid = getOpenid(userLoginDTO.getCode());
//
//        //判断openid是否为空，如果为空则登录失败
//        if (openid == null) {
//            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
//        }
//
//        //判断用户是否为新用户
//        User user = userMapper.getUserByOpenid(openid);
//
//        //如果是新用户，则注册用户
//        if (user == null) {
//            user = User.builder()
//                    .openid(openid)
//                    .createTime(LocalDateTime.now())
//                    .build();
//            userMapper.insertUser(user);
//        }
//
//        //返回用户对象
//        return user;
//    }
//
//    /**
//     * 调用微信接口服务
//     * @param code
//     * @return
//     */
//    private String getOpenid(String code) {
//        HashMap<String, String> map = new HashMap<>();
//        map.put("appid", weChatProperties.getAppid());
//        map.put("secret", weChatProperties.getSecret());
//        map.put("js_code", code);
//        map.put("grant_type", "authorization_code");
//        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
//
//        JSONObject jsonObject = JSON.parseObject(json);
//        String openid = jsonObject.getString("openid");
//        return openid;
//    }

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合要求，抛出异常
            return Result.error("手机号格式不正确");
        }
        //生成验证码
        String code = RandomStringUtils.randomNumeric(6);
        //保存验证码到redis
        redisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //发送验证码
        log.info("向手机号{}发送验证码：{}", phone, code);
        //返回ok
        return Result.success();
    }

    @Override
    public User login(UserLoginDTO loginForm, HttpSession session) {
        //校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合要求，抛出异常
            throw new LoginFailedException(MessageConstant.PHONE_FORMAT_ERROR);
        }
        //从redis获取并校验验证码
        String cachecode = (String) redisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cachecode == null || !cachecode.equals(code)) {
            //不一致，返回错误
           throw new LoginFailedException(MessageConstant.CODE_ERROR);
        }
        //校验用户是否存在
        User user = userMapper.getUserByPhone(phone);
        if (user == null) {
            //不存在，创建新用户
            user = createUserWithPhone(phone);
        }
//        //保存用户信息到redis
//        //随机生成token，作为登录凭证
//        String token = UUID.randomUUID().toString();
//        //将user对象转换为hash对象存储
//        UserDTO userDTO = new UserDTO();
//        BeanUtils.copyProperties(user, userDTO);
//        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
//                CopyOptions.create()
//                        .setIgnoreNullValue(true)
//                        .setFieldValueEditor((fieldname, fieldValue) -> fieldValue == null ? "" : fieldValue.toString()));
//        //存储
//        redisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
//        //设置过期时间
//        redisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
//        //返回token
        return user;
    }

    @Override
    public void updateIcon(Long userId, String filepath) {
        User user = userMapper.getUserById(userId);
        user.setIcon(filepath);
        userMapper.updateUser(user);
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
    }

    @Override
    public User getById(Long userId) {
        //根据id查询用户
        User user = userMapper.getUserById(userId);
        //返回用户信息
        return user;
    }


    @Override
    public void updateById(Long userId, UserUpdateInfoDTO info) {
        User user = new User();
        //根据id查询用户
        user.setId(userId);
        if (info.getPhone() == ""){
            info.setPhone(null);
        }
        if (info.getNickName() == ""){
            info.setNickName(null);
        }
        BeanUtils.copyProperties(info, user);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateUser(user);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomStringUtils.randomNumeric(10));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insertUser(user);
        return user;
    }
}
