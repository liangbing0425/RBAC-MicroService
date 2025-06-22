package org.shuzimali.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.shuzimali.userservice.entity.TransactionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLog> {
}