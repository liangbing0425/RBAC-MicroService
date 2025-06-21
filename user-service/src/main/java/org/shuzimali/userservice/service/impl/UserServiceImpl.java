package org.shuzimali.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuzimali.userservice.entity.User;
import org.shuzimali.userservice.mapper.UserMapper;
import org.shuzimali.userservice.rpc.PermissionFeignClient;
import org.shuzimali.userservice.service.UserService;
import org.shuzimali.userservice.util.JwtUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserMapper getBaseMapper() {
        return super.getBaseMapper();
    }

    private final PermissionFeignClient permissionFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final JwtUtil jwtUtil;

    @Override
    @GlobalTransactional
    public User register(HttpServletRequest request,User user) {
        // 加密密码
        user.setPassword(encryptPassword(user.getPassword()));
        user.setGmtCreate(LocalDateTime.now());

        // 分库分表写入用户数据
        save(user);

        // RPC调用绑定默认角色
        permissionFeignClient.bindDefaultRole(user.getUserId());

        // 发送日志消息至MQ
        sendLogToMQ(request,"REGISTER", user.getUserId(), "用户注册成功");

        return user;
    }

    @Override
    public Map<String, String> login(String username, String password) {
        // 根据username查询用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user != null && user.getPassword().equals(encryptPassword(password))) {
            String token = jwtUtil.generateToken(user.getUserId());
            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            result.put("type", "Bearer");
            return result;
        }

        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }

    @Override
    public void updateUser(HttpServletRequest request,Long userId, User user) {
        user.setUserId(userId);
        updateById(user);
        sendLogToMQ(request,"UPDATE_USER", userId, "用户信息更新");
    }

    @Override
    public void resetPassword(HttpServletRequest request,Long userId, String newPassword) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(encryptPassword(newPassword));
        updateById(user);
        sendLogToMQ(request,"RESET_PASSWORD", userId, "密码重置");
    }

    @Override
    public Map<String, Object> listUsersByRole(Integer page, Integer size, Long userId) {
        String roleCode = permissionFeignClient.getUserRoleCode(userId);
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if ("user".equals(roleCode)) {
            wrapper.eq(User::getUserId, userId);
        } else if ("admin".equals(roleCode)) {
            wrapper.ne(User::getUserId, 1L); // 1为超级管理员ID
        }

        Page<User> userPage = page(pageParam, wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getRecords());
        result.put("total", userPage.getTotal());
        return result;
    }

    @Override
    public boolean hasAdminPermission(Long userId) {
        String roleCode = permissionFeignClient.getUserRoleCode(userId);
        return "admin".equals(roleCode) || "super_admin".equals(roleCode);
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((password + "salt").getBytes());
    }

    private void sendLogToMQ(HttpServletRequest request,String action, Long userId, String detail) {
        // 构建日志事件并发送到MQ
        Map<String, Object> logEvent = new HashMap<>();
        logEvent.put("userId", userId);
        logEvent.put("action", action);
        logEvent.put("ip", request.getRemoteAddr());
        logEvent.put("detail", detail);

        rocketMQTemplate.convertAndSend("log-topic", logEvent);
    }
}
