-- 按user_id模2分片，创建2个物理表
CREATE TABLE users_0 LIKE users;
CREATE TABLE users_1 LIKE users;

/*
ShardingSphere配置示例：
# 数据节点配置
spring.shardingsphere.sharding.tables.users.actual-data-nodes=ds0.users_$->{0..1}

# 分表策略
spring.shardingsphere.sharding.tables.users.table-strategy.standard.sharding-column=user_id
spring.shardingsphere.sharding.tables.users.table-strategy.standard.precise-algorithm-class-name=org.shuzimali.userservice.util.ShardingKeyGenerator

# 分布式主键生成器
spring.shardingsphere.sharding.tables.users.key-generator.column=user_id
spring.shardingsphere.sharding.tables.users.key-generator.type=SNOWFLAKE
*/
