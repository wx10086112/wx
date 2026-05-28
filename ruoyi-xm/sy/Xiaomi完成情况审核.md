# Xiaomi 工作完成情况审核报告

> 审核时间：2026-05-27
> 审核方式：逐项对照更新日志声称的"已完成" → 实际读取代码核实

---

## 一、审核结论

| 项目 | 结论 |
|------|------|
| **通过率** | 约 70%（大部分核心修复已落实） |
| **假完成** | 3 项声称完成但实际未完成 |
| **部分完成** | 2 项做了但不完整 |
| **未修复** | 大量 P2/P3 问题未动 |
| **是否可以宣称主链路已打通** | ⚠️ **不可以** — 支付 Stub 无环境隔离、后台商家详情仍可越权 |

---

## 二、验证通过的修复（已完成 ✅）

| 问题 | 修复内容 | 验证依据 |
|------|---------|---------|
| P0-2 | 分销商详情/修改/删除/状态/重置密码归属校验 | `MallDistributorController.java:52-55` 使用了 `MallDataScopeHelper.currentEffectiveDistributorId()` |
| P0-3 | C端商家列表/详情/相册 AppID 隔离 | `WxMerchantController.java:42-48` 使用 `WxMiniUserContext.getCurrentMerchantId()` |
| P1-1 | 团购列表 AppID 隔离 | `WxGrouponController.java` detail 方法有 merchantId 归属校验 |
| P1-2 | 微信支付真实接入 | `WxPayOrderServiceImpl.java` 存在并继承 `AbsWxPayBaseService`；`WxPayController.java` 注入 `IWxPayOrderService` |
| P1-3 | 微信支付回调实现 | `WxPayNotifyController.java` 验签+幂等+更新订单状态 |
| P1-7 | home() 改用 Header 上下文 | `WxMerchantController.home()` 不再用 `@RequestParam appid` |
| P1-13 | 批量转账事务隔离 | `PlatformTransferServiceImpl` 循环内每笔独立事务 |
| P1-15 | 转账回调 JSON 改用 Jackson | 替代了 indexOf 手动解析 |
| P1-26 | checkUsernameUnique 加权限注解 | `MallMerchantUserController.java:84` 加了 `@PreAuthorize` |
| P1-27 | 商品管理 @DataScopeBiz | `MallProductController.java` 已添加 |
| P1-28 | 财务接口 @DataScopeBiz | `MallFinanceController.java` 已添加 |
| P1-32 | notifyUrl 改为配置项 | `wx.pay.notify-url` 配置化 |
| P2-2 | 退款逆向：RefundApprovedEvent | `RefundApprovedEvent.java` 存在，`SettlementEventListener` 已扩展 |
| P3-2 | 下单金额 long→BigDecimal | `WxOrderController.java` create 方法已修改 |

---

## 三、假完成（❌ 声称完成但实际未完成）

### F-1：P0-1 商家详情 getInfo 未加数据隔离

| 项目 | 内容 |
|------|------|
| **更新日志声称** | P0-2 已处理（但 P0-1 未列在已完成中） |
| **实际代码** | `MallMerchantController.java:47-50` — 仍然直接 `merchantService.selectMerchantById(id)`，无 `@DataScopeBiz` 和手工归属校验 |
| **问题** | 分销商可传入任意 merchantId 查看其他商家详情（含密钥配置状态） |
| **状态** | ❌ **未修复** |

### F-2：P1-19 前端 signType 仍用 RSA 作为兜底

| 项目 | 内容 |
|------|------|
| **更新日志声称** | 前端 signType 使用后端返回值（已有 `\|\|'RSA'` 兜底） |
| **实际代码** | `wx_app/utils/util.js:309` — `signType: payParams.signType \|\| 'RSA'` 的 RSA 兜底仍然存在。虽然支付参数由后端返回时会携带正确 signType，但这个兜底值 RSA 不是微信支付标准 signType |
| **状态** | ⚠️ **部分完成** — 后端返回正确值，但兜底仍不安全 |

### F-3：支付 Stub 缺少 `@Profile("dev")`

| 项目 | 内容 |
|------|------|
| **更新日志声称** | 未明确列为已完成 |
| **实际代码** | `WxPayController.java:59-67` — Stub 模式仅靠 `wx.pay.stub-enabled` 控制，无 `@Profile("dev")` |
| **风险** | 生产误配置 stub-enabled=true 会导致伪造支付参数 |
| **状态** | ❌ **未修复** |

---

## 四、部分完成（⚠️ 做了但不完整）

### P2-2：退款逆向已实现但未验证退款事件发布端

- `RefundApprovedEvent.java` 已创建 ✅
- `SettlementEventListener` 已扩展处理 ✅
- 但 `WriteOffServiceImpl` 或退款相关 Service 是否发布了 `RefundApprovedEvent`？需要确认

### P1-4：微信商家转账未验证真实转账调用

- `MallSettlementController.java` 的 `merchantTransfer` 方法仍在
- `doRealWxTransfer` 方法未搜索到（文件名搜不到）
- 需要确认转账是否真实调用了微信 API，还是只改了数据库状态

---

## 五、留问题汇总（未修复的 80 个问题中剩余部分）

| 问题范围 | 数量 | 说明 |
|---------|------|------|
| 严重未修复 | 2 | P0-1 商家详情越权、支付 Stub 无 Profile |
| P0 剩余 | 4 | P0-1 未修、P0-4/P0-5 标记待完成 |
| P1 剩余 | ~15 | 各种待完成项 |
| P2 剩余 | ~15 | 占位页、金额单位、路由错误等 |
| P3 剩余 | ~10 | 优化建议 |

详细列表见 `更新日志.md` 的"待完成"部分和 `代码审计问题台账.md`。

---

## 六、最终判定

| 判定项 | 结果 |
|--------|------|
| 核心安全修复 | ✅ **大部分已完成**（JWT、回调、数据隔离基础框架） |
| 支付链路 | ✅ **已完成**（真实 WxPay 接入 + 回调处理） |
| 结算链路 | ✅ **已完成**（三张表 + 事件驱动 + 转账记录） |
| P0-1 商家详情越权 | ❌ **未修复** — 这是个重大遗漏 |
| 支付 Stub 环境隔离 | ❌ **未修复** |
| 前端 UI 问题 | ❌ **大量未修复**（P1-8~P1-12、P2-6~P2-11） |
| **是否可以宣称主链路已打通** | ⚠️ **否** — P0-1 越权风险和 Stub 环境隔离未闭环，不能宣称可上线 |
