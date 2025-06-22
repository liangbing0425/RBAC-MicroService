package org.shuzimali.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.core.context.RootContext;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.shuzimali.userservice.entity.User;
import org.shuzimali.userservice.entity.TransactionLog;
import org.shuzimali.userservice.mapper.UserMapper;
import org.shuzimali.userservice.rpc.PermissionFeignClient;
import org.shuzimali.userservice.service.TransactionLogService;
import org.shuzimali.userservice.service.UserService;
import org.shuzimali.userservice.util.JwtUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PermissionFeignClient permissionFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final JwtUtil jwtUtil;
    private final TransactionLogService transactionLogService; // 注入事务日志服务

    @Override
    @GlobalTransactional
    public User register(HttpServletRequest request, User user) {
        // 加密密码
        user.setPassword(encryptPassword(user.getPassword()));
        user.setGmtCreate(LocalDateTime.now());

        // 分库分表写入用户数据
        save(user);

        // RPC调用绑定默认角色
        permissionFeignClient.bindDefaultRole(user.getUserId());

        // 发送事务消息并记录事务状态
        sendTransactionLogToMQ(request, "REGISTER", user.getUserId(), "用户注册成功");

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
    @GlobalTransactional
    public void updateUser(HttpServletRequest request, Long userId, User user) {
        user.setUserId(userId);
        updateById(user);
        sendTransactionLogToMQ(request, "UPDATE_USER", userId, "用户信息更新");
    }

    @Override
    @GlobalTransactional
    public void resetPassword(HttpServletRequest request, Long userId, String newPassword) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(encryptPassword(newPassword));
        updateById(user);
        sendTransactionLogToMQ(request, "RESET_PASSWORD", userId, "密码重置");
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

    /**
     * 发送事务消息并记录事务状态
     */
    // ai辅助生成
    private void sendTransactionLogToMQ(HttpServletRequest request, String action, Long userId, String detail) {
        String msgId = null;
        try {
            // 1. 构建日志事件（含唯一ID）
            Map<String, Object> logEvent = new HashMap<>();
            msgId = UUID.randomUUID().toString();
            logEvent.put("userId", userId);
            logEvent.put("action", action);
            logEvent.put("ip", request.getRemoteAddr());
            logEvent.put("detail", detail);
            logEvent.put("msgId", msgId); // 唯一消息ID用于幂等性

            // 2. 记录事务开始状态（PENDING）
            transactionLogService.recordTransactionStart(
                    msgId,
                    userId.toString(),
                    action,
                    "用户服务事务消息发送"
            );

            // 3. 获取Seata全局事务ID
            String globalTransactionId = RootContext.getXID();
            if (globalTransactionId == null) {
                throw new RuntimeException("未获取到Seata全局事务ID，无法发送事务消息");
            }

            // 4. 发送事务消息（绑定Seata全局事务ID）
            Message<Map<String, Object>> message = MessageBuilder.withPayload(logEvent)
                    .setHeader(RocketMQHeaders.KEYS, msgId)
                    .setHeader(RocketMQHeaders.TRANSACTION_ID, globalTransactionId)
                    .build();

            // 发送半消息并关联本地事务状态
            String finalMsgId = msgId;
            rocketMQTemplate.sendMessageInTransaction(
                    "log-topic", // 主题
                    message,     // 消息内容
                    logEvent    // 事务上下文
            );

        } catch (Exception e) {
            log.error("事务消息发送失败", e);
            // 发送失败时标记事务为回滚
            transactionLogService.markTransactionFailed(msgId);
            throw new RuntimeException("操作日志发送失败", e);
        }
    }
}