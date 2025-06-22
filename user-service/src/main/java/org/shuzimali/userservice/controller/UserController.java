package org.shuzimali.userservice.controller;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shuzimali.userservice.entity.User;
import org.shuzimali.userservice.service.UserService;
import org.shuzimali.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Tag(name = "UserControllerAPI", description = "用户控制器接口"
        , externalDocs = @ExternalDocumentation(description = "这是一个接口文档介绍"))
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public ResponseEntity<User> register(HttpServletRequest request,
                                        @Parameter(description = "用户信息", required = true)
                                        @RequestBody User user) {
        User registeredUser = userService.register(request,user);
        return ResponseEntity.created(null).body(registeredUser);
    }

    @Operation(summary = "用户登录", description = "用户登录获取JWT token")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Parameter(description = "用户登录信息", required = true)
            @RequestBody User user) {
        Map<String, String> token = userService.login(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId, 
            HttpServletRequest request) {
        // 从JWT获取当前用户ID并进行权限校验
        String token = request.getHeader("Authorization").split(" ")[1];
        Long currentUserId = jwtUtil.extractUserId(token);

        if (!currentUserId.equals(userId) &&
                !userService.hasAdminPermission(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "更新用户信息", description = "更新指定用户的详细信息")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId, 
            @Parameter(description = "更新后的用户信息", required = true)
            @RequestBody User user,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        Long currentUserId = jwtUtil.extractUserId(token);

        if (!currentUserId.equals(userId) &&
                !userService.hasAdminPermission(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        userService.updateUser(request,userId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "重置密码参数(userId, newPassword)", required = true)
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        Long currentUserId = jwtUtil.extractUserId(token);
        Long targetUserId = Long.parseLong(params.get("userId").toString());
        String newPassword = params.get("newPassword").toString();

        if (!currentUserId.equals(targetUserId) &&
                !userService.hasAdminPermission(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        userService.resetPassword(request,targetUserId, newPassword);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取用户列表", description = "根据用户角色分页查询用户列表")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") 
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        Long userId = jwtUtil.extractUserId(token);

        Map<String, Object> result = userService.listUsersByRole(page, size, userId);
        return ResponseEntity.ok(result);
    }
}