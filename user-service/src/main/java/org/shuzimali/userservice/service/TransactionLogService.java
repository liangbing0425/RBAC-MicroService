package org.shuzimali.userservice.service;

import org.shuzimali.userservice.entity.TransactionLog;

public interface TransactionLogService {
    /**
     * 记录事务开始
     */
    void recordTransactionStart(String logId, String businessKey,
                                String businessType, String remark);

    /**
     * 标记事务成功
     */
    void markTransactionSuccess(String logId);

    /**
     * 标记事务失败
     */
    void markTransactionFailed(String logId);

    /**
     * 查询事务状态
     */
    String getTransactionStatus(String logId);
}