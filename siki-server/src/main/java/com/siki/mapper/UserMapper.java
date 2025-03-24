package com.siki.mapper;

import com.siki.annotation.AutoFill;
import com.siki.entity.User;
import com.siki.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据phone查询用户
     * @param phone
     * @return
     */
    @Select("select * from user where phone = #{phone}")
    User getUserByPhone(String phone);

    /**
     * 新增用户
     * @param user
     */
//    @AutoFill(value = OperationType.INSERT)
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
