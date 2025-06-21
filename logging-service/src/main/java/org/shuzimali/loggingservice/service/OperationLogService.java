package org.shuzimali.loggingservice.service;



import org.shuzimali.loggingservice.entity.OperationLog;

import java.util.List;

public interface OperationLogService {
    void saveLog(OperationLog log);
    List<OperationLog> getAllLogs();
    List<OperationLog> getLogsByUserId(Long userId);
    List<OperationLog> getLogsByAction(String action);
}