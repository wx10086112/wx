# merchant_app 商家端小程序 — 后端接口对接文档

本文档面向后端开发人员，说明商家端小程序的全部接口调用约定、请求参数和期望的响应数据结构。

## 一、通信约定

### 基础地址

前端通过 `app.js` 中的 `baseUrl` 配置后端地址，默认 `http://localhost:8080`。所有接口路径以 `/wxmini/merchant-mini/` 为前缀。

### 鉴权方式

前端使用请求头 `Wx-Authorization` 传递 JWT token，格式为 `Bearer {token}`：

```
Wx-Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx
```

注意：与 C 端小程序的鉴权格式不同（C 端不带 Bearer 前缀），商家端使用标准 Bearer 格式。

token 存储在 `wx.setStorageSync('merchantToken', token)`，员工信息存储在 `wx.setStorageSync('merchantStaffUser', {...})`。

### 统一响应格式

前端按以下结构解析所有接口响应。当 `code` 为 `200` 或 `0` 时，前端直接取 `data` 字段作为业务结果（promise resolve 的就是 `data`，不是整个响应体）：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 金额约定

所有金额字段单位为 **分**（整数）。前端展示时除以 100 转为元，保留两位小数。

### 权限体系

商家端有独立的员工权限体系，登录后返回 `permissionCodes` 数组。前端根据权限码控制页面和按钮的可见性，后端每个接口也需校验对应权限。

| 权限码 | 说明 | 适用接口 |
|--------|------|----------|
| `stats.view` | 查看经营数据 | 工作台概览 |
| `order.manage` | 订单处理 | 订单列表、详情、接单、拒单、发货、完成、取消 |
| `verify.scan` | 扫码核销 | 核销接口 |
| `verify.manual` | 手动核销 | 核销接口 |
| `verify.record` | 核销记录 | 核销记录列表 |
| `goods.manage` | 商品管理 | 商品 CRUD、上下架、图片上传 |
| `store.manage` | 门店设置 | 门店信息读写 |
| `staff.manage` | 员工权限 | 员工列表、权限修改 |
| `finance.manage` | 财务收益 | 财务概览、提现申请 |

角色模板：

| 角色 | roleKey | 默认权限 |
|------|---------|----------|
| 店长 | `manager` | 全部 9 项 |
| 店员 | `clerk` | `stats.view`、`order.manage`、`verify.scan`、`verify.manual`、`verify.record` |

---

## 二、接口清单

### 2.1 认证模块

#### `POST /wxmini/merchant-mini/auth/login` — 员工登录

请求体：

```json
{
  "roleKey": "manager"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleKey | string | 否 | `manager` 或 `clerk`，用于演示切换角色 |

响应 `data`：

```json
{
  "staffId": 1,
  "name": "林店长",
  "phone": "13800001111",
  "roleKey": "manager",
  "roleName": "店长",
  "permissions": ["stats.view", "order.manage", "verify.scan", "verify.manual", "verify.record", "goods.manage", "store.manage", "staff.manage", "finance.manage"],
  "apiToken": "eyJhbGciOiJIUzI1NiJ9.xxxxx"
}
```

| 字段 | 说明 |
|------|------|
| `apiToken` | JWT token，前端存入 `merchantToken` |
| `permissions` | 权限码数组，前端控制页面和按钮可见性 |

---

### 2.2 工作台模块

#### `GET /wxmini/merchant-mini/workbench/overview` — 工作台概览

需权限：`stats.view`

响应 `data`：

```json
{
  "pendingAcceptCount": 1,
  "pendingVerifyCount": 2,
  "shippingCount": 1,
  "completedCount": 1,
  "refundingCount": 1,
  "onShelfCount": 3,
  "todaySalesAmount": 87000,
  "abnormalCount": 2
}
```

| 字段 | 说明 |
|------|------|
| `pendingAcceptCount` | 待接单数 |
| `pendingVerifyCount` | 待核销数 |
| `shippingCount` | 配送中数 |
| `completedCount` | 已完成数 |
| `refundingCount` | 退款中数 |
| `onShelfCount` | 在售商品数 |
| `todaySalesAmount` | 今日销售额（分） |
| `abnormalCount` | 异常订单数（退款中 + 已拒单） |

---

### 2.3 订单模块

#### `GET /wxmini/merchant-mini/order/list` — 订单列表

需权限：`order.manage`

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 按状态筛选 |

响应 `data`（数组）：

```json
[
  {
    "orderId": 1,
    "orderNo": "M202605090001",
    "goodsId": 101,
    "title": "芳香舒压 SPA 90 分钟",
    "customerName": "王女士",
    "customerPhone": "138****2201",
    "quantity": 1,
    "payAmount": 18800,
    "status": "PENDING_VERIFY",
    "orderType": "GROUPON",
    "createTime": 1778269200000,
    "payTime": 1778269800000,
    "writeOffCode": "LY8012",
    "acceptTime": null,
    "shipTime": null,
    "verifyTime": null,
    "verifyStaffName": null,
    "refundReason": null,
    "rejectReason": null,
    "cancelReason": null,
    "deliveryAddress": null,
    "deliveryPhone": null,
    "remark": ""
  }
]
```

| 字段 | 说明 |
|------|------|
| `orderNo` | 订单号，唯一 |
| `orderType` | 订单类型：`GROUPON` 团购到店 / `TAKEAWAY` 外卖配送 |
| `writeOffCode` | 核销码，团购订单在 PENDING_VERIFY 状态时有值 |
| `customerName` / `customerPhone` | 客户信息（脱敏） |
| `deliveryAddress` / `deliveryPhone` | 外卖订单的配送地址和联系电话 |
| `remark` | 客户备注 |
| 各时间字段 | 均为时间戳毫秒 |

---

#### `GET /wxmini/merchant-mini/order/detail/{orderNo}` — 订单详情

需权限：`order.manage`

响应 `data`：同列表单条结构。

---

#### `POST /wxmini/merchant-mini/order/write-off/{code}` — 核销订单

需权限：`verify.scan` 或 `verify.manual`

后端实现要点：
- 根据 `writeOffCode` 或 `orderNo` 查找订单
- 校验订单状态必须为 `PENDING_VERIFY`
- 校验团购券是否在有效期内
- 核销成功后流转为 `COMPLETED`，记录核销时间和操作员工
- 核销失败需返回明确的失败原因

响应 `data`：

```json
{
  "success": true,
  "message": "核销成功",
  "order": { ... }
}
```

失败时：

```json
{
  "success": false,
  "message": "该订单已核销完成",
  "order": { ... }
}
```

---

#### `POST /wxmini/merchant-mini/order/accept/{orderNo}` — 接单

需权限：`order.manage`

仅 `PENDING_ACCEPT` 状态可接单。接单后流转为 `ACCEPTED`。

响应 `data`：返回更新后的订单对象。

---

#### `POST /wxmini/merchant-mini/order/reject/{orderNo}` — 拒单

需权限：`order.manage`

请求体：

```json
{
  "reason": "商品已售罄"
}
```

仅 `PENDING_ACCEPT` 状态可拒单。拒单后流转为 `REJECTED`。

---

#### `POST /wxmini/merchant-mini/order/ship/{orderNo}` — 发货

需权限：`order.manage`

仅 `ACCEPTED` 状态可发货。发货后流转为 `SHIPPING`。

---

#### `POST /wxmini/merchant-mini/order/complete/{orderNo}` — 确认送达

需权限：`order.manage`

仅 `SHIPPING` 状态可确认完成。完成后流转为 `COMPLETED`。

---

#### `POST /wxmini/merchant-mini/order/cancel/{orderNo}` — 商家取消订单

需权限：`order.manage`

请求体：

```json
{
  "reason": "商品缺货"
}
```

`COMPLETED`、`CANCELLED`、`REFUNDED` 状态不可取消。

---

#### `POST /wxmini/merchant-mini/order/refund/approve/{orderNo}` — 同意退款

需权限：`order.manage`

仅 `REFUNDING` 状态可操作。同意后流转为 `REFUNDED`，需同步调用微信退款 API。

---

#### `POST /wxmini/merchant-mini/order/refund/reject/{orderNo}` — 拒绝退款

需权限：`order.manage`

请求体：

```json
{
  "reason": "商品已核销使用"
}
```

仅 `REFUNDING` 状态可操作。拒绝后流转回 `PENDING_VERIFY`。

---

### 2.4 核销记录模块

#### `GET /wxmini/merchant-mini/verify/record/list` — 核销记录列表

需权限：`verify.record`、`verify.scan` 或 `verify.manual`

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | `SUCCESS` / `FAILED`，按结果筛选 |

响应 `data`（数组）：

```json
[
  {
    "recordId": 1,
    "orderNo": "M202605080003",
    "goodsId": 101,
    "title": "芳香舒压 SPA 90 分钟",
    "inputCode": "LY7710",
    "writeOffCode": "LY7710",
    "customerName": "孙女士",
    "customerPhone": "136****1103",
    "payAmount": 18800,
    "status": "SUCCESS",
    "verifyTime": 1778190000000,
    "verifyStaffId": 2,
    "verifyStaffName": "周店员",
    "failureReason": null
  }
]
```

| 字段 | 说明 |
|------|------|
| `status` | `SUCCESS` 核销成功 / `FAILED` 核销失败 |
| `inputCode` | 用户输入/扫码的原始码 |
| `failureReason` | 失败原因，如"未找到对应订单"、"该订单已核销完成"、"团购券已过有效期" |

---

### 2.5 商品模块

#### `GET /wxmini/merchant-mini/goods/list` — 商品列表

需权限：`goods.manage`

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | `ON_SHELF` / `OFF_SHELF`，按上下架状态筛选 |

响应 `data`（数组）：

```json
[
  {
    "goodsId": 101,
    "title": "芳香舒压 SPA 90 分钟",
    "subtitle": "精油舒缓 + 热敷放松 + 独立房间",
    "imageUrl": "https://xxx/goods.jpg",
    "price": 19800,
    "originalPrice": 39800,
    "stock": 88,
    "sales": 2651,
    "validPeriod": "2026-05-01 至 2026-06-30",
    "verifyNotice": "到店出示核销码即可使用",
    "status": "ON_SHELF",
    "categoryName": "SPA轻养",
    "sort": 1
  }
]
```

| 字段 | 说明 |
|------|------|
| `goodsId` | 商品 ID |
| `price` / `originalPrice` | 团购价 / 原价（分） |
| `stock` | 库存数量 |
| `sales` | 已售数量 |
| `validPeriod` | 有效期文案，格式 "YYYY-MM-DD 至 YYYY-MM-DD" |
| `verifyNotice` | 核销提示文案 |
| `status` | `ON_SHELF` 上架 / `OFF_SHELF` 下架 |
| `sort` | 排序权重 |

---

#### `POST /wxmini/merchant-mini/goods/save` — 新增/编辑商品

需权限：`goods.manage`

请求体：

```json
{
  "goodsId": null,
  "title": "新套餐名称",
  "subtitle": "套餐副标题",
  "imageUrl": "https://xxx/goods.jpg",
  "price": 19800,
  "originalPrice": 39800,
  "stock": 100,
  "validPeriod": "2026-05-01 至 2026-06-30",
  "verifyNotice": "到店出示核销码即可使用",
  "categoryName": "SPA轻养",
  "sort": 1
}
```

`goodsId` 为 `null` 或不传时为新增，有值时为编辑。

响应 `data`：返回保存后的商品对象。

---

#### `PUT /wxmini/merchant-mini/goods/status` — 商品上下架

需权限：`goods.manage`

请求体：

```json
{
  "goodsId": 101,
  "status": "OFF_SHELF"
}
```

---

#### `POST /wxmini/merchant-mini/goods/image/upload` — 商品图片上传

需权限：`goods.manage`

请求：`multipart/form-data`，`file` 字段。

响应 `data`：

```json
{
  "url": "https://xxx/uploaded-goods-image.jpg",
  "fileName": "uploaded-goods-image.jpg"
}
```

---

### 2.6 门店设置模块

#### `GET /wxmini/merchant-mini/store/profile` — 获取门店信息

需权限：`store.manage`

响应 `data`：

```json
{
  "merchantId": 1,
  "storeId": 1,
  "brandName": "蓝屿轻养生活馆",
  "storeName": "蓝屿轻养·国贸旗舰店",
  "shortName": "国贸店",
  "brandSlogan": "单店团购到店核销运营端",
  "notice": "支持扫码核销、手动核销",
  "businessHours": "10:00-22:00",
  "phone": "010-88886601",
  "address": "北京市朝阳区建国路88号嘉里中心B1",
  "latitude": 39.9087,
  "longitude": 116.4591,
  "avatar": "https://xxx/avatar.jpg",
  "coverImage": "https://xxx/cover.jpg",
  "serviceTags": ["到店核销", "团购套餐", "营业中"],
  "bannerTitles": ["午市轻养专场", "肩颈护理次卡"],
  "businessStatus": true,
  "supportRefund": true,
  "supportBooking": true,
  "deliveryRange": 5,
  "deliveryFee": 500,
  "freeDeliveryAmount": 5000
}
```

| 字段 | 说明 |
|------|------|
| `businessStatus` | `true` 营业中 / `false` 休息中 |
| `supportRefund` | 是否支持退款 |
| `supportBooking` | 是否支持预约 |
| `deliveryRange` | 配送范围（公里） |
| `deliveryFee` | 基础配送费（分） |
| `freeDeliveryAmount` | 免配送费起送金额（分） |

---

#### `PUT /wxmini/merchant-mini/store/profile` — 更新门店信息

需权限：`store.manage`

请求体：同获取接口返回结构，传需要修改的字段即可。

---

### 2.7 员工模块

#### `GET /wxmini/merchant-mini/staff/list` — 员工列表

需权限：`staff.manage`

响应 `data`（数组）：

```json
[
  {
    "staffId": 1,
    "name": "林店长",
    "phone": "13800001111",
    "roleKey": "manager",
    "roleName": "店长",
    "status": "ACTIVE",
    "permissions": ["stats.view", "order.manage", "verify.scan", "verify.manual", "verify.record", "goods.manage", "store.manage", "staff.manage", "finance.manage"]
  }
]
```

| 字段 | 说明 |
|------|------|
| `roleKey` | `manager` 店长 / `clerk` 店员 |
| `status` | `ACTIVE` 启用 / `INACTIVE` 停用 |
| `permissions` | 权限码数组 |

---

#### `POST /wxmini/merchant-mini/staff/add` — 添加员工

需权限：`staff.manage`

请求体：

```json
{
  "name": "新员工",
  "phone": "13800003333",
  "roleKey": "clerk",
  "permissions": ["stats.view", "order.manage", "verify.scan", "verify.manual", "verify.record"]
}
```

---

#### `PUT /wxmini/merchant-mini/staff/update` — 编辑员工

需权限：`staff.manage`

请求体：

```json
{
  "staffId": 2,
  "name": "周店员",
  "phone": "13800002222",
  "status": "ACTIVE",
  "permissions": ["stats.view", "order.manage", "verify.scan", "verify.manual", "verify.record"]
}
```

---

#### `PUT /wxmini/merchant-mini/staff/permission` — 修改员工权限

需权限：`staff.manage`

请求体：

```json
{
  "staffId": 2,
  "permissions": ["stats.view", "order.manage", "verify.scan"]
}
```

---

### 2.8 财务模块

#### `GET /wxmini/merchant-mini/finance/overview` — 财务概览

需权限：`finance.manage`

响应 `data`：

```json
{
  "todayIncomeAmount": 32600,
  "monthIncomeAmount": 156800,
  "pendingSettleAmount": 45000,
  "withdrawableAmount": 111800,
  "platformFeeAmount": 17400,
  "completedOrderCount": 8,
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
  ],
  "withdrawList": [
    {
      "withdrawId": 1,
      "amount": 50000,
      "status": "PROCESSING",
      "applyTime": 1778200000000,
      "remark": "商家端在线提现申请"
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| `todayIncomeAmount` | 今日收入（分），仅计算商家分成部分 |
| `monthIncomeAmount` | 本月收入（分） |
| `pendingSettleAmount` | 待结算金额（分），T+1 结算 |
| `withdrawableAmount` | 可提现余额（分），已结算金额 - 冻结提现金额 |
| `platformFeeAmount` | 平台佣金累计（分） |
| `ledgerList[].merchantAmount` | 商家实收 = 订单金额 × 90% |
| `ledgerList[].platformFeeAmount` | 平台佣金 = 订单金额 × 10% |
| `ledgerList[].status` | `SETTLED` 已结算 / `PENDING` 待结算 |
| `withdrawList[].status` | `PROCESSING` 处理中 / `SUCCESS` 已到账 / `FAILED` 失败 |

---

#### `POST /wxmini/merchant-mini/finance/withdraw` — 申请提现

需权限：`finance.manage`

请求体：

```json
{
  "amount": 50000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| amount | long | 提现金额（分），不能超过可提现余额 |

响应 `data`：

```json
{
  "success": true,
  "message": "提现申请已提交",
  "record": {
    "withdrawId": 2,
    "amount": 50000,
    "status": "PROCESSING",
    "applyTime": 1778300000000,
    "remark": "商家端在线提现申请，等待平台/微信支付出款处理"
  }
}
```

---

### 2.9 营销模块

#### `GET /wxmini/merchant-mini/marketing/coupon/list` — 优惠券列表

响应 `data`（数组）：

```json
[
  {
    "couponId": 1,
    "couponName": "到店立减 10 元",
    "amount": 1000,
    "thresholdAmount": 9900,
    "totalCount": 100,
    "usedCount": 23,
    "status": "ACTIVE",
    "validStartTime": "2026-05-01",
    "validEndTime": "2026-06-30"
  }
]
```

---

#### `POST /wxmini/merchant-mini/marketing/coupon/save` — 创建/编辑优惠券

请求体：

```json
{
  "couponId": null,
  "couponName": "新优惠券",
  "amount": 2000,
  "thresholdAmount": 19900,
  "totalCount": 200,
  "validStartTime": "2026-05-01",
  "validEndTime": "2026-06-30"
}
```

---

#### `PUT /wxmini/merchant-mini/marketing/coupon/status` — 启停优惠券

请求体：

```json
{
  "couponId": 1,
  "status": "INACTIVE"
}
```

---

#### `GET /wxmini/merchant-mini/marketing/promotion/list` — 满减活动列表

#### `POST /wxmini/merchant-mini/marketing/promotion/save` — 创建/编辑满减活动

---

## 三、订单状态机

```
         ┌────────────────┐
         │ PENDING_ACCEPT │  外卖订单用户支付后
         │    (待接单)     │
         └───────┬────────┘
      ┌──────────┼──────────┐
      ▼                     ▼
┌──────────┐          ┌──────────┐
│ REJECTED │          │ ACCEPTED │
│  (已拒单) │          │  (已接单) │
└──────────┘          └─────┬────┘
                            ▼
                      ┌──────────┐
                      │ SHIPPING │
                      │  (配送中) │
                      └─────┬────┘
                            ▼
                      ┌───────────┐
                      │ COMPLETED │
                      │  (已完成)  │
                      └───────────┘


         ┌────────────────┐
         │ PENDING_VERIFY │  团购订单用户支付后
         │   (待核销)     │
         └───────┬────────┘
                 ▼
           ┌───────────┐
           │ COMPLETED │  核销成功
           │  (已完成)  │
           └───────────┘


退款流程（任意已支付状态均可触发）：
         ┌───────────┐
         │ REFUNDING │  用户申请退款
         │  (退款中)  │
         └─────┬─────┘
      ┌────────┼────────┐
      ▼                 ▼
┌──────────┐    ┌──────────────┐
│ REFUNDED │    │ PENDING_VERIFY│  拒绝退款，团购订单恢复
│  (已退款) │    │  (待核销)     │
└──────────┘    └──────────────┘
```

| 状态值 | 前端展示 | 前端可执行操作 |
|--------|----------|----------------|
| `PENDING_ACCEPT` | 待接单 | 接单、拒单 |
| `ACCEPTED` | 已接单 | 发货 |
| `SHIPPING` | 配送中 | 确认送达 |
| `PENDING_VERIFY` | 待核销 | 扫码/手动核销 |
| `COMPLETED` | 已完成 | 查看详情 |
| `REJECTED` | 已拒单 | 查看详情 |
| `REFUNDING` | 退款中 | 同意退款、拒绝退款 |
| `REFUNDED` | 已退款 | 查看详情 |
| `CANCELLED` | 已取消 | 查看详情 |

---

## 四、前端页面与接口调用关系

所有页面均已实现 **API 优先 + localStorage 降级** 模式：先调后端接口，接口不可用时自动回退到本地 mock 数据，后端未就绪也可完整演示。切换为真实环境只需修改 `app.js` 中的 `baseUrl`，无需改动任何页面代码。

| 前端页面 | 调用的接口 | 降级策略 |
|----------|-----------|----------|
| 登录 `pages/login` | `POST /auth/login` | mock 员工信息 |
| 工作台 `pages/workbench` | `GET /workbench/overview` | localStorage 统计 |
| 订单列表 `pages/order` | `GET /order/list` + 全部操作接口 | localStorage |
| 订单详情 `pages/order-detail` | `GET /order/detail/{no}` + 操作接口 | localStorage |
| 核销台 `pages/verify` | `POST /order/write-off/{code}` | localStorage 核销逻辑 |
| 核销记录 `pages/verify-records` | `GET /verify/record/list` | localStorage |
| 商品列表 `pages/goods` | `GET /goods/list`、`PUT /goods/status` | localStorage |
| 商品编辑 `pages/goods-edit` | `POST /goods/save`、`POST /goods/image/upload` | localStorage |
| 门店设置 `pages/store` | `GET /store/profile`、`PUT /store/profile` | localStorage |
| 员工管理 `pages/staff` | `GET /staff/list`、`POST /staff/add`、`PUT /staff/update`、`PUT /staff/permission` | localStorage |
| 财务收益 `pages/finance` | `GET /finance/overview`、`POST /finance/withdraw` | localStorage 计算 |
| 营销活动 `pages/marketing` | `GET /marketing/coupon/list`、`POST /marketing/coupon/save`、`PUT /marketing/coupon/status` | localStorage |
| **入驻申请 `pages/apply`** | `POST /apply/submit`、`GET /apply/status`、`POST /common/upload` | localStorage 状态 |

---

## 五、扫码核销流程

### 商家端核销实现

商家端核销台（`pages/verify`）已实现两种核销方式：

**扫码核销**：调用 `wx.scanCode()` 扫描 C 端用户展示的二维码，二维码内容为订单的 `writeOffCode` 字符串（如 `LY8012`）。

**手动核销**：商家手动输入核销码或订单号，调用同一个后端接口。

两种方式均调用 `POST /wxmini/merchant-mini/order/write-off/{code}`，后端根据传入的 `code` 查找订单并完成核销。

### 端到端核销流程

```
C 端用户                    商家端                      后端
   │                         │                          │
   │ 支付成功 → 订单状态      │                          │
   │ PAID_UNUSED              │                          │
   │ 系统生成 writeOffCode    │                          │
   │ = "LY8012"              │                          │
   │                         │                          │
   │ Canvas 生成二维码 ───────→│                          │
   │ 二维码内容 = "LY8012"    │                          │
   │                         │                          │
   │ 到店出示二维码           │                          │
   │ ────────────────────────→│ wx.scanCode()            │
   │                         │ 读取结果 = "LY8012"       │
   │                         │                          │
   │                         │ POST /write-off/LY8012 ──→│
   │                         │                          │ 查找 writeOffCode
   │                         │                          │ = "LY8012" 的订单
   │                         │                          │
   │                         │                          │ 校验状态 =
   │                         │                          │ PENDING_VERIFY
   │                         │                          │
   │                         │                          │ 校验有效期
   │                         │                          │
   │                         │                          │ 更新为 COMPLETED
   │                         │                          │ 记录 verifyTime
   │                         │                          │ + verifyStaffId
   │                         │                          │ + verifyStaffName
   │                         │                          │
   │                         │←──── { success: true } ──│
   │                         │ toast "核销成功"           │
   │                         │ 记录写入核销记录           │
   │                         │                          │
   │←── C 端下次刷新订单 ─────│                          │
   │     状态变为 COMPLETED   │                          │
```

### 后端核销接口校验清单

| 校验项 | 失败时返回的 failureReason |
|--------|---------------------------|
| 核销码对应订单是否存在 | "未找到对应订单" |
| 订单状态是否为 `PENDING_VERIFY` | "该订单已核销完成" 或 "当前订单状态不可核销" |
| 团购券是否在有效期内 | "团购券已过有效期" |

### 核销记录

每次核销（无论成功或失败）均需写入核销记录，前端通过 `GET /verify/record/list` 展示。记录需包含：
- 操作员工 ID 和姓名（`verifyStaffId`、`verifyStaffName`）
- 核销时间（`verifyTime`）
- 输入的原始码（`inputCode`）
- 核销结果（`status`: `SUCCESS` / `FAILED`）
- 失败原因（`failureReason`）

---

## 六、商家入驻审核

### 入驻申请页面

登录页底部新增"商家入驻申请"入口，引导未入驻商家提交资质材料。

**页面路径：** `pages/apply/apply`

**提交的材料：**

| 字段 | 必填 | 说明 |
|------|------|------|
| storeName | 是 | 店铺名称 |
| contactName | 是 | 联系人姓名 |
| contactPhone | 是 | 联系电话 |
| address | 否 | 店铺地址 |
| licenseImage | 是 | 营业执照照片 |
| idCardFrontImage | 是 | 法人身份证正面 |
| idCardBackImage | 否 | 法人身份证反面 |
| storeFrontImage | 是 | 门头照片 |

**审核状态流转：**

| 状态 | 前端展示 | 说明 |
|------|----------|------|
| `none` | 显示申请表单 | 初始状态 |
| `pending` | 显示"审核中"等待页 | 已提交，等待平台审核 |
| `approved` | 通过后跳转登录 | 平台审核通过，开通运营权限 |
| `rejected` | 显示拒绝原因 + 重新提交按钮 | 审核未通过 |

**后端对接需求：**

| 接口 | 说明 |
|------|------|
| `POST /wxmini/merchant-mini/apply/submit` | 提交入驻申请（含图片文件上传） |
| `GET /wxmini/merchant-mini/apply/status` | 查询审核状态 |
| `POST /wxmini/common/upload` | 图片上传（复用通用上传接口） |

后端需在平台管理后台实现审核功能，审核通过后自动创建商家账号并通知商家。

---

## 七、与 C 端接口差异对照

| 对比项 | C 端 (wx_app) | 商家端 (merchant_app) |
|--------|---------------|----------------------|
| API 前缀 | `/wxmini/` | `/wxmini/merchant-mini/` |
| Token 格式 | 原始 token（无 Bearer） | `Bearer {token}` |
| Token 存储 key | `token` | `merchantToken` |
| 响应解析 | 取整个 `{code, msg, data}` | 直接取 `data` 字段 |
| 订单状态 | PENDING_PAY → PAID_UNUSED → USED_COMPLETED | PENDING_ACCEPT/ACCEPTED/SHIPPING/PENDING_VERIFY → COMPLETED |
| 金额字段 | `payAmount`、`price` | `payAmount` |
| 核销码字段 | `writeOffCode` | `writeOffCode` |

---

## 八、开发对接顺序建议

1. **登录鉴权**：实现 `POST /auth/login`，返回 `apiToken` + `permissions`
2. **工作台**：实现 `GET /workbench/overview`，前端首页即可展示数据
3. **订单 + 核销**：实现订单列表、详情、核销，这是核心链路
4. **商品管理**：实现商品 CRUD + 图片上传
5. **门店设置**：实现门店信息读写
6. **员工管理**：实现员工 CRUD + 权限修改
7. **财务**：实现财务概览 + 提现
8. **营销**：实现优惠券和满减活动管理
