package org.shuzimali.loggingservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    private Long logId;
    private String msgId;
    private Long userId;
    private String action;
    private String ip;
    private String detail;
    private LocalDateTime gmtCreate;
}
