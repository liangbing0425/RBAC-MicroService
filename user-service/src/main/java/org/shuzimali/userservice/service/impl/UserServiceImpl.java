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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PermissionFeignClient permissionFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final JwtUtil jwtUtil;
    private final TransactionLogService transactionLogService;

    @Override
    @GlobalTransactional
    public User register(HttpServletRequest request, User user) {
        log.info("用户注册开始 | username={} | email={} | phone={}",
                user.getUsername(), user.getEmail(), user.getPhone());

        try {
            // 加密密码
            String encryptedPwd = encryptPassword(user.getPassword());
            user.setPassword(encryptedPwd);
            user.setGmtCreate(LocalDateTime.now());
            log.debug("密码加密完成 | encryptedPwd={}", encryptedPwd);

            // 分库分表写入用户数据
            boolean saveResult = save(user);
            log.info("用户数据保存 {} | userId={}", saveResult ? "成功" : "失败", user.getUserId());

            // RPC调用绑定默认角色
            log.debug("开始绑定默认角色 | userId={}", user.getUserId());
            permissionFeignClient.bindDefaultRole(user.getUserId());
            log.info("默认角色绑定完成 | userId={}", user.getUserId());

            // 发送事务消息并记录事务状态
            sendTransactionLogToMQ(request, "REGISTER", user.getUserId(), "用户注册成功");
            log.info("用户注册完成 | userId={}", user.getUserId());

            return user;
        } catch (Exception e) {
            log.error("用户注册失败 | username={} | error={}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map<String, String> login(String username, String password) {
        log.info("用户登录尝试 | username={}", username);

        try {
            // 根据username查询用户
            User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            log.debug("用户查询结果 | user={}", user != null ? "存在" : "不存在");

            if (user != null && user.getPassword().equals(encryptPassword(password))) {
                String token = jwtUtil.generateToken(user.getUserId());
                Map<String, String> result = new HashMap<>();
                result.put("token", token);
                result.put("type", "Bearer");

                log.info("用户登录成功 | userId={}", user.getUserId());
                return result;
            }

            log.warn("用户名或密码错误 | username={}", username);
            throw new RuntimeException("用户名或密码错误");
        } catch (Exception e) {
            log.error("用户登录异常 | username={} | error={}", username, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("查询用户信息 | userId={}", userId);
        User user = getById(userId);
        log.info("用户信息查询结果 | userId={} | exists={}", userId, user != null);
        return user;
    }

    @Override
    @GlobalTransactional
    public void updateUser(HttpServletRequest request, Long userId, User user) {
        log.info("更新用户信息开始 | userId={} | updateFields={}", userId, user);

        try {
            user.setUserId(userId);
            boolean updateResult = updateById(user);
            log.info("用户信息更新 {} | userId={}", updateResult ? "成功" : "失败", userId);

            sendTransactionLogToMQ(request, "UPDATE_USER", userId, "用户信息更新");
        } catch (Exception e) {
            log.error("用户信息更新失败 | userId={} | error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @GlobalTransactional
    public void resetPassword(HttpServletRequest request, Long userId, String newPassword) {
        log.info("密码重置开始 | userId={}", userId);

        try {
            String encryptedPwd = encryptPassword(newPassword);
            User user = new User();
            user.setUserId(userId);
            user.setPassword(encryptedPwd);

            boolean updateResult = updateById(user);
            log.info("密码重置 {} | userId={}", updateResult ? "成功" : "失败", userId);

            sendTransactionLogToMQ(request, "RESET_PASSWORD", userId, "密码重置");
        } catch (Exception e) {
            log.error("密码重置失败 | userId={} | error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> listUsersByRole(Integer page, Integer size, Long userId) {
        log.info("按角色分页查询用户 | page={} | size={} | requesterId={}", page, size, userId);

        try {
            String roleCode = permissionFeignClient.getUserRoleCode(userId);
            log.debug("获取用户角色成功 | userId={} | roleCode={}", userId, roleCode);

            Page<User> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

            if ("user".equals(roleCode)) {
                wrapper.eq(User::getUserId, userId);
                log.debug("普通用户查询模式 | 仅查询自身数据");
            } else if ("admin".equals(roleCode)) {
                wrapper.ne(User::getUserId, 1L); // 1为超级管理员ID
                log.debug("管理员查询模式 | 排除超级管理员");
            }

            Page<User> userPage = page(pageParam, wrapper);
            Map<String, Object> result = new HashMap<>();
            result.put("records", userPage.getRecords());
            result.put("total", userPage.getTotal());

            log.info("用户列表查询完成 | total={} | currentSize={}",
                    userPage.getTotal(), userPage.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("用户列表查询失败 | error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean hasAdminPermission(Long userId) {
        log.debug("检查管理员权限 | userId={}", userId);

        try {
            String roleCode = permissionFeignClient.getUserRoleCode(userId);
            boolean hasPermission = "admin".equals(roleCode) || "super_admin".equals(roleCode);

            log.info("管理员权限检查结果 | userId={} | hasPermission={}", userId, hasPermission);
            return hasPermission;
        } catch (Exception e) {
            log.error("权限检查异常 | userId={} | error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((password + "salt").getBytes());
    }

    private void sendTransactionLogToMQ(HttpServletRequest request, String action, Long userId, String detail) {
        String msgId = null;
        try {
            // 1. 构建日志事件
            Map<String, Object> logEvent = new HashMap<>();
            msgId = UUID.randomUUID().toString();
            logEvent.put("userId", userId);
            logEvent.put("action", action);
            logEvent.put("ip", request.getRemoteAddr());
            logEvent.put("detail", detail);
            logEvent.put("msgId", msgId);

            log.debug("准备发送事务消息 | action={} | userId={} | msgId={}", action, userId, msgId);

            // 2. 记录事务开始状态
            transactionLogService.recordTransactionStart(
                    msgId,
                    userId.toString(),
                    action,
                    "用户服务事务消息发送"
            );
            log.debug("事务日志记录完成 | msgId={}", msgId);

            // 3. 获取Seata全局事务ID
            String globalTransactionId = RootContext.getXID();
            if (globalTransactionId == null) {
                log.error("未获取到Seata全局事务ID | msgId={}", msgId);
                throw new RuntimeException("未获取到Seata全局事务ID，无法发送事务消息");
            }
            log.debug("获取到Seata全局事务ID | xid={}", globalTransactionId);

            // 4. 发送事务消息
            Message<Map<String, Object>> message = MessageBuilder.withPayload(logEvent)
                    .setHeader(RocketMQHeaders.KEYS, msgId)
                    .setHeader(RocketMQHeaders.TRANSACTION_ID, globalTransactionId)
                    .build();

            rocketMQTemplate.sendMessageInTransaction(
                    "log-topic",
                    message,
                    logEvent
            );
            log.info("事务消息发送成功 | msgId={} | action={}", msgId, action);
        } catch (Exception e) {
            log.error("事务消息发送失败 | msgId={} | action={} | error={}",
                    msgId, action, e.getMessage(), e);
            transactionLogService.markTransactionFailed(msgId);
            throw new RuntimeException("操作日志发送失败", e);
        }
    }
}