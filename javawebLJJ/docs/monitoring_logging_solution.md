# 监控和日志方案设计

## 监控日志策略概述

为确保校园活动报名系统的稳定运行，需要建立全面的监控和日志体系，包括应用性能监控、业务指标监控、系统日志收集、分布式链路追踪等。

## 监控架构设计

### 1. 监控层级

#### 1.1 基础设施监控
- 服务器资源监控 (CPU、内存、磁盘、网络)
- 数据库性能监控
- 缓存性能监控
- 消息队列监控

#### 1.2 应用层监控
- JVM监控 (堆内存、GC、线程)
- Web请求监控 (QPS、响应时间、错误率)
- 业务指标监控 (注册数、报名数、活跃用户)

#### 1.3 业务层监控
- 关键业务流程监控
- 转化率监控
- 用户行为分析

### 2. 监控技术栈

#### 2.1 指标收集
- Prometheus: 指标收集和存储
- Micrometer: 应用指标暴露

#### 2.2 日志收集
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Filebeat: 日志收集代理

#### 2.3 链路追踪
- Jaeger/Zipkin: 分布式链路追踪

#### 2.4 监控展示
- Grafana: 指标可视化
- Kibana: 日志分析可视化

## Prometheus指标监控

### 1. 自定义业务指标

```java
@Component
public class BusinessMetrics {
    
    private final Counter registrationCounter;
    private final Timer registrationTimer;
    private final Gauge activeUsersGauge;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.registrationCounter = Counter.builder("registrations_total")
            .description("Total number of registrations")
            .register(meterRegistry);
            
        this.registrationTimer = Timer.builder("registration_duration_seconds")
            .description("Registration processing time")
            .register(meterRegistry);
            
        this.activeUsersGauge = Gauge.builder("active_users")
            .description("Number of active users")
            .register(meterRegistry, this, BusinessMetrics::getActiveUserCount);
    }
    
    public void recordRegistration() {
        registrationCounter.increment();
    }
    
    public <T> T recordRegistrationTime(Supplier<T> operation) {
        return registrationTimer.recordCallable(operation::get);
    }
    
    private double getActiveUserCount() {
        // 返回活跃用户数的逻辑
        return userService.getActiveUserCount();
    }
}
```

### 2. Spring Boot Actuator端点

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
  prometheus:
    metrics:
      export:
        enabled: true
```

### 3. 关键监控指标

#### 3.1 应用性能指标
```
http_server_requests_total - HTTP请求总数
http_server_requests_seconds - HTTP请求响应时间
jvm_memory_used_bytes - JVM内存使用情况
jvm_gc_pause_seconds - GC暂停时间
tomcat_connections_active - Tomcat活跃连接数
```

#### 3.2 业务指标
```
registrations_total - 总报名数
registrations_active - 活跃报名数
activity_views_total - 活动浏览数
user_registrations_total - 用户注册数
```

## 日志收集方案

### 1. 结构化日志

```java
@Service
public class RegistrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS");
    
    public Registration createRegistration(RegistrationDTO registrationDTO) {
        String traceId = generateTraceId();
        
        // 业务日志
        businessLogger.info("Registration attempt - traceId: {}, userId: {}, activityId: {}", 
            traceId, registrationDTO.getUserId(), registrationDTO.getActivityId());
        
        try {
            // 执行报名逻辑
            Registration registration = new Registration();
            // ... 设置属性
            Registration savedRegistration = registrationRepository.save(registration);
            
            businessLogger.info("Registration successful - traceId: {}, registrationId: {}", 
                traceId, savedRegistration.getId());
                
            return savedRegistration;
        } catch (Exception e) {
            businessLogger.error("Registration failed - traceId: {}, userId: {}, activityId: {}, error: {}", 
                traceId, registrationDTO.getUserId(), registrationDTO.getActivityId(), e.getMessage(), e);
            throw e;
        }
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
```

### 2. Logback配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 应用日志 -->
    <appender name="APPLICATION" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <arguments/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <!-- 业务日志 -->
    <appender name="BUSINESS" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/business.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/business.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <mdc/>
                <message/>
                <arguments/>
            </providers>
        </encoder>
    </appender>

    <!-- 安全日志 -->
    <appender name="SECURITY" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/security.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/security.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>60</maxHistory>
            <totalSizeCap>2GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <message/>
                <mdc/>
            </providers>
        </encoder>
    </appender>

    <logger name="BUSINESS" level="INFO" additivity="false">
        <appender-ref ref="BUSINESS"/>
    </logger>

    <logger name="SECURITY" level="INFO" additivity="false">
        <appender-ref ref="SECURITY"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="APPLICATION"/>
    </root>
</configuration>
```

### 3. 日志字段规范

```json
{
  "timestamp": "2023-10-01T10:00:00.123Z",
  "level": "INFO",
  "logger": "BUSINESS",
  "message": "Registration successful",
  "traceId": "abc123",
  "userId": 123,
  "activityId": 456,
  "registrationId": 789,
  "application": "registration-service",
  "host": "server-01"
}
```

## 分布式链路追踪

### 1. 链路追踪配置

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://zipkin-server:9411/
    sender:
      type: web
  sleuth:
    sampler:
      probability: 0.1  # 采样率10%
    baggage:
      correlation-enabled: true
      remote-fields: 
        - traceId
        - userId
        - activityId
```

### 2. 链路追踪注解使用

```java
@Service
public class RegistrationOrchestrationService {
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RegistrationService registrationService;
    
    @NewSpan("process-registration")
    public Registration processRegistration(RegistrationDTO registrationDTO) {
        // 添加自定义标签
        SpanCustomizer span = tracer.currentSpan().customizer();
        span.tag("userId", registrationDTO.getUserId().toString());
        span.tag("activityId", registrationDTO.getActivityId().toString());
        
        // 调用其他服务
        Activity activity = activityService.getActivityById(registrationDTO.getActivityId());
        User user = userService.findById(registrationDTO.getUserId());
        
        return registrationService.createRegistration(registrationDTO);
    }
}
```

## ELK日志分析

### 1. Filebeat配置

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/campus-app/*.log
  json.keys_under_root: true
  json.add_error_key: true
  fields:
    service: campus-activity-system
    environment: production

output.logstash:
  hosts: ["logstash:5044"]

processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
  - add_docker_metadata: ~
```

### 2. Logstash配置

```
input {
  beats {
    port => 5044
  }
}

filter {
  if [type] == "application" {
    json {
      source => "message"
    }
  }
  
  # 添加地理IP解析
  if [client_ip] {
    geoip {
      source => "client_ip"
      target => "geoip"
      database => "/etc/logstash/GeoLite2-City.mmdb"
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "campus-app-%{+YYYY.MM.dd}"
  }
}
```

## 告警机制

### 1. Prometheus告警规则

```yaml
# alert_rules.yml
groups:
- name: campus_app_alerts
  rules:
  - alert: HighRegistrationErrorRate
    expr: rate(http_server_requests_total{status=~"5..", uri=~"/api/registrations"}[5m]) > 0.1
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "High registration error rate"
      description: "Registration error rate is {{ $value }} per second"

  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High response time"
      description: "95th percentile response time is {{ $value }} seconds"

  - alert: HighGCActivity
    expr: rate(jvm_gc_pause_seconds_count[5m]) > 10
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "High GC activity"
      description: "GC pause count is {{ $value }} per minute"
```

### 2. 告警通知

```yaml
# alertmanager.yml
route:
  group_by: ['alertname']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 1h
  receiver: 'campus-team'

receivers:
- name: 'campus-team'
  email_configs:
  - to: 'admin@campus.edu'
    from: 'alert@campus.edu'
    smarthost: 'smtp.campus.edu:587'
    auth_username: 'alert'
    auth_identity: 'alert'
    auth_password: 'password'
  webhook_configs:
  - url: 'http://notification-service:8080/api/alerts/webhook'
```

## 监控面板

### 1. Grafana仪表板配置

```json
{
  "dashboard": {
    "id": null,
    "title": "Campus Activity System Dashboard",
    "tags": ["campus", "activity", "registration"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "HTTP Requests",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_total[1m])",
            "legendFormat": "{{method}} {{status}}"
          }
        ]
      },
      {
        "id": 2,
        "title": "Registration Count",
        "type": "singlestat",
        "targets": [
          {
            "expr": "registrations_total",
            "refId": "A"
          }
        ]
      }
    ]
  }
}
```

## 监控指标分类

### 1. 系统指标
- CPU使用率
- 内存使用率
- 磁盘IO
- 网络流量

### 2. 应用指标
- JVM堆内存使用
- GC频率和暂停时间
- 线程池状态
- 连接池状态

### 3. 业务指标
- 注册用户数
- 活动创建数
- 报名成功率
- 活跃用户数

### 4. 性能指标
- API响应时间
- 数据库查询时间
- 缓存命中率
- 消息队列延迟

## 日志分析最佳实践

### 1. 日志级别规范
- ERROR: 系统错误，需要立即处理
- WARN: 潜在问题，需要关注
- INFO: 重要业务流程
- DEBUG: 详细调试信息

### 2. 敏感信息处理
- 不记录密码、密钥等敏感信息
- 对手机号、邮箱等进行脱敏
- 遵循数据保护法规

### 3. 日志轮转策略
- 按大小和时间轮转
- 保留适当的历史日志
- 定期清理过期日志

通过以上全面的监控和日志方案，可以确保校园活动报名系统具备完善的可观测性，及时发现和解决问题，保障系统的稳定运行。