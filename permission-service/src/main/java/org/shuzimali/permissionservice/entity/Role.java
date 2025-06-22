package org.shuzimali.permissionservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role")
public class Role {
    private Integer roleId;
    private String roleCode;
}
