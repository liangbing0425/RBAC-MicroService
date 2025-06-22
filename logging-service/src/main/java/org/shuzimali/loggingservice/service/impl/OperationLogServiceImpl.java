package org.shuzimali.loggingservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shuzimali.loggingservice.entity.OperationLog;
import org.shuzimali.loggingservice.mapper.OperationLogMapper;
import org.shuzimali.loggingservice.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public OperationLog getByMsgId(String msgId) {
        log.debug("开始查询操作日志 | msgId={}", msgId);
        OperationLog logEntity = getOne(
                new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getMsgId, msgId)
        );
        log.debug("操作日志查询结果 | msgId={} | exists={}", msgId, logEntity != null);
        return logEntity;
    }

    @Override
    public void saveLog(OperationLog logEntity) {
        log.info("开始保存操作日志 | userId={} | action={}", logEntity.getUserId(), logEntity.getAction());
        logEntity.setGmtCreate(LocalDateTime.now());
        boolean saveResult = save(logEntity);
        if (saveResult) {
            log.info("操作日志保存成功 | logId={}", logEntity.getLogId());
        } else {
            log.error("操作日志保存失败 | userId={}", logEntity.getUserId());
        }
    }

    @Override
    public List<OperationLog> getAllLogs() {
        log.debug("开始查询所有操作日志");
        List<OperationLog> logs = list();
        log.debug("操作日志查询完成 | count={}", logs.size());
        return logs;
    }

    @Override
    public List<OperationLog> getLogsByUserId(Long userId) {
        log.debug("开始按用户ID查询操作日志 | userId={}", userId);
        List<OperationLog> logs = list(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getUserId, userId));
        log.debug("用户操作日志查询完成 | userId={} | count={}", userId, logs.size());
        return logs;
    }

    @Override
    public List<OperationLog> getLogsByAction(String action) {
        log.debug("开始按操作类型查询日志 | action={}", action);
        List<OperationLog> logs = list(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getAction, action));
        log.debug("操作类型日志查询完成 | action={} | count={}", action, logs.size());
        return logs;
    }
}