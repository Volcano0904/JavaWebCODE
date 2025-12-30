# 校园活动报名系统

## 项目概述
这是一个高并发的校园活动报名系统，采用微服务架构设计，支持高可用、可扩展的后端架构。

## 系统架构
- **API网关**: 统一入口，路由转发，限流熔断
- **用户服务**: 用户注册、登录、权限管理
- **活动服务**: 活动发布、管理、查询
- **报名服务**: 报名处理、状态管理
- **数据库**: MySQL集群，分库分表
- **缓存**: Redis集群，提高访问速度
- **消息队列**: Kafka，异步处理报名请求
- **监控系统**: Prometheus + Grafana，ELK日志收集

## 项目结构
```
src/
├── api-gateway/          # API网关服务
├── user-service/         # 用户微服务
├── activity-service/     # 活动微服务
├── registration-service/ # 报名微服务
├── common/               # 公共组件
├── config/               # 配置文件
├── db-migrations/        # 数据库迁移脚本
├── redis-config/         # Redis配置
├── kafka-config/         # Kafka配置
└── monitoring/           # 监控配置
docs/                     # 文档
scripts/                  # 脚本文件
deploy/                   # 部署配置
```

## 技术栈
- **后端**: Spring Boot, Spring Cloud
- **数据库**: MySQL
- **缓存**: Redis
- **消息队列**: Kafka
- **监控**: Prometheus, Grafana, ELK
- **容器化**: Docker, Kubernetes