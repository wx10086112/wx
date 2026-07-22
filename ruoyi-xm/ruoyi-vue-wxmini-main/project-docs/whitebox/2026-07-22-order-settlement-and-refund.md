# 订单与分账白盒修复记录

日期：2026-07-22

## 已修复

1. 核销完成事件改为在订单事务提交后异步处理，避免订单事务回滚时提前生成财务记录。
2. 新增结算补偿任务。每 5 分钟扫描最多 20 个已完成但缺少分账台账、平台收入、商家结算或分销结算的订单，并按订单号幂等补齐。
3. 分账台账、平台收入、商家结算、分销结算的插入改为数据库原子幂等写入；数据库增加有效订单维度唯一索引。
4. 平台收入的 `commission_rate` 改为根据实际平台收入和订单金额计算，不再固定写入 `0.10`。
5. 退款订单只能从已支付状态进入已退款；已取消订单不能再被退款回调改写。
6. 退款记录的状态迁移收紧为：`APPROVED/ABNORMAL -> REFUNDED`，`APPROVED -> ABNORMAL`。微信接口暂时性异常采用最多 6 次指数退避重试，记录次数、最后原因和下次重试时间；达到上限后转为 `ABNORMAL`。退款执行前通过数据库租约抢占，避免事件和定时任务并发重复调用微信接口。
7. 全量建库脚本已与 `PlatformTransferRecordMapper` 对齐，补齐 `settlement_no`、收款对象、订单号、微信批次和回调字段。
8. 真实微信转账加入双重门槛：除 `transfer-enabled=true` 外，非 stub 模式必须明确配置 `WX_PAY_TRANSFER_LIVE_CONFIRMATION=REAL_WECHAT_TRANSFER_CONFIRMED`；自动任务开启而转账总开关关闭时应用拒绝启动。
9. 历史转账状态 `PENDING/SUCCESS` 迁移为 `WAITING/ARRIVED`，并将数据库默认值同步为 `WAITING`；退款表旧默认状态 `0` 迁移为 `1(PENDING)`。

## 本地验证

执行：

```powershell
mvn -pl ruoyi-mall-order,ruoyi-mall-finance,ruoyi-wxmini -am test
```

结果：构建成功，11 个相关测试全部通过。

- `OrderSettlementServiceImplTest`：分账记录完整创建；异常向外抛出以触发事务回滚。
- `PlatformIncomeServiceImplTest`：100.00 元订单、9.94 元平台收入记录 9.94% 实际抽成。
- `WxRefundEventListenerTest`：暂时性异常进入退避重试、达到上限转异常、原订单丢失转异常、并发执行方已持有租约时不重复调用微信。
- `WechatTransferSafetyGuardTest`：自动任务与总开关的不一致配置、真实转账缺少确认值、stub 模式以及确认后的真实转账均已覆盖。
- 6 个修改过的 MyBatis XML 文件均已通过 XML 解析。

## 服务器发布顺序

1. 先查看服务进程、当前发布目录、数据库名和应用配置，不覆盖现有预点单相关改动。
2. 执行迁移前的重复数据检查：

```sql
SELECT order_no, del_flag, COUNT(*) FROM platform_income GROUP BY order_no, del_flag HAVING COUNT(*) > 1;
SELECT order_no, del_flag, COUNT(*) FROM merchant_settlement_record GROUP BY order_no, del_flag HAVING COUNT(*) > 1;
SELECT order_no, distributor_id, del_flag, COUNT(*) FROM distributor_settlement_record GROUP BY order_no, distributor_id, del_flag HAVING COUNT(*) > 1;
SHOW CREATE TABLE platform_transfer_record;
```

3. 上述重复检查无结果后，执行 `sql/2026-07-22_order_settlement_whitebox_hotfix.sql`。
4. 部署后使用新构件重启实际后端进程，确认健康检查、订单创建、支付回调和核销接口正常。
5. 用一笔测试订单验证：支付成功、核销完成、`order_profit_ledger`、`platform_income`、`merchant_settlement_record` 和按需的 `distributor_settlement_record` 各只有一条有效记录。

## 配置结论

- `wx.pay.transfer-enabled` 和 `wx.pay.transfer-task-enabled` 默认关闭，保持关闭；确认微信转账资质和收款账户数据后才开启。真实转账还必须同时配置 `WX_PAY_TRANSFER_LIVE_CONFIRMATION=REAL_WECHAT_TRANSFER_CONFIRMED`。
- 真实微信支付、退款和分账需要服务器中的真实商户配置及微信回调，未在本地发起。

## 服务器发布结果

服务器：`101.34.207.58`

- 服务：`ruoyi-xm.service`，运行 JAR 为 `/opt/ruoyi-xm/ruoyi-admin.jar`。
- 数据库：MySQL `8.0.45`，数据库名 `ruoyi-cs`。
- 已执行迁移：`sql/2026-07-22_order_settlement_whitebox_hotfix.sql`。
- 三个有效订单幂等索引已确认存在：`uk_active_order_no`（平台收入、商家结算）和 `uk_active_order_distributor`（分销结算）。
- 转账表已具备当前代码使用的字段，默认状态已统一为 `WAITING`；历史有效转账记录为 0，完整性异常为 0。
- 退款表已补齐 `retry_count`、`last_retry_time`、`next_retry_time`、`last_retry_reason` 和 `idx_refund_retry(status, next_retry_time)`；默认状态已统一为 `1(PENDING)`。
- 服务器显式保持 `WX_PAY_TRANSFER_ENABLED=false`、`WX_PAY_TRANSFER_TASK_ENABLED=false`，真实转账确认值为空。
- 新 JAR SHA-256：`312ddebc3f937769eea6dbc1c079685d94c1c55842554f2f878d43c8e2eb4ece`。
- 本次旧 JAR 与环境文件备份：`/opt/ruoyi-xm/backups/20260722_1737_finance_refund_hardening/`。
- 重启后服务状态为 `active`，`http://127.0.0.1:8080/` 返回 HTTP 200。
- 已完成但缺失财务资料的订单数为 0。
- 当前可重试退款、退款异常、无效有效转账记录均为 0；重启后的 systemd 日志没有新增错误。
- 已逐项比对预点单相关控制器、服务和 Mapper 的旧/新 JAR 内容，哈希一致；本次发布没有回退预点单功能。
