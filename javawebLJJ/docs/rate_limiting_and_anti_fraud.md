# 限流和防刷机制设计

## 限流防刷策略概述

在校园活动报名系统中，为防止恶意刷单和保护系统资源，需要实现多层次的限流和防刷机制。这些机制包括API限流、用户行为分析、验证码验证等。

## 限流策略设计

### 1. 限流层级

#### 1.1 网关层限流
- 在API网关层面实现全局限流
- 防止大量请求直接冲击后端服务
- 使用令牌桶算法实现平滑限流

#### 1.2 服务层限流
- 在各个微服务内部实现精细化限流
- 针对不同接口设置不同的限流策略
- 基于用户、IP、活动等维度限流

#### 1.3 接口层限流
- 针对特定接口实现精确限流
- 如报名接口、查询接口等

### 2. 限流算法

#### 2.1 令牌桶算法
- 平滑处理请求，允许突发流量
- 可配置令牌生成速率和桶容量
- 适用于保护数据库等后端资源

#### 2.2 滑动窗口算法
- 精确控制单位时间内的请求数量
- 适用于精确的频率控制

#### 2.3 漏桶算法
- 以固定速率处理请求
- 适用于控制输出速率

## 限流维度

### 1. IP维度限流
- 限制单个IP的请求频率
- 防止来自同一IP的恶意请求

### 2. 用户维度限流
- 限制单个用户的请求频率
- 防止恶意用户刷单

### 3. 接口维度限流
- 不同接口设置不同的限流阈值
- 保护核心业务接口

### 4. 活动维度限流
- 针对热门活动设置专门的限流策略
- 防止单一活动占用过多资源

## 限流配置策略

### 1. 全局限流配置
```
默认限流: 100次/分钟/IP
登录用户: 200次/分钟/用户
接口限流: 
- 报名接口: 5次/分钟/用户
- 查询接口: 100次/分钟/用户
- 搜索接口: 30次/分钟/用户
```

### 2. 动态限流配置
- 根据系统负载动态调整限流阈值
- 支持运营人员手动调整

## 防刷机制设计

### 1. 验证码机制
- 在高并发场景下启用验证码
- 图形验证码、滑块验证码、短信验证码
- 人机验证，防止自动化脚本

### 2. 行为分析防刷
- 分析用户行为模式
- 识别异常操作行为
- 基于机器学习的异常检测

### 3. 设备指纹防刷
- 收集设备信息生成设备指纹
- 限制同一设备的请求频率
- 防止多账号操作

## 技术实现方案

### 1. 基于Redis的分布式限流

#### 1.1 Lua脚本实现令牌桶
```lua
-- 令牌桶算法Lua脚本
local key = KEYS[1]
local rate = tonumber(ARGV[1])  -- 令牌生成速率
local capacity = tonumber(ARGV[2])  -- 桶容量
local requested = tonumber(ARGV[3])  -- 请求令牌数
local now = tonumber(ARGV[4])  -- 当前时间戳

local state = redis.call('HMGET', key, 'last_time', 'tokens')
local last_time = tonumber(state[1])
local tokens = tonumber(state[2])

if not last_time then
    last_time = now
    tokens = capacity
end

local new_tokens = math.min(capacity, tokens + (now - last_time) * rate)
if new_tokens >= requested then
    tokens = new_tokens - requested
    redis.call('HMSET', key, 'last_time', now, 'tokens', tokens)
    return 1  -- 允许请求
else
    redis.call('HMSET', key, 'last_time', last_time, 'tokens', new_tokens)
    return 0  -- 拒绝请求
end
```

#### 1.2 滑动窗口限流
```java
@Component
public class SlidingWindowRateLimiter {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String key, int limit, int windowSizeSeconds) {
        String luaScript = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local now = tonumber(ARGV[2]) " +
            "local window = tonumber(ARGV[3]) " +
            
            "redis.call('ZREMRANGEBYSCORE', key, 0, now - window) " +
            "local current = redis.call('ZCARD', key) " +
            
            "if current < limit then " +
            "  redis.call('ZADD', key, now, now .. '-' .. math.random()) " +
            "  redis.call('EXPIRE', key, window + 1) " +
            "  return 1 " +
            "else " +
            "  return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(key),
            String.valueOf(limit),
            String.valueOf(System.currentTimeMillis() / 1000),
            String.valueOf(windowSizeSeconds)
        );
        
        return result == 1;
    }
}
```

### 2. 注解驱动的限流实现

#### 2.1 限流注解定义
```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流key的前缀
     */
    String key() default "rate_limit:";

    /**
     * 时间窗口，单位秒
     */
    int time() default 60;

    /**
     * 限制访问次数
     */
    int count() default 10;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流提示信息
     */
    String message() default "访问过于频繁，请稍后再试";
}
```

#### 2.2 限流切面实现
```java
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateKey(point, rateLimit);
        boolean allowed = isAllowed(key, rateLimit.count(), rateLimit.time());
        
        if (!allowed) {
            throw new RuntimeException(rateLimit.message());
        }
        
        return point.proceed();
    }
    
    private String generateKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        StringBuilder key = new StringBuilder(rateLimit.key());
        
        if (rateLimit.limitType() == LimitType.IP) {
            // 基于IP限流
            HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
            key.append(IpUtils.getIpAddr(request));
        } else if (rateLimit.limitType() == LimitType.USER) {
            // 基于用户限流
            // 从请求中获取用户ID
            key.append(getUserId());
        } else {
            // 基于方法名限流
            key.append(method.getName());
        }
        
        return key.toString();
    }
    
    private boolean isAllowed(String key, int count, int time) {
        String luaScript = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('INCRBY', key, 1) " +
            "if current == 1 then " +
            "  redis.call('EXPIRE', key, window) " +
            "  return 1 " +
            "elseif current < limit then " +
            "  return 1 " +
            "else " +
            "  return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(key),
            String.valueOf(count),
            String.valueOf(time)
        );
        
        return result == 1;
    }
}
```

## 具体接口限流配置

### 1. 报名接口限流
- 每个用户每分钟最多5次报名请求
- 每个IP每分钟最多10次报名请求
- 超过限制返回友好提示

### 2. 查询接口限流
- 活动列表查询：每分钟100次
- 活动详情查询：每分钟200次
- 用户信息查询：每分钟50次

### 3. 搜索接口限流
- 搜索请求：每分钟30次
- 防止恶意搜索占用资源

## 防刷策略实现

### 1. 报名防刷
```java
@Service
public class AntiFraudService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 检查用户报名行为是否异常
     */
    public boolean checkRegistrationBehavior(Long userId, Long activityId) {
        // 检查同一用户短时间内报名多个活动
        String userRecentKey = "user:recent:registrations:" + userId;
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        String oneMinuteAgo = String.valueOf(System.currentTimeMillis() - 60000);
        
        // 移除一分钟前的记录
        redisTemplate.opsForZSet().removeRangeByScore(userRecentKey, 0, Double.valueOf(oneMinuteAgo));
        
        // 检查当前分钟内的报名次数
        Long recentCount = redisTemplate.opsForZSet().count(userRecentKey, 
            Double.valueOf(oneMinuteAgo), Double.valueOf(currentTimestamp));
        
        if (recentCount != null && recentCount >= 5) { // 1分钟内最多报名5个活动
            return false;
        }
        
        // 添加当前报名记录
        redisTemplate.opsForZSet().add(userRecentKey, activityId.toString(), 
            System.currentTimeMillis());
        redisTemplate.expire(userRecentKey, Duration.ofMinutes(5));
        
        // 检查同一IP的报名行为
        String ipKey = "ip:registrations:" + getCurrentIp();
        String ipCount = redisTemplate.opsForValue().get(ipKey);
        if (ipCount != null && Integer.parseInt(ipCount) >= 10) { // IP每分钟最多10次报名
            return false;
        }
        
        // 更新IP计数
        redisTemplate.opsForValue().increment(ipKey, 1);
        redisTemplate.expire(ipKey, Duration.ofMinutes(1));
        
        return true;
    }
    
    private String getCurrentIp() {
        // 获取当前请求IP
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        return IpUtils.getIpAddr(request);
    }
}
```

### 2. 验证码防刷
- 高并发报名时自动启用验证码
- 智能识别异常流量触发验证码
- 支持多种验证码类型

## 监控和告警

### 1. 限流监控指标
- 限流触发次数统计
- 各维度限流情况分析
- 限流效果评估

### 2. 防刷监控指标
- 异常行为识别数量
- 验证码触发频率
- 恶意请求拦截率

### 3. 告警机制
- 限流触发过高时告警
- 异常行为激增时告警
- 系统资源使用过高时告警

## 配置管理

### 1. 动态配置
- 支持运行时调整限流参数
- 通过配置中心统一管理

### 2. 灰度发布
- 限流策略灰度发布
- 逐步扩大限流范围

## 性能考虑

### 1. Redis性能优化
- 使用连接池提高性能
- 合理设置Redis参数

### 2. Lua脚本优化
- 减少网络往返次数
- 原子性操作保证数据一致性

通过以上限流和防刷机制的设计和实现，校园活动报名系统可以有效防止恶意刷单和保护系统资源，确保系统在高并发场景下的稳定运行。