package org.shuzimali.permissionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shuzimali.permissionservice.entity.Role;

/**
 * 角色表数据访问接口
 * 对应数据库表: roles
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}
