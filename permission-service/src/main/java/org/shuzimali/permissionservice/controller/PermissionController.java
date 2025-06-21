package org.shuzimali.permissionservice.controller;


import lombok.RequiredArgsConstructor;
import org.shuzimali.permissionservice.entity.Role;
import org.shuzimali.permissionservice.entity.UserRole;
import org.shuzimali.permissionservice.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/bindDefaultRole")
    public ResponseEntity<Void> bindDefaultRole(@RequestParam Long userId) {
        permissionService.bindDefaultRole(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getUserRoleCode/{userId}")
    public ResponseEntity<String> getUserRoleCode(@PathVariable Long userId) {
        String roleCode = permissionService.getUserRoleCode(userId);
        return ResponseEntity.ok(roleCode);
    }

    @PostMapping("/upgradeToAdmin")
    public ResponseEntity<Void> upgradeToAdmin(@RequestParam Long userId) {
        permissionService.upgradeToAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/downgradeToUser")
    public ResponseEntity<Void> downgradeToUser(@RequestParam Long userId) {
        permissionService.downgradeToUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = permissionService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/userRole/{userId}")
    public ResponseEntity<UserRole> getUserRole(@PathVariable Long userId) {
        UserRole userRole = permissionService.getUserRole(userId);
        return ResponseEntity.ok(userRole);
    }
}
