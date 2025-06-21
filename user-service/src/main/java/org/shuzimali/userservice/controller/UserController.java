package org.shuzimali.userservice.controller;

import org.shuzimali.userservice.entity.User;
import org.shuzimali.userservice.service.UserService;
import org.shuzimali.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<User> register(HttpServletRequest request, @RequestBody User user) {
        User registeredUser = userService.register(request,user);
        return ResponseEntity.created(null).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        Map<String, String> token = userService.login(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId, HttpServletRequest request) {
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

    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable Long userId, @RequestBody User user,
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

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, Object> params,
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

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        Long userId = jwtUtil.extractUserId(token);

        Map<String, Object> result = userService.listUsersByRole(page, size, userId);
        return ResponseEntity.ok(result);
    }
}