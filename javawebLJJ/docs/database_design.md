# 数据库设计文档

## 数据库设计原则

1. **高并发支持**: 采用分库分表策略，提升系统并发处理能力
2. **数据一致性**: 使用事务保证数据一致性
3. **性能优化**: 合理设计索引，优化查询性能
4. **扩展性**: 预留字段，支持业务扩展
5. **安全性**: 防止SQL注入，数据加密存储

## 数据库表结构设计

### 1. 用户表 (user)

```sql
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `student_id` VARCHAR(20) COMMENT '学号',
  `college` VARCHAR(100) COMMENT '学院',
  `major` VARCHAR(100) COMMENT '专业',
  `role` TINYINT DEFAULT 1 COMMENT '角色(1-普通用户,2-管理员,3-超级管理员)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1-正常,0-禁用)',
  `avatar` VARCHAR(255) COMMENT '头像地址',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_college` (`college`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 2. 活动分类表 (activity_category)

```sql
CREATE TABLE `activity_category` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(500) COMMENT '分类描述',
  `parent_id` INT UNSIGNED DEFAULT 0 COMMENT '父分类ID(0-顶级分类)',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动分类表';
```

### 3. 活动表 (activity)

```sql
CREATE TABLE `activity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
  `description` TEXT COMMENT '活动描述',
  `poster_url` VARCHAR(500) COMMENT '活动海报URL',
  `organizer_id` BIGINT UNSIGNED NOT NULL COMMENT '组织者ID(用户ID)',
  `organizer_name` VARCHAR(100) NOT NULL COMMENT '组织者姓名',
  `category_id` INT UNSIGNED NOT NULL COMMENT '活动分类ID',
  `location` VARCHAR(200) NOT NULL COMMENT '活动地点',
  `address_detail` VARCHAR(500) COMMENT '详细地址',
  `start_time` TIMESTAMP NOT NULL COMMENT '开始时间',
  `end_time` TIMESTAMP NOT NULL COMMENT '结束时间',
  `registration_start_time` TIMESTAMP NOT NULL COMMENT '报名开始时间',
  `registration_end_time` TIMESTAMP NOT NULL COMMENT '报名结束时间',
  `max_participants` INT DEFAULT 0 COMMENT '最大参与人数(0-无限制)',
  `current_participants` INT DEFAULT 0 COMMENT '当前报名人数',
  `status` TINYINT DEFAULT 1 COMMENT '活动状态(1-草稿,2-已发布,3-进行中,4-已结束,5-已取消)',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门(1-是,0-否)',
  `is_recommended` TINYINT DEFAULT 0 COMMENT '是否推荐(1-是,0-否)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_organizer_id` (`organizer_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_registration_time` (`registration_start_time`, `registration_end_time`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_is_recommended` (`is_recommended`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';
```

### 4. 报名表 (registration)

```sql
CREATE TABLE `registration` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `student_id` VARCHAR(20) COMMENT '学号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '报名状态(1-待审核,2-已通过,3-已拒绝,4-已取消)',
  `reason` VARCHAR(500) COMMENT '审核理由',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';
```

### 5. 活动收藏表 (activity_favorite)

```sql
CREATE TABLE `activity_favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动收藏表';
```

### 6. 消息通知表 (notification)

```sql
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` TINYINT NOT NULL COMMENT '通知类型(1-系统通知,2-报名通知,3-活动变更)',
  `target_user_id` BIGINT UNSIGNED NOT NULL COMMENT '目标用户ID',
  `related_id` BIGINT COMMENT '关联ID(活动ID或报名ID)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1-未读,2-已读)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` TIMESTAMP NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_target_user_id` (`target_user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';
```

### 7. 系统配置表 (system_config)

```sql
CREATE TABLE `system_config` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` VARCHAR(1000) NOT NULL COMMENT '配置值',
  `description` VARCHAR(500) COMMENT '配置描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
```

## 数据库索引设计说明

### 用户表索引
- 主键索引: `id` - 快速定位用户
- 唯一索引: `username` - 防止重复用户名
- 普通索引: `email`, `student_id`, `college` - 提高查询效率

### 活动表索引
- 主键索引: `id` - 快速定位活动
- 普通索引: `organizer_id`, `category_id` - 按组织者、分类查询
- 普通索引: `status`, `start_time`, `end_time` - 按状态、时间查询
- 复合索引: `registration_start_time`, `registration_end_time` - 查询报名时间段活动

### 报名表索引
- 主键索引: `id` - 快速定位报名记录
- 唯一索引: `activity_id`, `user_id` - 防止重复报名
- 普通索引: `activity_id`, `user_id`, `status` - 提高查询效率

## 数据库安全措施

1. **SQL注入防护**: 使用预编译语句和参数化查询
2. **数据加密**: 敏感信息(如密码)采用哈希加密存储
3. **访问控制**: 设置合理的数据库权限
4. **审计日志**: 记录数据库操作日志
5. **定期备份**: 制定数据备份策略