# 微信服务商模式上线前测试规格

本文档用于上线前验收后台、后端、C 端小程序、商家小程序、微信支付服务商链路。当前业务模型按“每个商户一个 C 端微信小程序，平台是微信支付服务商，后台可按平台/分销商/商户查看流水”执行。

## 1. 上线准入结论

只有下面 P0 全部通过，才允许进入正式上线或灰度。

- P0 必须全部通过：登录租户识别、下单、服务商预下单、支付回调、退款申请/审核/退款回调、核销、结算、转账、后台数据隔离、金额分转换。
- P1 允许带记录灰度：页面展示小问题、非核心列表筛选、导出字段顺序。
- P2 可以上线后排期：文案、低频运营工具、非主链路体验优化。

不能用“只跑本地 stub”替代微信真实闭环。stub 只能证明代码和数据链路基本通，最终上线前至少要跑一次真实或体验版小额支付闭环。

## 2. 测试数据

先执行迁移，再执行完整测试 seed。

```sql
-- 核心迁移/热修顺序
source project-docs/test-sql/05_product_image.sql;
source project-docs/test-sql/08_order_alter.sql;
source project-docs/test-sql/10_groupon_activity_item.sql;
source project-docs/test-sql/14_soft_delete_and_amount.sql;
source project-docs/test-sql/18_platform_transfer_record.sql;
source project-docs/test-sql/2026-06-01_merchant_payment_share_fields.sql;
source project-docs/test-sql/2026-06-04_distributor_admin_role_menu.sql;
source project-docs/test-sql/2026-06-04_distributor_receiver_fields_hotfix.sql;
source sql/2026-06-08_distributor_del_flag_hotfix.sql;
source sql/2026-06-08_sys_user_scope_hotfix.sql;

-- 完整测试数据
source project-docs/test-sql/2026-06-08_service_provider_full_test_seed.sql;

-- seed 自检，所有 check_item 应为 PASS
source project-docs/test-sql/2026-06-08_service_provider_seed_assertions.sql;
```

固定后台账号，密码统一是 `admin123`。

| 账号 | 角色 | 预期数据范围 |
|---|---|---|
| `test_sp_platform` | 平台 | 可看全部测试商户、订单、退款、结算、转账 |
| `test_sp_dist_9901` | 分销商 9901 | 只能看 9901 分销商下的商户 9901/9902 |
| `test_sp_dist_9902` | 分销商 9902 | 只能看 9903/9904 等本分销商数据 |
| `test_sp_mch_9901` | 商户 9901 | 只能看商户 9901 自己的数据 |
| `test_sp_mch_9902` | 商户 9902 | 只能看商户 9902 自己的数据 |

固定小程序配置。

| 商户 | AppID | Secret | sub_mchid | 用途 |
|---|---|---|---|---|
| 9901 | `wx_test_sp_mch_9901` | `TEST_SECRET_9901_REPLACE_ME` | `1900009901` | 主完整链路 |
| 9902 | `wx_test_sp_mch_9902` | `TEST_SECRET_9902_REPLACE_ME` | `1900009902` | 同分销商跨商户隔离 |
| 9903 | `wx_test_sp_mch_9903` | `NULL` | `NULL` | 配置缺失失败测试 |
| 9904 | `wx_test_sp_mch_9904` | `TEST_SECRET_9904_REPLACE_ME` | `1900009904` | 商户停用失败测试 |
| 9905 | `wx_test_sp_mch_9905` | `TEST_SECRET_9905_REPLACE_ME` | `1900009905` | 平台直属商户，无分销商分账 |

测试 Secret 只用于本地/dev seed。线上必须在后台商户详情里填真实微信小程序 `AppSecret`，不能把 `TEST_SECRET_*` 带到生产。

开发环境 C 端登录测试。

```http
GET /wxmini/login/test?appid=wx_test_sp_mch_9901
```

预期返回 `appId=wx_test_sp_mch_9901`、`merchantId=9901`、`openId=test_openid_wx_test_sp_mch_9901`、`apiToken`。

后续 C 端请求必须带：

```http
X-Wx-AppId: wx_test_sp_mch_9901
Wx-Authorization: Bearer <apiToken>
```

## 3. 环境检查

本地/dev 环境。

- `spring.profiles.active` 包含 `dev`。
- `wx.pay.stub-enabled=true`。
- `wxmini.login.test-enabled=true`。
- 后端端口默认 `8080`。
- 能访问数据库，已执行完整 seed。

本地后端启动后，先跑 C 端和商家端冒烟脚本。

```powershell
powershell -ExecutionPolicy Bypass -File project-docs/test-scripts/service-provider-smoke.ps1 -BaseUrl http://localhost:8080
```

脚本会固定使用 `wx_test_sp_mch_9901/9902/9903/9904`、`test_sp_mch9901_owner/admin123` 和 seed 中的订单/商品数据，覆盖登录、租户隔离、下单、stub 预支付、缺配置商户拦截、重复退款拦截、商家端登录和商家端跨入口重放拦截。

预发/生产联调环境。

- `wx.pay.stub-enabled=false`。
- 服务商 `sp_mchid`、API v3 key、证书序列号、私钥、平台证书配置完整。
- 支付回调地址、退款回调地址、转账回调地址是公网 HTTPS。
- 每个商户后台配置真实 `cMiniAppId`、真实 `cMiniAppSecret`、真实 `merchantWxMchId`。
- 小程序请求头 `X-Wx-AppId` 与后台商户 `cMiniAppId` 一致。
- 系统参数已填真实隐私联系兜底信息：`mall.privacy.operatorName`、`mall.privacy.servicePhone`、`mall.privacy.contactEmail`、`mall.privacy.contactAddress`，并刷新 `sys_config` 缓存或重启后端。
- 使用每个商户真实小程序 `AppID` 请求 `/wxmini/template/config`，请求头带 `X-Wx-AppId`；返回的 `contactInfo/privacyInfo` 应优先使用该 AppID 对应商户的名称、电话、地址、营业时间，邮箱和用户权利请求说明使用系统参数兜底，并与微信公众平台隐私保护指引后台填写内容一致。

## 4. P0 用例

### P0-01 数据初始化

步骤：

1. 执行完整迁移和 seed。
2. 查看 seed 最后的统计查询。
3. 后台登录 `test_sp_platform`。

通过标准：

- 商户 9901-9905 都存在。
- `merchant_wx_mch_id`、分账比例、AppID/Secret 显示符合测试数据。
- `platform_transfer_record` 使用新字段 `transfer_no/settlement_no/target_type/receiver_openid`。

### P0-02 C 端租户识别

步骤：

1. `GET /wxmini/login/test?appid=wx_test_sp_mch_9901`。
2. 使用返回 token 请求商户、团购、订单接口，带 `X-Wx-AppId=wx_test_sp_mch_9901`。
3. 改成 `X-Wx-AppId=wx_test_sp_mch_9902` 重放同一个 token。

通过标准：

- 9901 token 正常只能访问 9901 上下文。
- token 与请求头 AppID 不一致时返回 403 或业务错误。
- 不允许前端传 `openId` 改变支付人身份。

### P0-03 C 端下单

步骤：

1. 9901 token 创建订单，商品 `993001`，数量 1。
2. 9901 token 创建订单，商品 `993003`，数量 1。
3. 尝试同单混合 `993001` 和 `993011`。
4. 9901 token 带 9902 AppID 下单 9901 商品。

通过标准：

- 单商户商品下单成功，返回 `orderNo/orderAmount/payAmount`，金额单位为分。
- 跨商户商品合并下单失败。
- token/AppID/商品商户不一致失败。
- `mall_order.user_id` 写入 `user_info.id`，不是字符串 `user_info.user_id`。

### P0-04 服务商预下单

步骤：

1. 用 9901 token 对待支付订单调用 `POST /wxmini/pay/order/create`。
2. 本地 stub 期望返回 `prepay_id=wx_stub_<orderNo>`。
3. 预发/生产真实联调抓请求日志。

通过标准：

- 服务端解析支付人 openId，前端不传 openId。
- 真实联调请求必须包含 `sub_appid=wx_test_sp_mch_9901` 对应真实 AppID、`sub_mchid=1900009901` 对应真实子商户号、`sub_openid`。
- 不出现普通直连商户模式的 appid/mchid/openid 口径。

### P0-05 支付查询与支付回调

步骤：

1. 对 `TESTSP9901001` 做支付查询。
2. 在真实联调中完成 0.01 元支付。
3. 微信回调 `/wxmini/pay/notify`。
4. 重放同一个回调。
5. 构造 sub_mchid 或 sub_appid 不匹配的回调。

通过标准：

- 首次成功回调把订单置为已支付，支付记录置成功。
- 重复回调幂等，不重复加账、不重复生成结算。
- sub_mchid/sub_appid 不匹配被拒绝或不更新订单。
- 查询同步只在微信确认 `SUCCESS` 时补偿更新。

### P0-06 退款申请和审核

步骤：

1. 对 `TESTSP9901002` 提交退款申请。
2. 后台审核通过 `TESTSPR9901003A` 场景。
3. 后台拒绝 `TESTSPR9901007R` 场景。
4. 对同一订单重复提交退款。

通过标准：

- 退款申请只允许已支付/已使用/已完成订单。
- 待审核/待微信退款状态下重复申请被拦截。
- 审核通过后状态是待微信退款，不直接把订单改成已退款。
- 拒绝退款不改支付成功状态。

### P0-07 微信退款回调

步骤：

1. 真实联调发起微信退款。
2. 接收 `/wxmini/pay/refund-notify`。
3. 重放退款回调。
4. 构造 sub_mchid/sub_appid 与订单商户不一致回调。

通过标准：

- 微信确认成功后，退款记录状态变已退款，订单变已退款。
- 已到达结算产生反冲或退款处理中状态。
- 重复回调幂等。
- 子商户/子 AppID 不匹配不更新本地状态。

### P0-08 核销和结算

步骤：

1. 商家小程序登录 `test_sp_mch9901_owner/admin123`。
2. 查询 `/wxmini/merchant-mini/order/list`。
3. 核销 `WO9901002` 或新支付成功订单。
4. 查看 `merchant_settlement_record` 和 `order_profit_ledger`。

通过标准：

- 只能核销本商户订单。
- 核销后订单变已使用或已完成。
- 生成结算记录，金额按商户/平台/分销商比例拆分，合计等于实付金额。
- 9905 商户无分销商分账，`distributor_amount=0`。

### P0-09 自动/手动转账

步骤：

1. 后台查看结算状态 `WAITING_T1/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REFUND_PROCESSING/REVERSED`。
2. 对 WAITING_T1 记录发起转账。
3. 接收转账回调。
4. 模拟失败回调。

通过标准：

- 转账记录写入 `platform_transfer_record`。
- target_type 区分 `MERCHANT/DISTRIBUTOR`。
- 成功回调结算变 `ARRIVED`。
- 失败回调结算变 `FAILED` 并保留失败原因。
- 已退款或反冲记录不能再次转账。

### P0-10 后台权限隔离

步骤：

1. `test_sp_platform` 查看商户、订单、退款、结算、转账。
2. `test_sp_dist_9901` 查看同样页面。
3. `test_sp_mch_9901` 查看同样页面。
4. 直接请求其他商户详情、编辑、删除、上下架、退款审核接口。

通过标准：

- 平台可看所有测试数据。
- 分销商 9901 可看 9901/9902，不可看 9903/9904/9905。
- 商户 9901 只能看 9901。
- 跨租户直接访问详情/编辑/删除/退款/上下架接口全部失败。

### P0-11 金额精度

步骤：

1. 下单 `0.01`、`9.99`、`88.88`、`199.99` 商品。
2. 查看接口返回金额。
3. 查看支付请求金额。
4. 查看结算和分账 ledger。

通过标准：

- 数据库金额为元，接口给小程序金额为分。
- `0.01` 返回 1 分。
- `199.99` 返回 19999 分。
- 分账金额四舍五入后合计等于支付金额。
- 不出现浮点误差、负数支付、0 元支付进微信。

### P0-12 安全回归

步骤：

1. 商品名、退款原因、商户描述输入 `<img src=x onerror=alert(1)>`。
2. 列表搜索输入 `' OR 1=1 --`、`${params.dataScopeBiz}`、`1;drop table merchant`。
3. 后台上传非图片文件和超大图片。

通过标准：

- `/mall/*`、`/wxmini/*` 输入被 XSS 过滤或字段级校验拦截。
- SQL 注入 payload 不改变查询范围、不报 SQL 结构错误。
- dataScope 只注入受控参数，不出现原始 SQL 片段。
- 文件上传类型、大小、路径均被限制。

## 5. P1 用例

- 商户后台新增/编辑小程序 AppID 时，Secret 被遮罩后不能在更换 AppID 时沿用旧遮罩值。
- AppID 和 Secret 必须同时为空或同时存在。
- 商户 9903 因 Secret/sub_mchid 缺失，登录或支付应失败并提示配置不完整。
- 商户 9904 因状态停用，商品展示/支付/核销应被拦截。
- 商家小程序商品上架、下架、批量上下架只影响本商户。
- 订单列表筛选 `PENDING_PAY/PAID_UNUSED/USED/COMPLETED/REFUNDING/REFUNDED/CANCELLED` 正确。
- 退款列表只展示当前用户和当前商户上下文内的数据。
- 后台导出时不泄露 Secret、API key、证书路径。

## 6. P2 用例

- 页面空状态、失败文案、加载状态一致。
- 超长商户名、商品名、退款原因展示不撑破页面。
- 列表分页、排序、时间筛选一致。
- 运营报表和结算概览在大数据量下响应可接受。

## 7. 真实微信联调清单

上线前至少完成一组真实闭环。

1. 使用真实商户小程序 AppID/AppSecret 登录。
2. 使用真实服务商证书和 API v3 key 创建 JSAPI 订单。
3. 使用真实子商户号完成 0.01 元支付。
4. 微信支付回调公网 HTTPS 到达后端。
5. 后台发起退款，微信退款回调公网 HTTPS 到达后端。
6. 如启用企业付款/商家转账，完成一笔小额转账或微信官方允许的等价验证。
7. 保存请求号、微信交易号、退款单号、回调日志、后台截图作为上线验收附件。

## 8. 回滚要求

- 数据回滚：执行 seed 顶部 cleanup 段即可删除 9900 段测试数据。
- 配置回滚：生产配置不得包含 `TEST_SECRET_*`、`wx_test_*`、`test_openid_*`。
- 功能回滚：如真实支付失败，先关闭生产入口或回滚后端，不允许继续让用户产生待支付但无法回调的订单。

## 9. 易混点

- `Secret` 指微信小程序 `AppSecret`，用于后端拿 `code` 换 `openid/session_key`，不能下发到小程序前端。
- 服务商模式下，小程序支付主链路用 `sub_appid + sub_mchid + sub_openid`。
- 本项目 JWT 中的 C 端用户 ID 是 `user_info.user_id` 字符串，订单/支付/退款表里的 `user_id` 是 `user_info.id` Long 主键。
- `X-Wx-AppId` 是租户识别关键请求头，不能由后台随便信任订单中的商户 ID 替代。
