package org.shuzimali.userservice.mapper;

import org.apache.ibatis.annotations.*;
import org.shuzimali.userservice.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Insert("INSERT INTO users (user_id, username, password, email, phone, gmt_create) " +
            "VALUES (#{userId}, #{username}, #{password}, #{email}, #{phone}, #{gmtCreate})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE users SET email = #{email}, phone = #{phone} WHERE user_id = #{userId}")
    int update(User user);

    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(1) FROM users WHERE email = #{email}")
    int countByEmail(@Param("email") String email);
}
