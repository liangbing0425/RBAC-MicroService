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
            // 解析消息体（JSON格式）
            Map<String, Object> logMap = JSON.parseObject(message, Map.class);

            // 转换为操作日志实体
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(Long.parseLong(logMap.get("userId").toString()));
            operationLog.setAction(logMap.get("action").toString());
            operationLog.setIp(logMap.get("ip").toString());
            operationLog.setDetail(logMap.get("detail").toString());

            // 保存日志到数据库
            operationLogService.saveLog(operationLog);

            log.info("消费日志消息成功: {}", message);
        } catch (Exception e) {
            log.error("消费日志消息失败: {}", message, e);
            // 可在此添加重试逻辑或死信队列处理
        }
    }
}
