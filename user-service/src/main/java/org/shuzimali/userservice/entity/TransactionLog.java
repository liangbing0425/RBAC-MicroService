package org.shuzimali.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("transaction_log")
public class TransactionLog {

    @TableId(value = "log_id", type = IdType.ASSIGN_UUID)
    private String logId;         // 事务ID/消息ID

    @TableField("business_key")
    private String businessKey;   // 业务主键

    @TableField("business_type")
    private String businessType;  // 业务类型

    @TableField("transaction_status")
    private String transactionStatus; // 事务状态：PENDING/COMMITTED/ROLLBACKED

    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    @TableField("gmt_modified")
    private LocalDateTime gmtModified;

    @TableField("remark")
    private String remark;        // 备注信息
}