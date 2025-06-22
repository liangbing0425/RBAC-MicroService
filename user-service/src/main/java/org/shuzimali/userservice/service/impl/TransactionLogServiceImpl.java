package org.shuzimali.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzimali.userservice.entity.TransactionLog;
import org.shuzimali.userservice.mapper.TransactionLogMapper;
import org.shuzimali.userservice.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogMapper transactionLogMapper;

    @Override
    @Transactional
    public void recordTransactionStart(String logId, String businessKey,
                                       String businessType, String remark) {
        log.info("开始记录事务日志 | logId={} | businessType={} | businessKey={}",
                logId, businessType, businessKey);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setLogId(logId);
        transactionLog.setBusinessKey(businessKey);
        transactionLog.setBusinessType(businessType);
        transactionLog.setTransactionStatus("PENDING");
        transactionLog.setRemark(remark);
        transactionLogMapper.insert(transactionLog);
        log.info("事务日志记录完成 | logId={} | status=PENDING", logId);
    }

    @Override
    @Transactional
    public void markTransactionSuccess(String logId) {
        log.info("标记事务成功 | logId={}", logId);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setLogId(logId);
        transactionLog.setTransactionStatus("COMMITTED");
        transactionLogMapper.updateById(transactionLog);
        log.info("事务状态更新为已提交 | logId={}", logId);
    }

    @Override
    @Transactional
    public void markTransactionFailed(String logId) {
        log.warn("标记事务失败 | logId={}", logId);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setLogId(logId);
        transactionLog.setTransactionStatus("ROLLBACKED");
        transactionLogMapper.updateById(transactionLog);
        log.warn("事务状态更新为已回滚 | logId={}", logId);
    }

    @Override
    public String getTransactionStatus(String logId) {
        log.debug("查询事务状态 | logId={}", logId);
        LambdaQueryWrapper<TransactionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionLog::getLogId, logId);
        TransactionLog transactionLog = transactionLogMapper.selectOne(wrapper);
        String status = transactionLog != null ? transactionLog.getTransactionStatus() : "UNKNOWN";
        log.debug("事务状态查询结果 | logId={} | status={}", logId, status);
        return status;
    }
}