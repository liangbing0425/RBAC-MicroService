package org.shuzimali.loggingservice.controller;


import org.shuzimali.loggingservice.entity.OperationLog;
import org.shuzimali.loggingservice.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LoggingController {

    private final OperationLogService operationLogService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<OperationLog>> getUserLogs(@PathVariable Long userId) {
        List<OperationLog> logs = operationLogService.getLogsByUserId(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<OperationLog>> getLogsByAction(@PathVariable String action) {
        List<OperationLog> logs = operationLogService.getLogsByAction(action);
        return ResponseEntity.ok(logs);
    }

    @GetMapping
    public ResponseEntity<List<OperationLog>> getAllLogs() {
        List<OperationLog> logs = operationLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}