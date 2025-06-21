package org.shuzimali.loggingservice.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shuzimali.loggingservice.entity.OperationLog;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    // 继承BaseMapper获取基础CRUD操作
}
