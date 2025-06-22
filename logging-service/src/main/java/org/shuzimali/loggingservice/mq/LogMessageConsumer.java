package org.shuzimali.loggingservice.mq;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.shuzimali.loggingservice.entity.OperationLog;
import org.shuzimali.loggingservice.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RocketMQMessageListener(topic = "log-topic", consumerGroup = "logging-service-group")
@Slf4j
@RequiredArgsConstructor
public class LogMessageConsumer implements RocketMQListener<String> {

    private final OperationLogService operationLogService;

    @Override
    public void onMessage(String message) {
        try {
            // 1. 解析消息并提取唯一ID
            Map<String, Object> logMap = JSON.parseObject(message, Map.class);
            String msgId = logMap.get("msgId").toString();

            // 2. 幂等性校验（通过数据库唯一索引）
            OperationLog existLog = operationLogService.getByMsgId(msgId);
            if (existLog != null) {
                log.warn("重复消费消息，msgId: {}", msgId);
                return;
            }

            // 3. 转换为操作日志实体并保存
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(Long.parseLong(logMap.get("userId").toString()));
            operationLog.setAction(logMap.get("action").toString());
            operationLog.setIp(logMap.get("ip").toString());
            operationLog.setDetail(logMap.get("detail").toString());
            operationLog.setMsgId(msgId);

            operationLogService.saveLog(operationLog);
            log.info("消费日志消息成功，msgId: {}", msgId);

        } catch (Exception e) {
            log.error("消费日志消息失败，message: {}, 异常: {}", message, e);
            // 抛出异常触发RocketMQ自动重试
            throw new RuntimeException("日志消息消费失败", e);
        }
    }
}