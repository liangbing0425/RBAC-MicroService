package org.shuzimali.userservice.service;

import org.shuzimali.userservice.entity.User;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService {

    /**
     * 用户注册
     */
    @Transactional
    User register(HttpServletRequest request, User user);

    /**
     * 用户登录
     */
    Map<String, String> login(String username, String password);

    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(HttpServletRequest request,Long userId, User user);

    /**
     * 重置密码
     */
    void resetPassword(HttpServletRequest request,Long userId, String newPassword);

    /**
     * 分页查询用户列表（根据角色权限过滤）
     */
    Map<String, Object> listUsersByRole(Integer page, Integer size, Long userId);

    /**
     * 检查用户是否有管理员权限
     */
    boolean hasAdminPermission(Long userId);
}