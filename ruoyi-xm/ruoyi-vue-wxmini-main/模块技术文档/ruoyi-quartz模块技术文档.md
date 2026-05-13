# ruoyi-quartz 模块技术文档

## 模块概述

ruoyi-quartz是若依框架的定时任务模块，基于Quartz框架实现，提供了完整的定时任务管理功能，支持任务的创建、修改、删除、启动、停止等操作。

## 实体类说明

### `com.ruoyi.quartz.domain.SysJob` -- 定时任务调度表

对应数据库表 `sys_job`，用于存储定时任务的配置信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| jobId | Long | 任务ID |
| jobName | String | 任务名称 |
| jobGroup | String | 任务组名 |
| invokeTarget | String | 调用目标字符串 |
| cronExpression | String | cron执行表达式 |
| misfirePolicy | String | cron计划策略 |
| concurrent | String | 是否并发执行（0允许 1禁止） |
| status | String | 任务状态（0正常 1暂停） |

**继承关系**：
- 继承 `BaseEntity`

**验证注解**：
- `@NotBlank(message = "任务名称不能为空")`
- `@Size(min = 0, max = 64, message = "任务名称不能超过64个字符")`
- `@NotBlank(message = "调用目标字符串不能为空")`
- `@Size(min = 0, max = 500, message = "调用目标字符串长度不能超过500个字符")`
- `@NotBlank(message = "Cron执行表达式不能为空")`
- `@Size(min = 0, max = 255, message = "Cron执行表达式不能超过255个字符")`

**Excel注解**：
- `@Excel(name = "任务序号", cellType = ColumnType.NUMERIC)`
- `@Excel(name = "任务名称")`
- `@Excel(name = "任务组名")`
- `@Excel(name = "调用目标字符串")`
- `@Excel(name = "执行表达式")`
- `@Excel(name = "计划策略", readConverterExp = "0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行")`
- `@Excel(name = "并发执行", readConverterExp = "0=允许,1=禁止")`
- `@Excel(name = "任务状态", readConverterExp = "0=正常,1=暂停")`

**特殊方法**：
- `getNextValidTime()` - 获取下次执行时间，基于cron表达式计算

---

### `com.ruoyi.quartz.domain.SysJobLog` -- 定时任务调度日志表

对应数据库表 `sys_job_log`，用于存储定时任务的执行日志。

| 字段 | 类型 | 说明 |
|------|------|------|
| jobLogId | Long | ID |
| jobName | String | 任务名称 |
| jobGroup | String | 任务组名 |
| invokeTarget | String | 调用目标字符串 |
| jobMessage | String | 日志信息 |
| status | String | 执行状态（0正常 1失败） |
| exceptionInfo | String | 异常信息 |
| startTime | Date | 开始时间 |
| stopTime | Date | 停止时间 |

**继承关系**：
- 继承 `BaseEntity`

**Excel注解**：
- `@Excel(name = "日志序号")`
- `@Excel(name = "任务名称")`
- `@Excel(name = "任务组名")`
- `@Excel(name = "调用目标字符串")`
- `@Excel(name = "日志信息")`
- `@Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")`
- `@Excel(name = "异常信息")`

## 控制器类

### `com.ruoyi.quartz.controller.SysJobController` -- 定时任务控制器

**主要接口**：
- `GET /monitor/job/list` - 查询定时任务列表
- `POST /monitor/job/list` - 查询定时任务列表
- `POST /monitor/job/add` - 新增定时任务
- `PUT /monitor/job/edit` - 修改定时任务
- `DELETE /monitor/job/{jobIds}` - 删除定时任务
- `PUT /monitor/job/changeStatus` - 任务状态修改
- `PUT /monitor/job/run` - 立即执行任务
- `GET /monitor/job/export` - 导出定时任务

### `com.ruoyi.quartz.controller.SysJobLogController` -- 定时任务日志控制器

**主要接口**：
- `GET /monitor/jobLog/list` - 查询定时任务日志列表
- `DELETE /monitor/jobLog/{jobLogIds}` - 删除定时任务日志
- `DELETE /monitor/jobLog/clean` - 清空定时任务日志
- `GET /monitor/jobLog/export` - 导出定时任务日志

## 服务类

### `com.ruoyi.quartz.service.ISysJobService` -- 定时任务服务接口

**主要方法**：
- `selectJobList()` - 查询定时任务列表
- `selectJobById()` - 查询定时任务配置
- `deleteJobByIds()` - 批量删除定时任务
- `jobPause()` - 暂停定时任务
- `jobResume()` - 恢复定时任务
- `runJob()` - 立即执行任务
- `insertJob()` - 新增定时任务
- `updateJob()` - 更新定时任务
- `checkJobUnique()` - 校验任务是否存在

### `com.ruoyi.quartz.service.ISysJobLogService` -- 定时任务日志服务接口

**主要方法**：
- `selectJobLogList()` - 查询定时任务日志列表
- `selectJobLogById()` - 查询定时任务日志
- `deleteJobLogByIds()` - 批量删除定时任务日志
- `cleanJobLog()` - 清空定时任务日志
- `addJobLog()` - 新增任务日志

## 实现类

### `com.ruoyi.quartz.service.impl.SysJobServiceImpl` -- 定时任务服务实现

**核心功能**：
- Quartz调度器管理
- 任务CRUD操作
- 任务状态控制
- 任务执行管理

### `com.ruoyi.quartz.service.impl.SysJobLogServiceImpl` -- 定时任务日志服务实现

**核心功能**：
- 日志记录和查询
- 日志清理
- 日志导出

## 任务调度器

### `com.ruoyi.quartz.util.QuartzJobManagement` -- Quartz任务管理器

**主要功能**：
- 任务调度管理
- 任务执行监控
- 任务状态维护

### `com.ruoyi.quartz.util.AbstractQuartzJob` -- 抽象定时任务

**主要功能**：
- 任务执行前后处理
- 异常处理
- 日志记录

## 工具类

### `com.ruoyi.quartz.util.CronUtils` -- Cron表达式工具类

**主要方法**：
- `getNextExecution()` - 获取下次执行时间
- `isValid()` - 验证Cron表达式
- `getExecutionDescription()` - 获取执行描述

### `com.ruoyi.quartz.util.ScheduleUtils` -- 调度工具类

**主要方法**：
- `createScheduleJob()` - 创建定时任务
- `runScheduleJob()` - 立即执行任务
- `pauseScheduleJob()` - 暂停定时任务
- `resumeScheduleJob()` - 恢复定时任务
- `deleteScheduleJob()` - 删除定时任务

## 配置类

### `com.ruoyi.quartz.config.ScheduleConfig` -- 定时任务配置

**主要配置**：
```java
@Configuration
public class ScheduleConfig {
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties());
        factory.setJobFactory(jobFactory);
        factory.setAutoStartup(true);
        return factory;
    }
    
    @Bean
    public Properties quartzProperties() {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "RuoyiScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "20");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
        return properties;
    }
}
```

## 数据库表结构

### sys_job 表结构

```sql
CREATE TABLE `sys_job` (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';
```

### sys_job_log 表结构

```sql
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';
```

## Cron表达式说明

### 基本格式

```
秒 分 时 日 月 周 [年]
```

### 字段说明

| 字段 | 允许值 | 允许的特殊字符 |
|------|--------|----------------|
| 秒 | 0-59 | , - * / |
| 分 | 0-59 | , - * / |
| 时 | 0-23 | , - * / |
| 日 | 1-31 | , - * / ? L W C |
| 月 | 1-12 | , - * / |
| 周 | 1-7 | , - * / ? L C # |

### 特殊字符说明

- `*` - 所有值
- `?` - 不指定值（仅用于日和周）
- `-` - 范围
- `,` - 列表
- `/` - 步长
- `L` - 最后（仅用于日和周）
- `W` - 工作日（仅用于日）
- `C` - 计算后（仅用于日和周）
- `#` - 第几个（仅用于周）

### 常用示例

```
0 0 12 * * ?           每天12点执行
0 15 10 ? * *          每天10点15分执行
0 15 10 * * ?          每天10点15分执行
0 15 10 * * ? *        每天10点15分执行
0 15 10 * * ? 2005     2005年每天10点15分执行
0 * 14 * * ?           每天14点到15点之间每分钟执行
0 0/5 14 * * ?         每天14点到15点之间每5分钟执行
0 0/5 14,18 * * ?      每天14点到15点、18点到19点之间每5分钟执行
0 0-5 14 * * ?         每天14点到14点5分之间每分钟执行
0 10,44 14 ? 3 WED     3月的星期三的14点10分和44分执行
0 15 10 ? * MON-FRI    周一到周五的10点15分执行
0 15 10 15 * ?         每月15号10点15分执行
0 15 10 L * ?          每月最后一天10点15分执行
0 15 10 ? * 6L         每月最后一个星期五10点15分执行
0 15 10 ? * 6#3        每月第3个星期五10点15分执行
```

## 任务执行策略

### MisfirePolicy（计划执行错误策略）

| 值 | 说明 |
|----|------|
| 0 | 默认策略 |
| 1 | 立即触发执行 |
| 2 | 触发一次执行 |
| 3 | 不触发立即执行 |

### Concurrent（并发控制）

| 值 | 说明 |
|----|------|
| 0 | 允许并发执行 |
| 1 | 禁止并发执行 |

### Status（任务状态）

| 值 | 说明 |
|----|------|
| 0 | 正常 |
| 1 | 暂停 |

## 任务目标格式

### Spring Bean方式

```
beanName.methodName
```

示例：
```
taskService.runTask
```

### 静态方法方式

```
className.staticMethodName
```

示例：
```
com.ruoyi.quartz.task.RyTask.runParams
```

### 带参数方式

```
beanName.methodName("param1", "param2")
```

示例：
```
taskService.runTask("param1", "param2")
```

## 使用示例

### 创建任务

```java
SysJob job = new SysJob();
job.setJobName("测试任务");
job.setJobGroup("DEFAULT");
job.setInvokeTarget("taskService.runTask");
job.setCronExpression("0 0/5 * * * ?");
job.setMisfirePolicy("3");
job.setConcurrent("0");
job.setStatus("0");
sysJobService.insertJob(job);
```

### 执行任务

```java
sysJobService.runJob(sysJob);
```

### 暂停任务

```java
sysJobService.jobPause(sysJob);
```

### 恢复任务

```java
sysJobService.jobResume(sysJob);
```

## 注意事项

1. **Cron表达式**：必须正确配置，否则任务无法执行
2. **目标方法**：必须是公共方法，且参数类型要匹配
3. **异常处理**：任务执行异常会记录到日志表
4. **并发控制**：根据业务需求选择是否允许并发执行
5. **任务日志**：系统会自动记录任务执行日志
6. **集群环境**：支持集群环境下的任务调度
7. **任务持久化**：任务信息存储在数据库中
8. **重启恢复**：系统重启后会自动恢复暂停的任务

## 版本信息

- **模块版本**: 4.6.0
- **Quartz版本**: 2.3.x
- **Java版本**: JDK 1.8+
- **Spring Boot版本**: 2.5.x
- **作者**: ruoyi
