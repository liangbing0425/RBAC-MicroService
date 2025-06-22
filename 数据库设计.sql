-- 1. 角色表
CREATE TABLE roles (
  role_id INT PRIMARY KEY,        -- 1:超级管理员 2:普通用户 3:管理员
  role_code VARCHAR(20) UNIQUE   -- 角色编码：super_admin/user/admin
);

-- 2. 用户角色关系表
CREATE TABLE user_roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  role_id INT,
  UNIQUE KEY uk_user_role (user_id),  -- 每个用户仅绑定一个角色
  FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- 3. 用户表（水平分表基础结构）
CREATE TABLE users_1 (
  user_id BIGINT PRIMARY KEY,
  username VARCHAR(50),
  password VARCHAR(255),
  email VARCHAR(100),
  phone VARCHAR(20),
  gmt_create TIMESTAMP
);

-- 4. 操作日志表
CREATE TABLE operation_logs (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  action VARCHAR(50),       -- 操作类型标识，如"update_user"
  ip VARCHAR(15),
  detail TEXT               -- 记录修改内容（如JSON格式的字段变更）
);

-- 5. 初始化角色数据
INSERT INTO roles (role_id, role_code) VALUES (1, 'super_admin');
INSERT INTO roles (role_id, role_code) VALUES (2, 'user');
INSERT INTO roles (role_id, role_code) VALUES (3, 'admin');


-- 在用户服务数据库中创建事务日志表
CREATE TABLE transaction_log (
                                 log_id VARCHAR(50) PRIMARY KEY,        -- 事务ID/消息ID（UUID）
                                 business_key VARCHAR(100) NOT NULL,    -- 业务主键（如用户ID）
                                 business_type VARCHAR(50) NOT NULL,    -- 业务类型（REGISTER/UPDATE_USER等）
                                 transaction_status VARCHAR(20) NOT NULL, -- 事务状态：PENDING/COMMITTED/ROLLBACKED
                                 gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 remark VARCHAR(255),                    -- 备注信息
                                 INDEX idx_business_key (business_key),  -- 业务主键索引
                                 INDEX idx_transaction_status (transaction_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轻量级事务日志表';

-- 在operation_logs表中添加msg_id字段及唯一索引
ALTER TABLE operation_logs
    ADD COLUMN msg_id VARCHAR(50) COMMENT '消息唯一ID',
    ADD UNIQUE KEY uk_msg_id (msg_id);