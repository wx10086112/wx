# wx_app C 端小程序 — 后端接口对接文档

本文档面向后端开发人员，说明前端小程序的全部接口调用约定、请求参数和期望的响应数据结构。

## 一、通信约定

### 基础地址

前端通过 `app.js` 中的 `baseUrl` 配置后端地址，默认 `http://localhost:8080`。所有接口路径以 `/wxmini/` 为前缀。

### 鉴权方式

前端使用自定义请求头 `Wx-Authorization` 传递 JWT token，格式为原始 token 字符串（非 Bearer 前缀），与若依框架默认的 `Authorization` 隔离。

```
Wx-Authorization: eyJhbGciOiJIUzI1NiJ9.xxxxx
```

后端需在 `/wxmini/**` 路径下配置独立的 JWT 过滤器（已有 `WxMiniJwtFilter`），验证 token 并将用户 ID 写入 `WxMiniUserContext` 线程上下文。

**白名单路径**（不需要 token）：
- `GET /wxmini/login`
- `POST /wxmini/login/quick`
- `POST /wxmini/user/phone/bind`
- `GET /wxmini/template/config`

### 统一响应格式

前端按以下结构解析所有接口响应：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

| 字段 | 说明 |
|------|------|
| `code` | `200` 或 `0` 表示成功，`401` 表示 token 过期（前端会清除登录态），其他为业务错误 |
| `msg` | 错误提示文案，前端会直接 toast 展示 |
| `data` | 业务数据，具体结构见各接口说明 |

### 金额约定

所有金额字段单位为 **分**（整数）。前端展示时除以 100 转为元，保留两位小数。

示例：`19800` → 前端展示为 `¥198.00`

---

## 二、接口清单

### 2.1 用户模块

#### `GET /wxmini/login` — 微信登录

前端调用时机：用户点击登录按钮，`wx.login()` 获取 code 后调用。

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appid | string | 是 | 小程序 AppID |
| code | string | 是 | wx.login() 返回的临时凭证 |

响应 `data`：

```json
{
  "openId": "oK1qf5XXXXXXXXXXXXXXXX",
  "userId": "uuid-string",
  "userName": "微信用户",
  "userType": "0",
  "phone": "138****8888",
  "avatarUrl": "https://thirdwx.qlogo.cn/xxx",
  "apiToken": "eyJhbGciOiJIUzI1NiJ9.xxxxx"
}
```

| 字段 | 说明 |
|------|------|
| `apiToken` | JWT token，前端存入 localStorage，后续通过 `Wx-Authorization` 传递 |
| `phone` | 已绑定手机号的用户返回手机号，新用户返回空字符串。前端根据此字段决定是否引导手机号授权 |
| `userName` | 用户名，新用户默认返回"微信用户" |
| `avatarUrl` | 头像 URL，新用户可返回默认头像 |

---

#### `POST /wxmini/user/phone/bind` — 手机号绑定（新增接口）

前端调用时机：登录成功但用户无手机号时，用户点击"授权手机号"按钮后调用。使用微信新版 `getPhoneNumber` 组件返回的 code。

请求体：

```json
{
  "code": "the-phone-code-from-getPhoneNumber"
}
```

后端实现要点：
- 使用 WxJava SDK `wxMaService.getUserService().getPhoneNumberInfo(code)` 解密手机号
- 将手机号绑定到当前登录用户（通过 `WxMiniUserContext` 获取 userId）
- 返回更新后的用户信息

响应 `data`：

```json
{
  "phone": "13800001111",
  "userName": "微信用户",
  "avatarUrl": "https://thirdwx.qlogo.cn/xxx"
}
```

---

#### `GET /wxmini/user/info` — 获取用户信息

响应 `data`：同登录接口返回结构。

---

#### `PUT /wxmini/user/info` — 更新用户信息

前端调用时机：个人信息编辑页保存时。

请求体：

```json
{
  "userName": "新昵称",
  "avatarUrl": "https://xxx/avatar.jpg"
}
```

响应 `data`：返回更新后的完整用户信息。

---

### 2.2 商家模块

#### `GET /wxmini/merchant/list` — 商家列表

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| latitude | number | 否 | 用户纬度，用于计算距离 |
| longitude | number | 否 | 用户经度 |
| categoryId | number | 否 | 分类筛选 |

响应 `data`（数组）：

```json
[
  {
    "id": 1,
    "merchantId": 1,
    "name": "蓝屿轻养·国贸旗舰店",
    "shortName": "国贸店",
    "avatar": "https://xxx/avatar.jpg",
    "coverImage": "https://xxx/cover.jpg",
    "sales": 3260,
    "address": "北京市朝阳区建国路88号嘉里中心B1",
    "distance": "650m",
    "distanceValue": 650,
    "categoryId": 0,
    "categoryName": "本店全部服务",
    "businessHours": "10:00-22:00",
    "businessHoursText": "周一至周日 10:00-22:00",
    "phone": "010-88886601",
    "latitude": 39.9087,
    "longitude": 116.4591,
    "tags": ["到店核销", "服务项目", "营业中"],
    "serviceAbilityTags": ["到店核销", "可预约", "支持退款", "过期自动退"],
    "facilityTags": ["免费停车", "独立房间", "安静环境", "专业护理"],
    "albumList": ["https://xxx/img1.jpg", "https://xxx/img2.jpg"],
    "notice": "支持扫码核销、手动核销",
    "isHot": true,
    "businessStatus": true
  }
]
```

| 字段 | 说明 |
|------|------|
| `id` | 门店 ID（前端用于跳转详情） |
| `merchantId` | 商家主体 ID |
| `sales` | 月销量 |
| `distance` | 前端展示用的距离文案（如 "650m"、"1.2km"），后端可返回空，前端会根据 latitude/longitude 自行计算 |
| `distanceValue` | 距离数值（米），同上可选 |
| `tags` | 门店标签，前端会过滤掉"营业中"/"休息中"后展示 |
| `businessStatus` | `true` 营业中 / `false` 休息中 |
| `latitude`/`longitude` | 门店坐标，前端用于距离计算和地图导航 |
| `albumList` | 门店相册图片 URL 数组 |
| `isHot` | 是否热门，前端可选展示 |

---

#### `GET /wxmini/merchant/detail/{id}` — 商家详情

响应 `data`：同列表单条结构，字段一致。

---

### 2.3 商品/团购模块

#### `GET /wxmini/groupon/list` — 团购商品列表

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchantId | number | 否 | 按商家筛选 |
| categoryId | number | 否 | 按分类筛选 |
| keyword | string | 否 | 搜索关键词 |

响应 `data`（数组）：

```json
[
  {
    "id": 101,
    "goodsId": 101,
    "title": "芳香舒压 SPA 90 分钟",
    "subtitle": "精油舒缓 + 热敷放松 + 独立房间",
    "merchantId": 1,
    "merchantName": "蓝屿轻养·国贸旗舰店",
    "image": "https://xxx/product.jpg",
    "originalPrice": 39800,
    "price": 19800,
    "sales": 2651,
    "stock": 88,
    "validDays": 30,
    "validPeriod": "2026-05-01 至 2026-06-30",
    "categoryId": 2,
    "categoryName": "SPA轻养",
    "tags": ["热销", "到店使用"],
    "description": "适合上班族下班放松，支持晚间到店。",
    "contentDetail": ["90 分钟芳香舒压护理", "肩颈热敷 1 次", "草本茶饮 1 份"],
    "bookingRequired": false,
    "bookingRule": "高峰时段建议提前 2 小时电话确认。",
    "refundRule": "未使用支持随时退，过期自动退。",
    "limitRule": "每个账号限购 3 份。",
    "status": "ON_SHELF",
    "sort": 1
  }
]
```

| 字段 | 说明 |
|------|------|
| `id` / `goodsId` | 商品 ID，两者一致 |
| `originalPrice` | 原价（分） |
| `price` | 团购价（分） |
| `sales` | 已售数量 |
| `stock` | 库存数量 |
| `validDays` | 购买后有效天数 |
| `validPeriod` | 有效期文案，直接展示 |
| `contentDetail` | 套餐包含项目列表 |
| `bookingRequired` | 是否需要预约 |
| `bookingRule` | 预约规则文案 |
| `refundRule` | 退款规则文案 |
| `limitRule` | 限购规则文案 |
| `status` | `ON_SHELF` 上架 / `OFF_SHELF` 下架 |
| `tags` | 标签数组，如 ["热销", "到店使用", "限时"] |

---

#### `GET /wxmini/groupon/detail/{id}` — 团购商品详情

响应 `data`：同列表单条结构。

---

### 2.4 订单模块

#### `POST /wxmini/order/create` — 创建订单

前端支持两种下单模式，后端需同时支持：

**单品下单**（从商品详情页直接购买）：

```json
{
  "productId": 101,
  "quantity": 1,
  "phone": "13800001111",
  "couponId": 1
}
```

**购物车下单**（从购物车结算，可能包含多个商品）：

```json
{
  "items": [
    { "productId": 101, "quantity": 2 },
    { "productId": 102, "quantity": 1 }
  ],
  "phone": "13800001111",
  "couponId": 1
}
```

| 字段 | 说明 |
|------|------|
| `productId` | 商品 ID（单品模式） |
| `items` | 商品列表（购物车模式），每个元素含 `productId` 和 `quantity` |
| `quantity` | 购买数量 |
| `phone` | 用户手机号 |
| `couponId` | 优惠券 ID，可选 |

响应 `data`（单品返回单个订单号，购物车返回订单号列表）：

```json
{
  "orderNo": "ORD202605180001",
  "orderAmount": 19800,
  "couponAmount": 1000,
  "payAmount": 18800
}
```

---

#### `POST /wxmini/pay/order/create` — 发起微信支付

请求体：

```json
{
  "orderNo": "ORD202605180001"
}
```

响应 `data`（微信支付参数，前端传给 `wx.requestPayment`）：

```json
{
  "timeStamp": "1778256000",
  "nonceStr": "5K8264ILTKCH16CQ2502SI8ZNMTM67VS",
  "package": "prepay_id=wx20xxxxx",
  "signType": "RSA",
  "paySign": "oR9d8PuhnIc+YZ8cBHFCwfgpaK9gd7vaRvkYD7rthRAZ"
}
```

后端实现要点：调用微信支付 V3 统一下单 API，返回前端调起支付所需的参数。

---

#### `GET /wxmini/order/list` — 订单列表

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 按状态筛选 |

响应 `data`（数组）：

```json
[
  {
    "id": 1,
    "orderNo": "ORD202605080001",
    "productId": 101,
    "merchantId": 1,
    "title": "芳香舒压 SPA 90 分钟",
    "merchantName": "蓝屿轻养·国贸旗舰店",
    "image": "https://xxx/product.jpg",
    "quantity": 1,
    "orderAmount": 19800,
    "couponAmount": 1000,
    "payAmount": 18800,
    "price": 18800,
    "phone": "138****8888",
    "status": "PAID_UNUSED",
    "createTime": 1778256000000,
    "payTime": 1778256300000,
    "writeOffCode": "LY8012",
    "writeOffDeadline": 1780848000000,
    "writeOffTime": null,
    "refundReason": null,
    "refundTime": null,
    "expireTime": null
  }
]
```

| 字段 | 说明 |
|------|------|
| `orderNo` | 订单号，唯一 |
| `title` | 商品标题（冗余存储，方便列表展示） |
| `merchantName` | 商家名称（冗余） |
| `image` | 商品封面图（冗余） |
| `orderAmount` | 订单总金额（分） |
| `couponAmount` | 优惠券抵扣金额（分） |
| `payAmount` / `price` | 实付金额（分），两者一致 |
| `writeOffCode` | 核销码（4 位字母数字，如 "LY8012"），状态为 PAID_UNUSED 时有值 |
| `writeOffDeadline` | 核销截止时间（时间戳毫秒），超过后应自动退款 |
| `writeOffTime` | 实际核销时间（时间戳毫秒） |
| `createTime` / `payTime` | 下单/支付时间（时间戳毫秒） |
| `expireTime` | 待支付订单过期时间（时间戳毫秒），超时自动关闭 |
| `refundReason` / `refundTime` | 退款原因和时间 |

---

#### `GET /wxmini/order/detail/{orderNo}` — 订单详情

响应 `data`：同列表单条结构。

---

#### `POST /wxmini/order/cancel/{orderNo}` — 取消订单

仅 PENDING_PAY 状态可取消。

响应 `data`：返回更新后的订单对象。

---

#### `POST /wxmini/order/writeOff/{code}` — 核销订单

由商家端小程序调用（C 端不直接调用），传入核销码完成核销。

C 端展示的核销二维码内容即为 `writeOffCode` 字符串（如 `LY8012`），商家端扫码后将此字符串作为 `code` 参数传给此接口。后端校验订单状态和有效期后完成核销。

---

#### `GET /wxmini/pay/order/query` — 查询支付结果

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| outTradeNo | string | 是 | 商户订单号（即 orderNo） |

响应 `data`：返回支付状态信息。

---

### 2.5 模板配置

#### `GET /wxmini/template/config` — 获取模板配置

此接口无需鉴权。前端用于控制各页面的文案、功能开关等，支持按商家/品牌定制。

响应 `data`：完整的模板配置对象（见下方结构）。前端会与本地默认配置做深度合并，缺失字段自动使用默认值，因此后端只需返回需要定制的字段即可。

<details>
<summary>完整模板配置结构（点击展开）</summary>

```json
{
  "templateMeta": {
    "code": "merchant_o2o_v1",
    "name": "门店服务",
    "version": "1.1.0",
    "configOwner": "merchant-mini-admin",
    "description": "门店服务、到店使用和售后服务配置"
  },
  "brandInfo": {
    "id": "brand_001",
    "name": "蓝屿轻养生活馆",
    "slogan": "",
    "notice": "支持扫码核销、手动核销、商品上下架与单店员工权限管理",
    "servicePhone": "010-88886601",
    "searchPlaceholder": "搜索套餐、服务项目",
    "primaryColor": "#1677ff"
  },
  "home": {
    "locationLabel": "距离本店",
    "merchantSectionTitle": "本店信息",
    "merchantSectionSubtitle": "查看营业时间、门店地址与联系方式",
    "productSectionTitle": "本店服务项目",
    "productSectionSubtitle": "购买后到店出示使用码即可使用",
    "sortOptions": []
  },
  "profile": {
    "loginTitle": "点击登录",
    "loginDesc": "登录后查看订单、券包与到店使用信息",
    "orderSectionTitle": "我的订单",
    "orderMoreText": "全部订单 ›",
    "orderEntries": [
      { "label": "待支付", "status": "PENDING_PAY" },
      { "label": "待使用", "status": "UNUSED" },
      { "label": "退款/售后", "status": "AFTER_SALE" }
    ],
    "assetEntries": [
      { "label": "优惠券/红包", "url": "/pages/coupon/coupon", "countField": "couponCount" },
      { "label": "我的收藏", "url": "/pages/favorite/favorite", "countField": "favoriteCount" }
    ],
    "benefitTitle": "权益中心",
    "benefitDesc": "优惠券、收藏和待使用订单统一展示，方便到店前快速查看。",
    "benefitTips": ["支持到店使用", "支持退款售后"],
    "serviceMenus": [
      { "label": "我的优惠券 / 红包", "url": "/pages/coupon/coupon" },
      { "label": "我的收藏", "url": "/pages/favorite/favorite" },
      { "label": "联系客服", "url": "/pages/contact/contact" }
    ],
    "logoutText": "退出登录"
  },
  "merchantDetail": {
    "hotTag": "热门",
    "phoneActionText": "一键拨打",
    "mapActionText": "查看地图",
    "addressTitle": "门店地址",
    "productSectionTitle": "在售项目",
    "productSectionSubtitle": "点击查看详情与使用规则",
    "albumSectionTitle": "门店相册",
    "albumSectionSubtitle": "门头、环境与项目实拍",
    "collectText": "收藏门店",
    "collectedText": "已收藏",
    "contactButtonText": "联系门店"
  },
  "productDetail": {
    "decisionSectionTitle": "购买决策信息",
    "ruleSectionTitle": "使用规则",
    "merchantSectionTitle": "服务门店",
    "contentSectionTitle": "项目内容",
    "salesLabel": "已售",
    "stockLabel": "库存",
    "validDaysLabel": "有效期",
    "timeRangeRuleText": "使用时间段：以门店营业时间为准",
    "bookingYesText": "需要预约",
    "bookingNoText": "无需预约",
    "collectText": "收藏",
    "collectedText": "已收藏",
    "shareText": "分享",
    "buyButtonText": "立即抢购"
  },
  "checkout": {
    "productSectionTitle": "确认商品",
    "infoSectionTitle": "购买信息",
    "priceSectionTitle": "价格明细",
    "useRuleSectionTitle": "使用说明",
    "quantityLabel": "购买数量",
    "phoneLabel": "手机号",
    "couponLabel": "优惠券",
    "subtotalLabel": "商品金额",
    "discountLabel": "优惠抵扣",
    "totalLabel": "实付总金额",
    "paySummaryLabel": "待支付",
    "submitButtonText": "提交订单并支付",
    "loginHintText": "订单创建后可在订单中心完成支付与到店使用"
  },
  "featureToggle": {
    "enableCoupon": true,
    "enableFavorite": true,
    "enableAddress": false,
    "enableJoinApply": false,
    "enableBookingRule": true,
    "enableRefundRule": true,
    "enableMerchantAlbum": true
  }
}
```

</details>

---

## 三、订单状态机

```
                ┌─────────────┐
                │ PENDING_PAY │  用户下单后
                │   (待支付)    │
                └──────┬──────┘
           ┌───────────┼───────────┐
           ▼           ▼           ▼
     ┌──────────┐ ┌───────────┐ ┌──────────┐
     │CANCELLED │ │PAID_UNUSED│ │ REFUNDING│
     │  (已取消) │ │  (待使用)  │ │  (退款中) │
     └──────────┘ └─────┬─────┘ └─────┬────┘
                        ▼             ▼
                  ┌───────────┐ ┌──────────┐
                  │  USED_    │ │ REFUNDED │
                  │ COMPLETED │ │  (已退款) │
                  │  (已完成)  │ └──────────┘
                  └───────────┘
                        │
                  ┌──────────┐
                  │  CLOSED  │  超时未支付自动关闭
                  │  (已关闭) │
                  └──────────┘
```

| 状态值 | 前端展示 | 前端可执行操作 |
|--------|----------|----------------|
| `PENDING_PAY` | 待支付 | 去支付、取消订单 |
| `PAID_UNUSED` | 待使用 | 查看核销码、申请退款 |
| `USED_COMPLETED` | 已完成 | 再来一单、评价订单 |
| `REFUNDING` | 退款中 | 查看进度 |
| `REFUNDED` | 已退款 | 重新购买 |
| `CANCELLED` | 已取消 | 重新购买 |
| `CLOSED` | 已关闭 | 重新购买 |

---

## 四、后端需实现的定时任务

| 任务 | 说明 |
|------|------|
| 待支付超时关闭 | `PENDING_PAY` 超过 `expireTime` 自动流转为 `CLOSED` |
| 核销码过期退款 | `PAID_UNUSED` 超过 `writeOffDeadline` 自动退款并流转为 `REFUNDED` |

---

## 五、前端页面与接口调用关系

所有页面均已实现 **API 优先 + localStorage 降级** 模式：先调后端接口，接口不可用时自动回退到本地 mock 数据，后端未就绪也可完整演示。切换为真实环境只需修改 `app.js` 中的 `baseUrl`，无需改动任何页面代码。

| 前端页面 | 调用的接口 | 降级策略 |
|----------|-----------|----------|
| 首页 `pages/home` | `GET /wxmini/merchant/list`、`GET /wxmini/groupon/list` | mock 数据 |
| 商品详情 `pages/product-detail` | `GET /wxmini/groupon/detail/{id}` | mock 数据 |
| **购物车 `pages/cart`** | 纯前端（localStorage），不调后端接口 | — |
| 确认下单 `pages/checkout` | `POST /wxmini/order/create` | localStorage 本地创建 |
| 订单列表 `pages/order` | `GET /wxmini/order/list` | localStorage 读取 |
| 订单详情 `pages/order-detail` | `GET /wxmini/order/detail/{orderNo}` | localStorage 查找 |
| 支付 `pages/order-detail` | `POST /wxmini/pay/order/create` → `wx.requestPayment` | 模拟支付（直接更新状态） |
| 个人中心 `pages/mine` | `GET /wxmini/login`（登录时） | mock token + 用户信息 |
| 手机号绑定 `pages/mine` | `POST /wxmini/user/phone/bind` | toast 提示 |
| 个人信息编辑 `pages/profile-edit` | `PUT /wxmini/user/info` | 本地更新 |
| 搜索 `pages/search` | 纯前端筛选 | mock 数据 |
| 搜索结果 `pages/search-result` | `GET /wxmini/groupon/list?keyword=xxx` | mock 数据筛选 |

---

## 六、文件上传

前端头像/评价图片上传使用 `wx.uploadFile`，接口：

```
POST /wxmini/common/upload
Content-Type: multipart/form-data
Wx-Authorization: {token}
```

请求：`file` 字段，单个文件。

响应 `data`：

```json
{
  "url": "https://xxx/uploaded-image.jpg",
  "fileName": "uploaded-image.jpg"
}
```

---

## 七、核销二维码与端到端核销流程

### 前端二维码生成

C 端使用 `components/qrcode/` 组件在 Canvas 上实时生成二维码，无需后端返回图片。二维码内容为订单的 `writeOffCode` 字符串（如 `LY8012`），使用 QR Version 1-6、EC Level L、Alphanumeric 编码模式。

组件用法：

```xml
<qrcode text="{{order.writeOffCode}}" size="{{280}}" />
```

已集成的页面：
- `pages/order` — 核销码弹窗中展示二维码
- `pages/order-detail` — 待使用订单的核销卡中展示二维码

### 端到端核销流程

```
C 端用户                    商家端                      后端
   │                         │                          │
   │ 查看订单 → 展示二维码     │                          │
   │ 二维码内容 = writeOffCode │                          │
   │                         │                          │
   │ 到店出示二维码 ──────────→│ wx.scanCode() 读取码内容   │
   │                         │                          │
   │                         │ POST /order/writeOff/{code} ──→
   │                         │                          │ 校验订单状态
   │                         │                          │ 校验有效期
   │                         │                          │ 更新为 COMPLETED
   │                         │                          │ 记录核销时间和操作人
   │                         │←── { success: true } ────│
   │                         │ toast "核销成功"           │
   │                         │                          │
   │←───────── 订单状态变为已完成 ──────────────────────│
```

后端核销接口需校验：
1. 核销码或订单号对应订单是否存在
2. 订单状态必须为 `PAID_UNUSED`（C 端）或 `PENDING_VERIFY`（商家端）
3. 团购券是否在有效期内（`writeOffDeadline` 未过期）
4. 记录核销操作员工 ID 和姓名（从 `WxMiniUserContext` 获取）

---

## 八、支付流程与订阅消息

### 微信支付对接

前端已实现完整的支付流程，后端对接后即可使用：

1. 用户确认订单 → 前端调用 `POST /wxmini/order/create` 创建订单
2. 前端调用 `POST /wxmini/pay/order/create` 获取微信支付参数
3. 前端调用 `wx.requestPayment(payParams)` 调起微信支付
4. 支付成功 → 后端收到微信回调 → 更新订单状态为 `PAID_UNUSED`
5. 前端自动请求订阅消息授权

后端未就绪时，前端自动降级为模拟支付（直接更新订单状态），不影响演示。

### 订阅消息

前端在支付成功后自动调用 `wx.requestSubscribeMessage` 请求消息授权，模板 ID：

| 模板 | 用途 | 触发时机 |
|------|------|----------|
| `order_status_change` | 订单状态变更通知 | 订单状态发生变更时 |
| `write_off_remind` | 核销提醒 | 核销码即将过期时 |

后端需在微信公众平台申请对应消息模板，并在订单状态变更时调用微信订阅消息 API 发送通知。

---

## 九、开发对接顺序建议

1. **登录 + 鉴权**：先实现 `GET /wxmini/login` 和 JWT 过滤器，确保前端能登录并拿到 token
2. **模板配置**：实现 `GET /wxmini/template/config`，可先返回空对象（前端有默认值兜底）
3. **商家 + 商品**：实现商家列表、商品列表、商品详情，前端首页和详情页即可跑通
4. **下单 + 支付**：实现创建订单、发起支付，完成核心购买链路
5. **订单管理**：实现订单列表、详情、取消，配合前端订单中心
6. **用户信息**：实现用户信息获取/更新、手机号绑定
7. **其他**：优惠券、收藏、搜索等辅助功能按优先级迭代
