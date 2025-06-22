package org.shuzimali.loggingservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.shuzimali.loggingservice.entity.OperationLog;
import org.shuzimali.loggingservice.mapper.OperationLogMapper;
import org.shuzimali.loggingservice.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public OperationLog getByMsgId(String msgId) {
        return getOne(
                new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getMsgId, msgId)
        );
    }
    @Override
    public void saveLog(OperationLog log) {
        log.setGmtCreate(LocalDateTime.now());
        save(log);
    }

    @Override
    public List<OperationLog> getAllLogs() {
        return list();
    }

    @Override
    public List<OperationLog> getLogsByUserId(Long userId) {
        return list(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getUserId, userId));
    }

    @Override
    public List<OperationLog> getLogsByAction(String action) {
        return list(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getAction, action));
    }
}
