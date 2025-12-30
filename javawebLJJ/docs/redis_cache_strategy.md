# Redis缓存策略设计

## 缓存策略概述

为了提高校园活动报名系统的性能和响应速度，采用Redis作为缓存层，实现热点数据的快速访问，减少数据库压力。

## 缓存层级设计

### 1. 一级缓存 (本地缓存)
- 使用Spring Cache + Caffeine实现
- 缓存高频访问的小数据量信息

### 2. 二级缓存 (Redis缓存)
- 使用Redis集群实现分布式缓存
- 缓存热点数据和会话信息

## 缓存策略

### 1. 活动信息缓存

#### 缓存键设计
```
activity:detail:{activityId} - 活动详细信息，TTL: 30分钟
activity:list:{status}:{page}:{size} - 活动列表，TTL: 10分钟
activity:hot - 热门活动列表，TTL: 15分钟
activity:recommended - 推荐活动列表，TTL: 15分钟
activity:registration-open - 报名开放活动列表，TTL: 5分钟
```

#### 缓存策略
- 活动详细信息缓存30分钟
- 活动列表根据状态和分页参数缓存10分钟
- 热门和推荐活动列表缓存15分钟
- 报名开放活动列表缓存5分钟（时效性要求高）

### 2. 用户信息缓存

#### 缓存键设计
```
user:detail:{userId} - 用户详细信息，TTL: 1小时
user:profile:{userId} - 用户公开信息，TTL: 2小时
user:username:{username} - 用户名对应的用户ID，TTL: 1小时
```

#### 缓存策略
- 用户详细信息缓存1小时
- 用户公开信息缓存2小时
- 用户名映射缓存1小时

### 3. 报名信息缓存

#### 缓存键设计
```
registration:user:{userId}:activity:{activityId} - 用户活动报名状态，TTL: 30分钟
registration:count:activity:{activityId} - 活动报名人数统计，TTL: 5分钟
registration:list:activity:{activityId}:{page}:{size} - 活动报名列表，TTL: 10分钟
```

#### 缓存策略
- 用户活动报名状态缓存30分钟
- 活动报名人数统计缓存5分钟
- 活动报名列表缓存10分钟

### 4. 会话缓存

#### 缓存键设计
```
session:{sessionId} - 用户会话信息，TTL: 24小时
token:user:{userId} - 用户Token映射，TTL: 24小时
```

## 缓存更新策略

### 1. 写穿透策略 (Write-Through)
- 数据写入时同时更新缓存和数据库
- 适用于用户信息、活动信息等

### 2. 写回策略 (Write-Behind)
- 数据写入时先更新缓存，异步更新数据库
- 适用于活动报名人数统计等

### 3. 缓存失效策略
- 数据更新时主动失效相关缓存
- 使用缓存标签机制批量失效

## Redis集群配置

### 1. 集群架构
- 3主3从Redis集群，保证高可用
- 每个主节点负责一部分数据分片

### 2. 配置参数
```yaml
# 连接池配置
redis:
  lettuce:
    pool:
      max-active: 200    # 最大连接数
      max-idle: 50       # 最大空闲连接
      min-idle: 10       # 最小空闲连接
      max-wait: 10000ms  # 最大等待时间

# 超时配置
  timeout: 10000ms
```

## 缓存预热策略

### 1. 系统启动时预热
- 预热热门活动信息
- 预热系统配置信息

### 2. 定时预热
- 定时更新热门活动列表
- 定时更新推荐活动列表

## 缓存降级策略

### 1. 缓存失效降级
- 当Redis不可用时，直接访问数据库
- 记录降级日志，便于问题排查

### 2. 缓存容量降级
- 当缓存容量不足时，采用LRU策略淘汰旧数据
- 监控缓存命中率，及时扩容

## 缓存监控指标

### 1. 性能指标
- 缓存命中率
- 缓存响应时间
- 缓存QPS

### 2. 容量指标
- 缓存使用率
- 内存使用情况
- 连接数

## 代码实现示例

### 1. Spring Cache配置
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
                
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 2. 缓存注解使用
```java
@Service
public class ActivityService {
    
    @Cacheable(value = "activity", key = "#id", unless = "#result == null")
    public Activity getActivityById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = "activity", key = "#activity.id")
    public Activity updateActivity(Long id, Activity activity) {
        return activityRepository.save(activity);
    }
}
```

## 缓存安全措施

### 1. 访问控制
- Redis访问认证
- 网络访问控制

### 2. 数据安全
- 敏感数据加密存储
- 定期数据备份

## 性能优化建议

### 1. 数据结构优化
- 合理选择Redis数据结构
- 使用Pipeline批量操作

### 2. 序列化优化
- 使用高效的序列化方式
- 压缩大数据对象

### 3. 连接优化
- 合理配置连接池参数
- 使用连接复用机制

## 容灾方案

### 1. 缓存雪崩防护
- 缓存过期时间随机化
- 多级缓存架构

### 2. 缓存穿透防护
- 布隆过滤器
- 空值缓存

### 3. 缓存击穿防护
- 分布式锁
- 热点数据永不过期

通过以上缓存策略的设计和实现，可以显著提升校园活动报名系统的性能和用户体验。