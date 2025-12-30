# 安全防护措施设计

## 安全防护策略概述

校园活动报名系统需要面对多种安全威胁，包括SQL注入、XSS攻击、CSRF攻击、身份伪造等。为确保系统安全，需要建立多层次、全方位的安全防护体系。

## 安全威胁分析

### 1. 数据库安全威胁
- SQL注入攻击
- 数据库权限滥用
- 敏感数据泄露

### 2. 应用层安全威胁
- XSS跨站脚本攻击
- CSRF跨站请求伪造
- 身份认证绕过
- 会话劫持

### 3. 传输层安全威胁
- 数据传输未加密
- 中间人攻击

### 4. 业务逻辑安全威胁
- 恶意刷单
- 数据篡改
- 权限提升

## 安全防护措施

### 1. 身份认证与授权

#### 1.1 JWT令牌认证
- 使用JWT实现无状态认证
- 设置合理的过期时间
- 支持令牌刷新机制

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            // 设置认证信息到SecurityContext
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, null, null);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        
        chain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### 1.2 基于角色的访问控制 (RBAC)
- 定义用户角色和权限
- 实现细粒度权限控制
- 支持动态权限配置

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {
    
    @PreAuthorize("hasPermission(#activityId, 'Activity', 'edit')")
    @PutMapping("/activities/{activityId}")
    public ResponseEntity<?> updateActivity(@PathVariable Long activityId, @RequestBody ActivityDTO activity) {
        // 更新活动逻辑
    }
}
```

### 2. 输入验证与过滤

#### 2.1 参数验证
- 使用Bean Validation进行参数校验
- 自定义验证注解
- 防止恶意输入

```java
@Data
public class RegistrationDTO {
    @NotNull(message = "活动ID不能为空")
    @Min(value = 1, message = "活动ID必须大于0")
    private Long activityId;

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
}
```

#### 2.2 SQL注入防护
- 使用参数化查询
- 避免拼接SQL语句
- 使用ORM框架的查询API

```java
// 正确方式 - 使用参数化查询
public List<Activity> findActivitiesByTitle(String title) {
    return activityRepository.findByTitleContaining(title);
}

// 错误方式 - 容易受SQL注入攻击
/*
public List<Activity> findActivitiesByTitle(String title) {
    String sql = "SELECT * FROM activity WHERE title LIKE '%" + title + "%'";
    // 这种方式容易受SQL注入攻击
}
*/
```

### 3. 输出编码与过滤

#### 3.1 XSS防护
- 对输出内容进行HTML编码
- 使用CSP策略
- 设置安全响应头

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .contentSecurityPolicy("script-src 'self'")
                .and()
            .xssProtection()
                .and()
            .frameOptions()
                .deny()
                .and()
            .contentTypeOptions()
                .and()
            .referrerPolicy()
                .and()
            .httpStrictTransportSecurity()
                .maxAgeInSeconds(31536000)
                .and()
            .csrf().disable();
            
        return http.build();
    }
}
```

### 4. 传输安全

#### 4.1 HTTPS强制使用
- 强制使用HTTPS协议
- HSTS头设置
- SSL证书管理

#### 4.2 敏感数据加密
- 用户密码加密存储
- 使用BCrypt算法
- 敏感信息传输加密

```java
@Service
public class UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        // 密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }
    
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
```

### 5. API安全

#### 5.1 API限流
- 实现API访问频率限制
- 防止API滥用
- 保护后端服务

#### 5.2 API密钥管理
- 为第三方应用分配API密钥
- 实现密钥生命周期管理
- 支持密钥权限控制

### 6. 会话安全

#### 6.1 会话固定攻击防护
- 登录后生成新的会话ID
- 防止会话固定攻击

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
    // 验证用户凭据
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    
    // 创建新的会话以防止会话固定攻击
    session.invalidate();
    HttpSession newSession = request.getSession(true);
    
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // 生成JWT令牌
    String token = jwtUtil.generateToken(request.getUsername());
    
    return ResponseEntity.ok(new LoginResponse(token));
}
```

#### 6.2 会话超时设置
- 设置合理的会话超时时间
- 自动清理过期会话

### 7. 日志安全

#### 7.1 安全日志记录
- 记录安全相关事件
- 包括登录、登出、权限变更等
- 防止日志注入

```java
@Component
public class SecurityLogger {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    
    public void logLoginAttempt(String username, String ip, boolean success) {
        if (success) {
            securityLogger.info("Successful login - User: {}, IP: {}", 
                sanitizeInput(username), sanitizeInput(ip));
        } else {
            securityLogger.warn("Failed login attempt - User: {}, IP: {}", 
                sanitizeInput(username), sanitizeInput(ip));
        }
    }
    
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("[\n\r\t]", "_");
    }
}
```

### 8. 数据库安全

#### 8.1 最小权限原则
- 数据库用户权限最小化
- 不同服务使用不同数据库账户
- 定期审查数据库权限

#### 8.2 数据脱敏
- 敏感数据脱敏显示
- 支持字段级加密

```sql
-- 数据库视图实现数据脱敏
CREATE VIEW user_public_view AS
SELECT 
    id,
    username,
    CONCAT(LEFT(phone, 3), '****', RIGHT(phone, 4)) AS phone_masked,
    CONCAT(LEFT(email, 1), '***@', SUBSTRING(email, LOCATE('@', email) + 1)) AS email_masked
FROM user;
```

### 9. 安全配置管理

#### 9.1 配置文件安全
- 敏感配置外部化
- 使用配置中心管理
- 配置文件权限控制

#### 9.2 环境隔离
- 开发、测试、生产环境分离
- 不同环境使用不同安全配置

### 10. 安全测试

#### 10.1 静态代码分析
- 使用工具扫描安全漏洞
- 定期进行代码审查

#### 10.2 动态安全测试
- 进行渗透测试
- 漏洞扫描

## 安全监控与响应

### 1. 安全事件监控
- 实时监控安全事件
- 异常行为检测
- 威胁情报集成

### 2. 应急响应机制
- 安全事件响应流程
- 快速处置机制
- 事件复盘改进

## 安全培训与意识

### 1. 开发安全培训
- 安全编码规范
- 常见漏洞防范
- 安全测试方法

### 2. 安全意识提升
- 定期安全培训
- 安全知识分享
- 安全文化建设

通过以上全面的安全防护措施，校园活动报名系统能够有效抵御各类安全威胁，确保系统和数据的安全性。