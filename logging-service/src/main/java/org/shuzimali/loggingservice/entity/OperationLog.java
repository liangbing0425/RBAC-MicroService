package org.shuzimali.loggingservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long logId;
    private Long userId;
    private String action;
    private String ip;
    private String detail;
    private LocalDateTime gmtCreate;
}
