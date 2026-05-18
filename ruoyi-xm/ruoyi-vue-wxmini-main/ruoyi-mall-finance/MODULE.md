# ruoyi-mall-finance 模块说明

## 职责

商城财务管理模块，提供平台流水查询、利润分成记录、商户账单、提现管理、收入统计及财务报表等后台管理功能。

## 包结构

### domain

| 类名 | 关键字段 | 说明 |
|------|----------|------|
| `TransactionRecord` | id, merchantId, type, amount, balance, orderNo, description | 交易流水记录 |
| `PlatformIncome` | id, merchantId, orderNo, orderAmount, commissionRate, commission | 平台收入/利润分成 |
| `MerchantBill` | id, merchantId, billNo, billType, startDate, endDate, totalOrders, totalAmount, totalCommission, netIncome, status, settleTime | 商户账单（当前无 Mapper，疑似预留） |
| `WithdrawRecord` | id, merchantId, amount, bankName, bankAccount, accountName, status, auditTime, payTime, rejectReason | 提现记录 |

所有实体继承 `BaseEntity`（来自 mall-common），支持 `createTime`/`updateTime` 等公共字段。

### mapper

| Mapper | 关键方法 |
|--------|----------|
| `TransactionRecordMapper` | CRUD, `selectTransactionRecordByMerchantId`, `sumAmountByType(type)`, `sumTodayByType(type)`, `sumMonthByType(type)`, `selectMonthlyReport()` |
| `PlatformIncomeMapper` | CRUD, `selectPlatformIncomeByMerchantId`, `sumTotalCommission()` |
| `WithdrawRecordMapper` | CRUD, `selectWithdrawRecordByMerchantId`, `sumPaidTotal()` |

聚合统计方法均使用 `@Select` 注解直接写 SQL。

### service

接口：`IFinanceService`，实现：`FinanceServiceImpl`

| 方法 | 说明 |
|------|------|
| `selectPlatformFlowList(TransactionRecord)` | 查询平台流水（置 merchantId=null，不按商户过滤） |
| `selectProfitShareList(PlatformIncome)` | 查询利润分成列表 |
| `selectWithdrawList(WithdrawRecord)` | 查询提现记录列表 |
| `selectMerchantFlowList(TransactionRecord)` | 查询商户流水（按 merchantId 过滤） |

### controller

`MallFinanceController` — 路径前缀 `/mall/finance`，权限 `mall:finance:list`

| HTTP 方法 | 路径 | 权限 | 说明 |
|-----------|------|------|------|
| GET | `/mall/finance/platform-flow/list` | `mall:finance:list` | 平台流水分页列表 |
| GET | `/mall/finance/profit-share/list` | `mall:finance:list` | 利润分成分页列表 |
| GET | `/mall/finance/withdraw/list` | `mall:finance:list` | 提现记录分页列表 |
| GET | `/mall/finance/merchant-flow/list` | `mall:finance:list` | 商户流水分页列表 |

`MallFinanceExtendController` — 路径前缀 `/mall/finance`

| HTTP 方法 | 路径 | 权限 | 说明 |
|-----------|------|------|------|
| POST | `/mall/finance/withdraw/approve/{id}/{status}` | `mall:finance:edit` | 审批提现申请 |
| GET | `/mall/finance/income/stats` | `mall:finance:list` | 收入统计（总佣金/今日/本月/已提现总额） |
| GET | `/mall/finance/report` | `mall:finance:list` | 财务报表（按月分组汇总） |

## 模块依赖

- **ruoyi-mall-common** — 基础公共类（BaseEntity 等）
- **ruoyi-mall-order** — `MallFinanceExtendController` 直接注入 `RefundRecordMapper`，用于关联退款数据

## 被依赖

- **ruoyi-admin** — 唯一引用本模块的上层模块，负责 Spring Boot 启动和组件扫描

## 使用示例

```java
// Controller 中注入 Service 查询平台流水
@Autowired
private IFinanceService financeService;

// 分页查询平台流水
@GetMapping("/platform-flow/list")
public TableDataInfo list(TransactionRecord query) {
    startPage();
    return getDataTable(financeService.selectPlatformFlowList(query));
}

// Controller 中直接注入 Mapper 做聚合统计
@Autowired
private PlatformIncomeMapper platformIncomeMapper;

BigDecimal totalCommission = platformIncomeMapper.sumTotalCommission();
BigDecimal todayIncome = transactionRecordMapper.sumTodayByType(1);   // type=1 收入
```
