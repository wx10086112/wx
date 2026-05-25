# 小程序接口缺失清单（C端 + 商家端）

> 基于 `wx_app/README.md` 和 `merchant_app/README.md` 与当前后端代码对比
> 更新日期：2026-05-19

---

## 一、C端小程序（wx_app）— 17个接口

### 总览

| 状态 | 数量 | 说明 |
|------|------|------|
| 已完成 | 2 | login、user/info |
| 路径不匹配 | 2 | phone/bind、order/writeOff |
| 仅有Stub | 2 | pay/order/create、pay/order/query |
| 未实现 | 11 | 商家、商品、订单、模板、上传 |
| **合计** | **17** | |

### 已完成（2个）

| 接口 | 文件 | 说明 |
|------|------|------|
| `GET /wxmini/login` | `mall-user/.../WxLoginController.java` | 微信登录，按AppID查商家secret |
| `GET /wxmini/user/info` | `mall-user/.../WxMaUserController.java` | 获取用户信息 |

### 路径/方法不匹配（2个）

| 接口 | 当前实现 | 应改为 | 文件 |
|------|----------|--------|------|
| `POST /wxmini/user/phone/bind` | `GET /wxmini/user/phone` | POST + 路径改为 `/user/phone/bind` | `mall-user/.../WxMaUserController.java` |
| `POST /wxmini/order/writeOff/{code}` | `/wxmini/merchant-mini/order/write-off/{code}` | 需新增C端独立端点 | `wxmini/.../MerchantMiniController.java` |

### 仅有Stub/TODO（2个）

| 接口 | 文件 | 说明 |
|------|------|------|
| `POST /wxmini/pay/order/create` | `mall-pay/.../WxPayController.java` | 空壳，需对接微信支付V3 |
| `GET /wxmini/pay/order/query` | `mall-pay/.../WxPayController.java` | 空壳，需对接微信支付V3 |

### 未实现（11个）

| 接口 | 模块 | 依赖 |
|------|------|------|
| `PUT /wxmini/user/info` | mall-user | mall_user表 |
| `GET /wxmini/merchant/list` | mall-merchant | merchant + merchant_store |
| `GET /wxmini/merchant/detail/{id}` | mall-merchant | merchant + merchant_store |
| `GET /wxmini/groupon/list` | mall-product | product表 |
| `GET /wxmini/groupon/detail/{id}` | mall-product | product表 |
| `POST /wxmini/order/create` | mall-order | mall_order + order_item |
| `GET /wxmini/order/list` | mall-order | mall_order表 |
| `GET /wxmini/order/detail/{orderNo}` | mall-order | mall_order表 |
| `POST /wxmini/order/cancel/{orderNo}` | mall-order | mall_order表 |
| `GET /wxmini/template/config` | wxmini | 无DB，DTO已建好 |
| `POST /wxmini/common/upload` | wxmini | 复用已有/common/upload |

---

## 二、商家端小程序（merchant_app）— 31个接口

### 总览

| 状态 | 数量 | 说明 |
|------|------|------|
| 已完成 | 13 | 登录、工作台、订单查询、核销、商品CRUD、门店、员工权限、财务 |
| 文档过期 | 1 | auth/login文档仍写roleKey，实际已改为username+password |
| 未实现 | 17 | 订单操作、营销、入驻申请 |
| **合计** | **31** | |

### 已完成（13个）

| 接口 | 文件 | 说明 |
|------|------|------|
| `POST /auth/login` | `MerchantMiniController:46` | 用户名+密码登录，BCrypt校验 |
| `GET /workbench/overview` | `MerchantMiniController:53` | 工作台统计 |
| `GET /order/list` | `MerchantMiniController:62` | 订单列表 |
| `GET /order/detail/{orderNo}` | `MerchantMiniController:71` | 订单详情 |
| `POST /order/write-off/{code}` | `MerchantMiniController:84` | 核销 |
| `GET /verify/record/list` | `MerchantMiniController:97` | 核销记录 |
| `GET /goods/list` | `MerchantMiniController:106` | 商品列表 |
| `POST /goods/save` | `MerchantMiniController:115` | 新增/编辑商品 |
| `PUT /goods/status` | `MerchantMiniController:128` | 商品上下架 |
| `POST /goods/image/upload` | `MerchantMiniController:143` | 商品图片上传 |
| `GET /store/profile` | `MerchantMiniController:181` | 门店信息 |
| `PUT /store/profile` | `MerchantMiniController:190` | 更新门店信息 |
| `GET /staff/list` | `MerchantMiniController:203` | 员工列表 |
| `PUT /staff/permission` | `MerchantMiniController:212` | 修改员工权限 |
| `GET /finance/overview` | `MerchantMiniController:225` | 财务概览 |
| `POST /finance/withdraw` | `MerchantMiniController:234` | 申请提现 |

### 文档过期（1个）

| 接口 | 问题 | 说明 |
|------|------|------|
| `POST /auth/login` | README仍写`roleKey`，实际已改为`username+password` | 需更新README |

### 未实现（17个）

#### 订单操作（6个）

| 接口 | 说明 | 前端状态机需要 |
|------|------|----------------|
| `POST /order/accept/{orderNo}` | 接单，PENDING_ACCEPT → ACCEPTED | 待接单 → 已接单 |
| `POST /order/reject/{orderNo}` | 拒单，PENDING_ACCEPT → REJECTED | 待接单 → 已拒单 |
| `POST /order/ship/{orderNo}` | 发货，ACCEPTED → SHIPPING | 已接单 → 配送中 |
| `POST /order/complete/{orderNo}` | 确认送达，SHIPPING → COMPLETED | 配送中 → 已完成 |
| `POST /order/cancel/{orderNo}` | 商家取消订单 | 非终态可取消 |
| `POST /order/refund/approve/{orderNo}` | 同意退款，REFUNDING → REFUNDED | 退款中 → 已退款 |
| `POST /order/refund/reject/{orderNo}` | 拒绝退款，REFUNDING → PENDING_VERIFY | 退款中恢复待核销 |

> 注：前端已删除SHIPPING相关（2026-05-19改造），ship/complete是否还需要看业务需求

#### 员工管理（2个）

| 接口 | 说明 |
|------|------|
| `POST /staff/add` | 添加员工（含密码创建） |
| `PUT /staff/update` | 编辑员工信息/状态 |

#### 营销模块（6个）

| 接口 | 说明 |
|------|------|
| `GET /marketing/coupon/list` | 优惠券列表 |
| `POST /marketing/coupon/save` | 创建/编辑优惠券 |
| `PUT /marketing/coupon/status` | 启停优惠券 |
| `GET /marketing/promotion/list` | 满减活动列表 |
| `POST /marketing/promotion/save` | 创建/编辑满减活动 |

#### 入驻申请（2个）

| 接口 | 说明 |
|------|------|
| `POST /apply/submit` | 提交入驻申请（含图片） |
| `GET /apply/status` | 查询审核状态 |

---

## 三、合并统计

| 类别 | C端 (wx_app) | 商家端 (merchant_app) | 合计 |
|------|-------------|---------------------|------|
| 已完成 | 2 | 16 | 18 |
| 路径不匹配 | 2 | 0 | 2 |
| 仅有Stub | 2 | 0 | 2 |
| 文档过期 | 0 | 1 | 1 |
| 未实现 | 11 | 17 | 28 |
| **合计** | **17** | **34** | **51** |

---

## 四、实现优先级建议

```
第一批：商家端订单操作（核心链路）
  ├── POST /merchant-mini/order/accept/{orderNo}
  ├── POST /merchant-mini/order/reject/{orderNo}
  ├── POST /merchant-mini/order/cancel/{orderNo}
  ├── POST /merchant-mini/order/refund/approve/{orderNo}
  ├── POST /merchant-mini/order/refund/reject/{orderNo}
  └── POST /merchant-mini/staff/add + PUT /staff/update

第二批：C端首页 + 商品
  ├── GET /wxmini/merchant/list
  ├── GET /wxmini/merchant/detail/{id}
  ├── GET /wxmini/groupon/list
  ├── GET /wxmini/groupon/detail/{id}
  └── GET /wxmini/template/config（可先返回空对象）

第三批：C端订单链路
  ├── POST /wxmini/order/create
  ├── GET /wxmini/order/list
  ├── GET /wxmini/order/detail/{orderNo}
  └── POST /wxmini/order/cancel/{orderNo}

第四批：用户 + 辅助
  ├── PUT /wxmini/user/info
  ├── POST /wxmini/user/phone/bind（修正路径）
  ├── POST /wxmini/common/upload
  └── 更新商家端login的README文档

第五批：营销 + 入驻（可后置）
  ├── GET/POST /merchant-mini/marketing/coupon/*
  ├── GET/POST /merchant-mini/marketing/promotion/*
  ├── POST /merchant-mini/apply/submit
  └── GET /merchant-mini/apply/status

第六批：支付（依赖微信商户号）
  ├── POST /wxmini/pay/order/create
  └── GET /wxmini/pay/order/query
```
