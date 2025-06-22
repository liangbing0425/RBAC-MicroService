package org.shuzimali.permissionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shuzimali.permissionservice.entity.Role;
import org.shuzimali.permissionservice.entity.UserRole;
import org.shuzimali.permissionservice.mapper.RoleMapper;
import org.shuzimali.permissionservice.mapper.UserRoleMapper;
import org.shuzimali.permissionservice.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements PermissionService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public void bindDefaultRole(Long userId) {
        log.info("开始绑定默认角色 | userId={}", userId);
        // 绑定默认角色：普通用户（roleId=2）
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(2); // 普通用户角色ID
        userRoleMapper.insert(userRole);
        log.info("默认角色绑定成功 | userId={} | roleId=2", userId);
    }

    @Override
    public String getUserRoleCode(Long userId) {
        log.debug("开始查询用户角色编码 | userId={}", userId);
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        if (userRole == null) {
            log.debug("用户未分配角色，返回默认角色 | userId={} | roleCode=user", userId);
            return "user"; // 默认返回普通用户
        }

        Role role = roleMapper.selectById(userRole.getRoleId());
        log.debug("用户角色查询完成 | userId={} | roleCode={}", userId, role.getRoleCode());
        return role.getRoleCode();
    }

    @Override
    @Transactional
    public void upgradeToAdmin(Long userId) {
        log.info("开始升级用户为管理员 | userId={}", userId);
        // 升级为管理员角色（roleId=3）
        updateUserRole(userId, 3);
        log.info("用户升级为管理员成功 | userId={} | roleId=3", userId);
    }

    @Override
    @Transactional
    public void downgradeToUser(Long userId) {
        log.info("开始降级用户为普通用户 | userId={}", userId);
        // 降级为普通用户角色（roleId=2）
        updateUserRole(userId, 2);
        log.info("用户降级为普通用户成功 | userId={} | roleId=2", userId);
    }

    @Override
    public List<Role> getAllRoles() {
        log.debug("开始查询所有角色列表");
        List<Role> roles = roleMapper.selectList(null);
        log.debug("角色列表查询完成 | count={}", roles.size());
        return roles;
    }

    @Override
    public UserRole getUserRole(Long userId) {
        log.debug("开始查询用户角色关系 | userId={}", userId);
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        log.debug("用户角色关系查询完成 | userId={} | exists={}", userId, userRole != null);
        return userRole;
    }

    /**
     * 更新用户角色
     */
    private void updateUserRole(Long userId, Integer roleId) {
        log.debug("开始更新用户角色 | userId={} | targetRoleId={}", userId, roleId);
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        if (userRole != null) {
            log.debug("更新已有用户角色 | userId={} | oldRoleId={} | newRoleId={}",
                    userId, userRole.getRoleId(), roleId);
            userRole.setRoleId(roleId);
            userRoleMapper.updateById(userRole);
        } else {
            log.debug("创建新用户角色关系 | userId={} | roleId={}", userId, roleId);
            userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        log.debug("用户角色更新完成 | userId={} | roleId={}", userId, roleId);
    }
}