package org.shuzimali.userservice.listener;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.shuzimali.userservice.service.TransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

import java.util.Map;

// ai辅助生成
@RocketMQTransactionListener
public class LogTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private TransactionLogService transactionLogService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        Map<String, Object> logEvent = (Map<String, Object>) arg; // 从arg获取事务上下文
        String msgId = (String) logEvent.get("msgId");

        try {
            // 执行本地事务
            transactionLogService.markTransactionSuccess(msgId);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 从消息头或Body中提取msgId
        String msgId = msg.getHeaders().get("msgId", String.class);

        // 根据msgId查询事务日志状态
        if ("COMMITTED".equals(transactionLogService.getTransactionStatus(msgId)) ) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}