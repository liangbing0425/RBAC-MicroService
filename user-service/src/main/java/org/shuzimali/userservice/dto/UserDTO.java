package org.shuzimali.userservice.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String roleCode; // 用户角色编码
}
