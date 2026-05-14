# 更新日志

## 2026-05-13

### 初始化

- 从仓库拉取项目源码，首次本地构建

### 修复：Controller URL 路径冲突导致启动失败

**问题：** `ruoyi-wxmini` 和 `ruoyi-admin` 两个模块的 Controller 定义了相同的 URL 映射，
Spring Boot 启动时抛出 `AmbiguousMappingException`，无法注册重复的请求路径。

**涉及文件与改动：**

#### 1. MallFinanceExtendController.java
路径：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/mall/MallFinanceExtendController.java`

删除 3 个与 `MallFinanceController`（ruoyi-wxmini）重复的方法：

| 删除的方法 | URL 路径 | 保留方 |
|---|---|---|
| `platformFlowList()` | `GET /mall/finance/platform-flow/list` | MallFinanceController |
| `profitShareList()` | `GET /mall/finance/profit-share/list` | MallFinanceController |
| `withdrawList()` | `GET /mall/finance/withdraw/list` | MallFinanceController |

保留的独有接口：
- `POST /mall/finance/withdraw/approve/{id}/{status}` — 审批提现
- `GET /mall/finance/income/stats` — 收益统计
- `GET /mall/finance/report` — 财务报表

清理了不再使用的 import：`TableDataInfo`、`PlatformIncome`、`TransactionRecord`

#### 2. MallOrderExtendController.java
路径：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/mall/MallOrderExtendController.java`

删除 2 个与 `MallOrderController`（ruoyi-wxmini）重复的方法：

| 删除的方法 | URL 路径 | 保留方 |
|---|---|---|
| `list()` | `GET /mall/order/list` | MallOrderController |
| `detail()` | `GET /mall/order/{id}` | MallOrderController.getInfo（返回订单+订单项，数据更完整） |

保留的独有接口：
- `GET /mall/order/abnormal/list` — 异常订单列表（status=5）
- `GET /mall/order/after-sale/list` — 售后订单列表（status=4）

清理了不再使用的 import：`AjaxResult`

### 实现：工作台页面对接数据库

**问题：** `DashboardServiceImpl` 所有方法返回硬编码的 0 和空列表，前端工作台页面只能显示 mock 数据。

**改动：**

#### 1. DashboardServiceImpl.java
路径：`ruoyi-system/src/main/java/com/ruoyi/wxmini/service/impl/DashboardServiceImpl.java`

重写全部 5 个方法，调用 Mapper 的真实数据库查询替代硬编码返回值：

| 方法 | 数据来源 | 查询内容 |
|---|---|---|
| `selectDashboardStats()` | MallOrderMapper + MerchantMapper + MallUserMapper | 今日交易额、总流水、今日订单数、活跃商家数、用户总数、今日新增用户 |
| `selectTrendData()` | MallOrderMapper.selectDailyStatsForWeek() | 近7天每日订单数、金额、完成数 |
| `selectOrderStatusData()` | MallOrderMapper.selectOrderStatsByStatus() | 各状态订单数量（待支付/已支付/已使用/已完成/已退款/已取消） |
| `selectHotProducts()` | ProductMapper.selectHotProducts(5) | 按销量降序 TOP5 商品 |
| `selectMerchantRank()` | MerchantMapper.selectMerchantRankByIncome(5) | 按总收入降序 TOP5 商家 |

#### 2. Mapper 接口新增查询方法

- **MallOrderMapper** — 新增 `countTodayOrders()`、`sumTodayAmount()`、`sumTotalFlow()`、`selectDailyStatsForWeek()`（@Select 注解）
- **MerchantMapper** — 新增 `countActiveMerchant()`、`selectMerchantRankByIncome(int limit)`（@Select 注解）
- **MallUserMapper** — 新增 `countTotal()`、`countTodayNew()`（@Select 注解）
- **ProductMapper** — 新增 `selectHotProducts(int limit)`（@Select 注解，关联 merchant 表获取商家名称）

### 修复：工作台趋势图 SQL 报错 only_full_group_by

**问题：** `MallOrderMapper.selectDailyStatsForWeek()` 的 SQL 中，SELECT 使用 `DATE_FORMAT(create_time, '%m-%d')`，
但 GROUP BY 使用 `DATE(create_time)`，在 MySQL `sql_mode=only_full_group_by` 模式下报语法错误。

**改动：** 将 GROUP BY 改为 `DATE_FORMAT(create_time, '%m-%d')` 与 SELECT 表达式一致，ORDER BY 改为 `MIN(create_time)` 保证按时间排序。

文件：`ruoyi-system/src/main/java/com/ruoyi/wxmini/mapper/MallOrderMapper.java`
