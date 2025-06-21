package org.shuzimali.permissionservice.service;

import org.shuzimali.permissionservice.entity.Role;
import org.shuzimali.permissionservice.entity.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PermissionService {
    void bindDefaultRole(Long userId);
    String getUserRoleCode(Long userId);
    void upgradeToAdmin(Long userId);
    void downgradeToUser(Long userId);
    List<Role> getAllRoles();
    UserRole getUserRole(Long userId);
}
