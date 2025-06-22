package org.shuzimali.permissionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.shuzimali.permissionservice.entity.Role;
import org.shuzimali.permissionservice.entity.UserRole;
import org.shuzimali.permissionservice.mapper.RoleMapper;
import org.shuzimali.permissionservice.mapper.UserRoleMapper;
import org.shuzimali.permissionservice.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements PermissionService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public void bindDefaultRole(Long userId) {
        // 绑定默认角色：普通用户（roleId=2）
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(2); // 普通用户角色ID
        userRoleMapper.insert(userRole);
    }

    @Override
    public String getUserRoleCode(Long userId) {
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        if (userRole == null) {
            return "user"; // 默认返回普通用户
        }

        Role role = roleMapper.selectById(userRole.getRoleId());
        return role.getRoleCode();
    }

    @Override
    @Transactional
    public void upgradeToAdmin(Long userId) {
        // 升级为管理员角色（roleId=3）
        updateUserRole(userId, 3);
    }

    @Override
    @Transactional
    public void downgradeToUser(Long userId) {
        // 降级为普通用户角色（roleId=2）
        updateUserRole(userId, 2);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public UserRole getUserRole(Long userId) {
        return userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
    }

    /**
     * 更新用户角色
     */
    private void updateUserRole(Long userId, Integer roleId) {
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        if (userRole != null) {
            userRole.setRoleId(roleId);
            userRoleMapper.updateById(userRole);
        } else {
            userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}