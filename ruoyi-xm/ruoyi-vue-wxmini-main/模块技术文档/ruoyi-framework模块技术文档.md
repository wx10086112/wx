# ruoyi-framework 模块技术文档

## 模块概述

ruoyi-framework是若依框架的核心框架模块，提供了Spring Boot的核心配置、安全认证、Web配置、数据源管理等基础功能。

## 配置类

### `com.ruoyi.framework.config.RuoYiConfig` -- 若依系统配置

**主要功能**：
- 系统参数配置
- 文件上传配置
- 用户信息配置
- 权限配置

**主要配置项**：
- `addressEnabled` - 地址开关
- `demoEnabled` - 演示模式开关
- `copyrightYear` - 版权年份
- `instance` - 单例配置

### `com.ruoyi.framework.config.DruidConfig` -- 数据源配置

**主要功能**：
- Druid数据源配置
- 数据库连接池管理
- SQL监控配置
- 防火墙配置

**主要配置**：
```java
@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    @ConfigurationProperties("spring.datasource.druid.slave")
    public DataSource slaveDataSource() {
        return DruidDataSourceBuilder.create().build();
    }
}
```

### `com.ruoyi.framework.config.MybatisConfig` -- MyBatis配置

**主要功能**：
- MyBatis配置
- 分页插件配置
- SQL拦截器配置

**主要配置**：
```java
@Configuration
public class MybatisConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
```

### `com.ruoyi.framework.config.WebMvcConfig` -- Web MVC配置

**主要功能**：
- Web MVC配置
- 拦截器配置
- 跨域配置
- 静态资源配置

**主要配置**：
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RepeatSubmitInterceptor())
                .addPathPatterns("/**");
    }
}
```

### `com.ruoyi.framework.config.ThreadPoolConfig` -- 线程池配置

**主要功能**：
- 线程池配置
- 异步任务执行
- 线程池监控

**主要配置**：
```java
@Configuration
public class ThreadPoolConfig {
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        return executor;
    }
}
```

## 安全配置

### `com.ruoyi.framework.config.SecurityConfig` -- 安全配置

**主要功能**：
- Spring Security配置
- 认证配置
- 授权配置
- 密码编码器配置

**主要配置**：
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }
}
```

### `com.ruoyi.framework.web.service.UserDetailsServiceImpl` -- 用户详情服务实现

**主要功能**：
- 用户认证
- 权限验证
- 用户信息加载

**主要方法**：
- `loadUserByUsername()` - 根据用户名加载用户信息
- `createLoginUser()` - 创建登录用户对象

### `com.ruoyi.framework.web.service.TokenService` -- Token服务

**主要功能**：
- JWT Token生成
- Token验证
- Token刷新
- 用户信息缓存

**主要方法**：
- `createToken()` - 创建Token
- `verifyToken()` - 验证Token
- `refreshToken()` - 刷新Token
- `getLoginUser()` - 获取登录用户

## 过滤器

### `com.ruoyi.framework.security.filter.JwtAuthenticationTokenFilter` -- JWT认证过滤器

**主要功能**：
- JWT Token验证
- 用户身份认证
- 请求拦截处理

**处理流程**：
1. 从请求头获取Token
2. 验证Token有效性
3. 解析用户信息
4. 设置用户上下文
5. 放行或拒绝请求

### `com.ruoyi.framework.web.filter.RepeatSubmitInterceptor` -- 防重复提交拦截器

**主要功能**：
- 防止重复提交
- 请求拦截处理
- Token验证

## 拦截器

### `com.ruoyi.framework.aspectj.AspectjAutoProxyConfiguration` -- AOP自动代理配置

**主要功能**：
- AOP自动代理配置
- 切面自动代理

### `com.ruoyi.framework.aspectj.LogAspect` -- 日志切面

**主要功能**：
- 操作日志记录
- 方法执行监控
- 异常处理

### `com.ruoyi.framework.aspectj.DataScopeAspect` -- 数据权限切面

**主要功能**：
- 数据权限过滤
- SQL条件注入
- 权限范围控制

## 异常处理

### `com.ruoyi.framework.exception.GlobalExceptionHandler` -- 全局异常处理器

**主要功能**：
- 全局异常捕获
- 统一异常响应
- 错误日志记录

**主要异常处理**：
- `ServiceException` - 业务异常
- `AuthenticationException` - 认证异常
- `AccessDeniedException` - 授权异常
- `SQLException` - 数据库异常
- `Exception` - 其他异常

### `com.ruoyi.framework.exception.ServiceException` -- 业务异常

**主要功能**：
- 业务异常定义
- 异常信息封装
- 异常码管理

## 实体类

### `com.ruoyi.framework.web.domain.AjaxResult` -- 统一响应结果

**主要功能**：
- 统一API响应格式
- 成功/失败响应
- 数据封装

**主要方法**：
- `success()` - 成功响应
- `error()` - 错误响应
- `put()` - 添加数据
- `remove()` - 移除数据

### `com.ruoyi.framework.web.domain.TableDataInfo` -- 表格数据信息

**主要功能**：
- 表格数据封装
- 分页信息
- 数据列表

**主要字段**：
- `total` - 总记录数
- `rows` - 数据列表
- `code` - 状态码
- `msg` - 消息

### `com.ruoyi.framework.web.domain.Page` -- 分页信息

**主要功能**：
- 分页参数封装
- 分页计算
- 分页验证

**主要字段**：
- `pageNum` - 当前页码
- `pageSize` - 每页大小
- `total` - 总记录数
- `pages` - 总页数

## 工具类

### `com.ruoyi.framework.web.service.PermissionService` -- 权限服务

**主要功能**：
- 权限验证
- 角色验证
- 数据权限

**主要方法**：
- `hasPermi()` - 验证权限
- `hasRole()` - 验证角色
- `hasAnyPermi()` - 验证任意权限
- `hasAnyRole()` - 验证任意角色

### `com.ruoyi.framework.web.service.DictService` -- 字典服务

**主要功能**：
- 字典数据缓存
- 字典标签获取
- 字典数据管理

**主要方法**：
- `getDictCache()` - 获取字典缓存
- `getDictLabel()` - 获取字典标签
- `getDictValue()` - 获取字典值

### `com.ruoyi.framework.web.service.ConfigService` -- 配置服务

**主要功能**：
- 系统配置缓存
- 配置参数获取
- 配置管理

**主要方法**：
- `getConfigCache()` - 获取配置缓存
- `getConfigKey()` - 获取配置值
- `setConfigCache()` - 设置配置缓存

## 监听器

### `com.ruoyi.framework.web.listener.ConfigListener` -- 配置监听器

**主要功能**：
- 配置变更监听
- 缓存更新处理

### `com.ruoyi.framework.web.listener.DictListener` -- 字典监听器

**主要功能**：
- 字典变更监听
- 缓存更新处理

## 任务调度

### `com.ruoyi.framework.task.AsyncFactory` -- 异步任务工厂

**主要功能**：
- 异步任务创建
- 任务类型定义
- 任务执行管理

### `com.ruoyi.framework.task.AsyncManager` -- 异步任务管理器

**主要功能**：
- 异步任务执行
- 任务队列管理
- 线程池管理

## 数据权限

### `com.ruoyi.framework.security.handle.DataPermissionHandler` -- 数据权限处理器

**主要功能**：
- 数据权限过滤
- SQL条件注入
- 权限范围控制

### `com.ruoyi.framework.security.context.PermissionContextHolder` -- 权限上下文持有者

**主要功能**：
- 权限上下文管理
- 线程安全处理
- 上下文传递

## 文件处理

### `com.ruoyi.framework.config.FileUploadConfig` -- 文件上传配置

**主要功能**：
- 文件上传路径配置
- 文件大小限制
- 文件类型限制

### `com.ruoyi.framework.utils.FileUtils` -- 文件工具类

**主要功能**：
- 文件上传处理
- 文件下载处理
- 文件验证

## 国际化

### `com.ruoyi.framework.config.I18nConfig` -- 国际化配置

**主要功能**：
- 国际化资源配置
- 语言切换处理
- 消息解析

## 缓存配置

### `com.ruoyi.framework.config.CacheConfig` -- 缓存配置

**主要功能**：
- Redis缓存配置
- 缓存策略配置
- 缓存管理

## 配置文件

### application.yml配置示例

```yaml
# Spring配置
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: password
        driver-class-name: com.mysql.cj.jdbc.Driver
      slave:
        enabled: true
        url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: password
        driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: delFlag
      logic-delete-value: 2
      logic-not-delete-value: 0

# JWT配置
jwt:
  secret: abcdefghijklmnopqrstuvwxyz
  expireTime: 30

# 文件上传配置
file:
  path: /opt/upload/
  maxSize: 10
  allowType: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx
```

## 注意事项

1. **安全配置**：JWT密钥要定期更换
2. **数据源配置**：主从数据源配置要正确
3. **文件上传**：文件路径要有访问权限
4. **缓存配置**：Redis连接要正常
5. **线程池配置**：要根据业务调整参数
6. **日志配置**：日志级别要合理设置
7. **数据权限**：权限规则要正确配置
8. **异常处理**：异常信息要友好提示

## 依赖说明

### 主要依赖

```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<!-- Druid -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 版本信息

- **模块版本**: 4.6.0
- **Spring Boot版本**: 2.5.x
- **Spring Security版本**: 5.5.x
- **MyBatis Plus版本**: 3.4.x
- **Druid版本**: 1.2.x
- **Java版本**: JDK 1.8+
- **作者**: ruoyi
