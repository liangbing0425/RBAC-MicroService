package org.shuzimali.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzimali.userservice.entity.TransactionLog;
import org.shuzimali.userservice.mapper.TransactionLogMapper;
import org.shuzimali.userservice.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogMapper transactionLogMapper;

    @Override
    @Transactional
    public void recordTransactionStart(String logId, String businessKey,
                                       String businessType, String remark) {
        TransactionLog log = new TransactionLog();
        log.setLogId(logId);
        log.setBusinessKey(businessKey);
        log.setBusinessType(businessType);
        log.setTransactionStatus("PENDING");
        log.setRemark(remark);
        transactionLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void markTransactionSuccess(String logId) {
        TransactionLog log = new TransactionLog();
        log.setLogId(logId);
        log.setTransactionStatus("COMMITTED");
        transactionLogMapper.updateById(log);
    }

    @Override
    @Transactional
    public void markTransactionFailed(String logId) {
        TransactionLog log = new TransactionLog();
        log.setLogId(logId);
        log.setTransactionStatus("ROLLBACKED");
        transactionLogMapper.updateById(log);
    }

    @Override
    public String getTransactionStatus(String logId) {
        LambdaQueryWrapper<TransactionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionLog::getLogId, logId);
        TransactionLog log = transactionLogMapper.selectOne(wrapper);
        return log != null ? log.getTransactionStatus() : "UNKNOWN";
    }
}