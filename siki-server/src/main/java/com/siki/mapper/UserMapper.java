package com.siki.mapper;

import com.siki.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getUserByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */
    void insertUser(User user);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 查询新用户数量
     * @param map
     */
    Integer countNewUserByMap(Map map);

    /**
     * 查询用户数量
     * @param map
     */
    Integer countUserByMap(Map map);
}
