package org.shuzimali.permissionservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRole {
    private Long id;
    private Long userId;
    private Integer roleId;
}