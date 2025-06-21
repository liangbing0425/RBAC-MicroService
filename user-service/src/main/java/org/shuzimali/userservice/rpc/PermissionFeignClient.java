package org.shuzimali.userservice.rpc;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "permission-service", path = "/api/permission")
public interface PermissionFeignClient {

    @PostMapping("/bindDefaultRole")
    void bindDefaultRole(@RequestParam("userId") Long userId);

    @GetMapping("/getUserRoleCode/{userId}")
    String getUserRoleCode(@PathVariable("userId") Long userId);

    @PostMapping("/upgradeToAdmin")
    void upgradeToAdmin(@RequestParam("userId") Long userId);

    @PostMapping("/downgradeToUser")
    void downgradeToUser(@RequestParam("userId") Long userId);
}
