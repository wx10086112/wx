# 商家端结算中心后端改造说明

本文档给后端同事使用，说明商家端小程序从“财务收益 + 手动提现”切换为“微信官方 T+1 自动到账结算中心”后，后端需要怎么改。

适用目录：`ruoyi-xm/merchant_app`

## 1. 改造目标

当前旧模型：

- 商家端查看财务概览
- 商家端主动输入金额并提交提现申请
- 后端处理 `POST /finance/withdraw`

目标新模型：

- 商家端不再发起提现
- 订单核销完成后进入微信官方结算链路
- 平台按 `T+1` 自动发起到商家结算账户的打款
- 商家端只看“待到账、打款中、已到账、到账失败”

一句话：前端要的是“结算结果展示页”，不是“提现申请页”。

## 2. 前端已经切换的接口语义

商家端前端已改为优先请求：

```text
GET /wxmini/merchant-mini/settlement/overview
```

前端仍保留了 `getFinanceOverview -> getSettlementOverview` 的兼容别名，但正式后端请直接实现新的 `settlement` 路径。

旧接口：

```text
GET /wxmini/merchant-mini/finance/overview
POST /wxmini/merchant-mini/finance/withdraw
```

建议：

- `GET /finance/overview` 可以短期保留并内部转发到新服务，避免旧版本调用报错
- `POST /finance/withdraw` 建议废弃，返回“该版本已切换为微信自动结算，无需商家手动提现”

## 3. 必改的后端模块

### 3.1 控制器

新增或替换为：

```text
GET /wxmini/merchant-mini/settlement/overview
```

建议控制器命名：

- `MerchantMiniSettlementController`

### 3.2 服务层

建议新增：

- `IMerchantMiniSettlementService`
- `MerchantMiniSettlementServiceImpl`

职责：

- 查询商家结算账户
- 统计待结算/打款中/已到账金额
- 聚合到账记录
- 聚合订单结算流水
- 处理退款后的逆向回滚状态

### 3.3 DTO / VO

建议新增以下 DTO：

- `MerchantMiniSettlementOverviewDto`
- `MerchantMiniSettlementAccountDto`
- `MerchantMiniSettlementRecordDto`
- `MerchantMiniSettlementLedgerDto`

## 4. 前端期望的数据结构

`GET /wxmini/merchant-mini/settlement/overview`

返回体：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "todayIncomeAmount": 32600,
    "monthIncomeAmount": 156800,
    "pendingSettleAmount": 45000,
    "settledAmount": 111800,
    "processingAmount": 12000,
    "pendingAutoTransferAmount": 57000,
    "platformFeeAmount": 17400,
    "completedOrderCount": 8,
    "autoTransferMode": "T+1",
    "nextAutoTransferTime": 1778400000000,
    "settlementAccount": {
      "accountName": "蓝屿轻养·国贸旗舰店",
      "bankName": "招商银行",
      "accountNoTail": "6601",
      "status": "VERIFIED"
    },
    "settlementRecordList": [
      {
        "settlementId": "S202605250001",
        "orderNo": "M202605080003",
        "title": "芳香舒压 SPA 90 分钟",
        "amount": 16920,
        "status": "ARRIVED",
        "applyTime": 1778190000000,
        "expectedTransferTime": 1778276400000,
        "arriveTime": 1778276400000,
        "remark": "微信已自动打款至结算卡"
      },
      {
        "settlementId": "S202605250002",
        "orderNo": "M202605090001",
        "title": "肩颈理疗放松套餐 60 分钟",
        "amount": 12420,
        "status": "WAITING_T1",
        "applyTime": 1778269800000,
        "expectedTransferTime": 1778356200000,
        "arriveTime": null,
        "remark": "订单完成后进入 T+1 自动打款队列"
      }
    ],
    "ledgerList": [
      {
        "ledgerId": 1,
        "orderNo": "M202605080003",
        "title": "芳香舒压 SPA 90 分钟",
        "orderAmount": 18800,
        "merchantAmount": 16920,
        "platformFeeAmount": 1880,
        "status": "SETTLED",
        "finishTime": 1778190000000,
        "settleTime": 1778276400000
      }
    ]
  }
}
```

## 5. 字段定义

### 5.1 overview 顶部统计

| 字段 | 类型 | 说明 |
|------|------|------|
| `todayIncomeAmount` | long | 今日核销完成后产生的商家收益，单位分 |
| `monthIncomeAmount` | long | 本月商家收益，单位分 |
| `pendingSettleAmount` | long | 已完单但未到 T+1 打款时间的金额 |
| `settledAmount` | long | 已到账金额 |
| `processingAmount` | long | 微信已受理、尚未到账的金额 |
| `pendingAutoTransferAmount` | long | 待到账总额 = `pendingSettleAmount + processingAmount` |
| `platformFeeAmount` | long | 平台累计佣金 |
| `completedOrderCount` | int | 已完成订单数 |
| `autoTransferMode` | string | 固定传 `T+1` |
| `nextAutoTransferTime` | long | 下一次自动打款任务时间戳，毫秒 |

### 5.2 settlementAccount

| 字段 | 类型 | 说明 |
|------|------|------|
| `accountName` | string | 结算主体名称 |
| `bankName` | string | 结算银行名称 |
| `accountNoTail` | string | 银行卡后四位 |
| `status` | string | `VERIFIED` / `PENDING` / `DISABLED` |

### 5.3 settlementRecordList

| 字段 | 类型 | 说明 |
|------|------|------|
| `settlementId` | string | 到账记录 ID |
| `orderNo` | string | 关联订单号，可为空 |
| `title` | string | 商品/批次名称 |
| `amount` | long | 到账金额，单位分 |
| `status` | string | `WAITING_T1` / `TRANSFERRING` / `ARRIVED` / `FAILED` |
| `applyTime` | long | 订单进入结算链路时间 |
| `expectedTransferTime` | long | 预计微信自动打款时间 |
| `arriveTime` | long/null | 实际到账时间 |
| `remark` | string | 记录备注或失败原因 |

### 5.4 ledgerList

这是订单结算流水，不是提现记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| `ledgerId` | long | 流水 ID |
| `orderNo` | string | 订单号 |
| `title` | string | 商品名称 |
| `orderAmount` | long | 订单支付金额 |
| `merchantAmount` | long | 商家分账金额 |
| `platformFeeAmount` | long | 平台佣金 |
| `status` | string | `PENDING` / `SETTLED` |
| `finishTime` | long | 核销完成时间 |
| `settleTime` | long | 预计打款时间 |

## 6. 建议状态机

### 6.1 订单完成到结算

```text
COMPLETED
  -> WAITING_T1
  -> TRANSFERRING
  -> ARRIVED
```

异常路径：

```text
WAITING_T1 / TRANSFERRING
  -> FAILED
```

退款路径：

```text
COMPLETED
  -> REFUNDING
  -> REFUNDED
```

### 6.2 状态说明

| 状态 | 含义 |
|------|------|
| `WAITING_T1` | 订单完成，但还没到次日自动打款时间 |
| `TRANSFERRING` | 已向微信发起自动打款，等待到账 |
| `ARRIVED` | 已到账到商家结算银行卡 |
| `FAILED` | 自动打款失败，需要平台排查 |

## 7. 定时任务建议

后端至少要有两个任务：

### 7.1 结算入队任务

触发条件：

- 团购订单核销完成
- 订单状态改为 `COMPLETED`

处理动作：

- 生成结算流水
- 计算商家分账金额、平台佣金
- 标记为 `WAITING_T1`

### 7.2 T+1 自动打款任务

建议执行频率：

- 每天固定时间批量跑
- 或每 10 分钟扫描一次满足条件的记录

处理动作：

- 查询 `WAITING_T1` 且已满足 T+1 条件的记录
- 调用微信官方自动提现/预约提现能力
- 成功后改为 `TRANSFERRING`
- 微信结果回调或主动查询成功后改为 `ARRIVED`
- 失败则改为 `FAILED`

## 8. 退款逆向处理

这一块必须做，不然后台财务会错。

### 场景 1

订单在 `WAITING_T1` 状态时退款：

- 直接取消结算记录
- 订单进入退款链路

### 场景 2

订单在 `TRANSFERRING` 状态时退款：

- 标记该结算记录为“退款处理中”
- 根据微信实际能力做逆向回滚或人工复核

### 场景 3

订单已 `ARRIVED` 后退款：

- 需要生成负向结算记录或逆向分账记录
- 保证商家端“到账记录”和平台财务总账能对平

## 9. 数据库建议

建议新增一张结算记录表，例如：

```text
merchant_settlement_record
```

建议字段：

- `id`
- `merchant_id`
- `store_id`
- `order_no`
- `order_amount`
- `merchant_amount`
- `platform_fee_amount`
- `status`
- `apply_time`
- `expected_transfer_time`
- `arrive_time`
- `fail_reason`
- `wechat_batch_no`
- `create_time`
- `update_time`

如果你们已有分账流水表，也可以在现有表上扩展，只要能支撑商家端的 `settlementRecordList + ledgerList` 两种视图。

## 10. 前端对应页面

前端已经按下面这个页面结构改好了：

- `pages/finance/finance`
  - 顶部：待自动到账金额
  - 中部：结算账户信息
  - 中部：到账记录筛选
  - 底部：订单结算流水

前端已经移除：

- 金额输入框
- 申请提现按钮
- 手动提现流程

## 11. 建议联调顺序

1. 先返回 `settlementAccount + overview 顶部字段`
2. 再补 `settlementRecordList`
3. 最后补 `ledgerList`
4. 最后接微信真实自动打款能力

这样前端能最快先联通页面，不必等完整支付链路全部打完。

## 12. 最后建议

如果短期内后端还来不及全量切换，可以先做一个兼容版本：

- 新接口：`GET /settlement/overview`
- 旧接口：`GET /finance/overview` 内部调用同一套 service
- 旧提现接口：直接返回提示文案，不再真正受理

这样前端、后端、测试三方都更平滑。
