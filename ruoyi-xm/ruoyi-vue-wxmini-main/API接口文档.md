# 商城 API 接口文档

> 基于 Spring Boot 后端 Controller 代码自动生成，涵盖管理后台及小程序端全部接口。

---

## 一、商城商家管理

**基础路径**: `/mall/merchant`
**Controller**: `MallMerchantController` / `MallMerchantAuditController` / `MallMerchantFlowController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/merchant/list` | 查询商户列表 | `mall:merchant:list` |
| GET | `/mall/merchant/{id}` | 查询商户详情 | `mall:merchant:query` |
| POST | `/mall/merchant` | 新增商户 | `mall:merchant:add` |
| PUT | `/mall/merchant` | 修改商户 | `mall:merchant:edit` |
| DELETE | `/mall/merchant/{ids}` | 删除商户（批量） | `mall:merchant:remove` |
| GET | `/mall/merchant/audit/list` | 查询审核列表 | `mall:merchant:audit` |
| PUT | `/mall/merchant/audit/{id}/{status}` | 商户审核（通过/拒绝） | `mall:merchant:audit` |
| GET | `/mall/merchant/flow/list` | 商家流水列表 | `mall:merchant:list` |

---

## 二、商城商品管理

**基础路径**: `/mall/product`
**Controller**: `MallProductController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/product/list` | 查询商品列表 | `mall:product:list` |
| GET | `/mall/product/{id}` | 查询商品详情 | `mall:product:query` |
| POST | `/mall/product` | 新增商品 | `mall:product:add` |
| PUT | `/mall/product` | 修改商品 | `mall:product:edit` |
| DELETE | `/mall/product/{ids}` | 删除商品（批量） | `mall:product:remove` |

---

## 三、商城订单管理

**基础路径**: `/mall/order` / `/mall/after-sale`
**Controller**: `MallOrderController` / `MallOrderExtendController` / `MallAfterSaleController`

### 3.1 订单管理

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/order/list` | 查询订单列表 | `mall:order:list` |
| GET | `/mall/order/{id}` | 查询订单详情（含订单项） | `mall:order:query` |
| PUT | `/mall/order` | 修改订单 | `mall:order:edit` |
| GET | `/mall/order/abnormal/list` | 异常订单列表（状态=5） | `mall:order:list` |
| GET | `/mall/order/after-sale/list` | 售后订单列表（状态=4） | `mall:order:list` |

### 3.2 售后管理

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/after-sale/list` | 查询退款/售后记录列表 | `mall:order:list` |
| GET | `/mall/after-sale/{id}` | 查询售后详情 | `mall:order:query` |
| POST | `/mall/after-sale/handle/{id}/{status}` | 处理售后申请（同意/拒绝） | `mall:order:edit` |

---

## 四、商城财务管理

**基础路径**: `/mall/finance`
**Controller**: `MallFinanceController` / `MallFinanceExtendController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/finance/platform-flow/list` | 平台流水列表 | `mall:finance:list` |
| GET | `/mall/finance/profit-share/list` | 利润分成列表 | `mall:finance:list` |
| GET | `/mall/finance/withdraw/list` | 提现记录列表 | `mall:finance:list` |
| GET | `/mall/finance/merchant-flow/list` | 商户流水列表 | `mall:finance:list` |
| GET | `/mall/finance/income/stats` | 收入统计（总佣金/今日/本月/已提现） | `mall:finance:list` |
| GET | `/mall/finance/report` | 财务报表（按月汇总） | `mall:finance:list` |
| POST | `/mall/finance/withdraw/approve/{id}/{status}` | 审批提现申请 | `mall:finance:edit` |

---

## 五、商城工作台（管理后台 Dashboard）

**基础路径**: `/mall/dashboard`
**Controller**: `MallDashboardExtendController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/dashboard/stats` | 工作台统计概览 | `mall:dashboard:list` |
| GET | `/mall/dashboard/trend` | 趋势数据 | `mall:dashboard:list` |
| GET | `/mall/dashboard/merchant-rank` | 商家排行 | `mall:dashboard:list` |
| GET | `/mall/dashboard/sales-stats` | 销售统计（总销售额/订单量/均价/转化率） | `mall:dashboard:list` |
| GET | `/mall/dashboard/order-stats` | 订单统计（按状态/近30天每日） | `mall:dashboard:list` |

---

## 六、商城用户管理

**基础路径**: `/mall/user`
**Controller**: `MallUserController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/mall/user/list` | 查询小程序用户列表 | `mall:user:list` |

---

## 七、微信小程序（C端用户）

### 7.1 微信消息网关

**基础路径**: `/wxmini/portal/{appid}`
**Controller**: `WxPortalController`（ruoyi-mall-user 模块）

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/wxmini/portal/{appid}` | 微信服务器验证（签名校验） | 无（公开） |
| POST | `/wxmini/portal/{appid}` | 接收微信服务器消息推送 | 无（公开） |

### 7.2 用户登录

**基础路径**: `/wxmini`
**Controller**: `WxLoginController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/wxmini/login` | 微信小程序登录（code换openid，自动注册，返回JWT） | 无（公开） |

### 7.3 用户信息

**基础路径**: `/wxmini/user`
**Controller**: `WxMaUserController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/wxmini/user/info` | 获取/同步微信用户信息 | 无（JWT鉴权） |
| GET | `/wxmini/user/phone` | 获取/同步用户绑定手机号 | 无（JWT鉴权） |

### 7.4 微信支付

**基础路径**: `/wxmini/pay`
**Controller**: `WxPayController`

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| POST | `/wxmini/pay/order/create` | 创建支付订单（统一下单） | 无（JWT鉴权） |
| GET | `/wxmini/pay/order/query` | 查询支付订单状态 | 无（JWT鉴权） |
| POST | `/wxmini/pay/notify` | 微信支付回调通知 | 无（公开） |

---

## 八、商家小程序（商家端）

### 8.1 微信消息网关

**基础路径**: `/wxmini/portal/{appid}`
**Controller**: `WxPortalController`（ruoyi-wxmini 模块）

| 方法 | 完整URL | 说明 | 权限标识 |
|------|---------|------|----------|
| GET | `/wxmini/portal/{appid}` | 微信服务器验证 | 无（公开） |
| POST | `/wxmini/portal/{appid}` | 接收微信消息推送 | 无（公开） |

### 8.2 商家端业务接口

**基础路径**: `/wxmini/merchant-mini`
**Controller**: `MerchantMiniController`

权限校验方式：通过 `WxMiniUserContext` 的商家员工角色与自定义权限码（非 `@PreAuthorize`）。

| 方法 | 完整URL | 说明 | 所需权限码 |
|------|---------|------|-----------|
| POST | `/wxmini/merchant-mini/auth/login` | 商家端登录 | 无（公开） |
| GET | `/wxmini/merchant-mini/workbench/overview` | 工作台概览 | `stats.view` |
| GET | `/wxmini/merchant-mini/order/list` | 订单列表 | `order.manage` |
| GET | `/wxmini/merchant-mini/order/detail/{orderNo}` | 订单详情 | `order.manage` |
| POST | `/wxmini/merchant-mini/order/write-off/{code}` | 核销订单 | `verify.scan` / `verify.manual` |
| GET | `/wxmini/merchant-mini/verify/record/list` | 核销记录列表 | `verify.record` / `verify.scan` / `verify.manual` |
| GET | `/wxmini/merchant-mini/goods/list` | 商品列表 | `goods.manage` |
| POST | `/wxmini/merchant-mini/goods/save` | 保存商品（新增/编辑） | `goods.manage` |
| PUT | `/wxmini/merchant-mini/goods/status` | 修改商品状态（上架/下架） | `goods.manage` |
| POST | `/wxmini/merchant-mini/goods/image/upload` | 上传商品图片 | `goods.manage` |
| GET | `/wxmini/merchant-mini/store/profile` | 获取店铺资料 | `store.manage` |
| PUT | `/wxmini/merchant-mini/store/profile` | 修改店铺资料 | `store.manage` |
| GET | `/wxmini/merchant-mini/staff/list` | 员工列表 | `staff.manage` |
| PUT | `/wxmini/merchant-mini/staff/permission` | 修改员工权限 | `staff.manage` |
| GET | `/wxmini/merchant-mini/finance/overview` | 财务概览 | `finance.manage` |
| POST | `/wxmini/merchant-mini/finance/withdraw` | 申请提现 | `finance.manage` |

---

## 接口汇总统计

| 模块 | 接口数量 |
|------|---------|
| 商城商家管理 | 8 |
| 商城商品管理 | 5 |
| 商城订单管理 | 8 |
| 商城财务管理 | 7 |
| 商城工作台 | 5 |
| 商城用户管理 | 1 |
| 微信小程序（C端） | 6 |
| 商家小程序 | 17 |
| **合计** | **57** |
