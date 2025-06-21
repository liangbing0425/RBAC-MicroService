package org.shuzimali.permissionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shuzimali.permissionservice.entity.UserRole;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT * FROM user_roles WHERE user_id = #{userId}")
    UserRole selectByUserId(Long userId);
}
