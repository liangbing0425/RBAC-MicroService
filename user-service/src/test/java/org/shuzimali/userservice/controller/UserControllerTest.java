package org.shuzimali.userservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shuzimali.userservice.entity.User;
import org.shuzimali.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private HttpServletRequest request; // 直接模拟接口
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void registerNewUserSuccessfully() {
        // 1. 准备测试数据
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
        testUser.setGmtCreate(LocalDateTime.now());

        // 2. 模拟Service行为
        when(userService.register(any(HttpServletRequest.class), any(User.class))).thenReturn(testUser);


        // 3. 调用Controller方法
        ResponseEntity<User> response = userController.register(request, testUser);

        // 4. 验证响应
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUser.getUsername(), response.getBody().getUsername());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
    }
}