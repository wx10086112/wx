# 小程序接口缺失清单（当前口径：`wx_app` 同时承载 C 端与商家端）

> 基于当前 `wx_app` 目录结构与后端代码对比整理
> 更新日期：2026-06-02

---

## 一、说明

当前前端结构已经调整为：

- C 端页面：`wx_app/pages/*`
- 商家端页面：`wx_app/pages/merchant/*`
- 商家端接口封装：`wx_app/api/merchant-mini/*`

原 `merchant_app` 目录已废弃并移除，因此本清单不再使用旧目录名描述现行前端代码。

---

## 二、C 端接口待补清单

### 已有基础

| 接口 | 说明 |
|------|------|
| `GET /wxmini/login` | 微信登录入口已存在 |
| `GET /wxmini/user/info` | 获取用户信息入口已存在 |

### 路径或实现仍需统一

| 接口 | 当前问题 | 说明 |
|------|----------|------|
| `POST /wxmini/user/phone/bind` | 后端路径/方法仍需与前端口径完全对齐 | 手机号绑定仍需统一 |
| `POST /wxmini/order/writeOff/{code}` | 当前商家端已存在核销接口，C 端是否保留独立端点需明确 | 避免消费者与商家端接口语义混淆 |

### 仍待真实化的核心接口

| 接口 | 模块 | 说明 |
|------|------|------|
| `PUT /wxmini/user/info` | mall-user | 用户资料更新 |
| `GET /wxmini/merchant/list` | mall-merchant | 商家列表 |
| `GET /wxmini/merchant/detail/{id}` | mall-merchant | 商家详情 |
| `GET /wxmini/groupon/list` | mall-product | 团购/商品列表 |
| `GET /wxmini/groupon/detail/{id}` | mall-product | 团购/商品详情 |
| `POST /wxmini/order/create` | mall-order | 创建订单 |
| `GET /wxmini/order/list` | mall-order | 订单列表 |
| `GET /wxmini/order/detail/{orderNo}` | mall-order | 订单详情 |
| `POST /wxmini/order/cancel/{orderNo}` | mall-order | 取消订单 |
| `GET /wxmini/template/config` | wxmini | 模板配置 |
| `POST /wxmini/common/upload` | wxmini | 通用上传 |
| `POST /wxmini/pay/order/create` | mall-pay | 微信支付下单 |
| `GET /wxmini/pay/order/query` | mall-pay | 微信支付查单 |

---

## 三、商家端接口待补清单

当前对应前端目录：

- `wx_app/pages/merchant/*`
- `wx_app/api/merchant-mini/*`

### 已有基础接口

| 接口 | 说明 |
|------|------|
| `POST /wxmini/merchant-mini/auth/login` | 商家账号密码登录 |
| `GET /wxmini/merchant-mini/workbench/overview` | 工作台统计 |
| `GET /wxmini/merchant-mini/order/list` | 订单列表 |
| `GET /wxmini/merchant-mini/order/detail/{orderNo}` | 订单详情 |
| `POST /wxmini/merchant-mini/order/write-off/{code}` | 核销 |
| `GET /wxmini/merchant-mini/verify/record/list` | 核销记录 |
| `GET /wxmini/merchant-mini/goods/list` | 商品列表 |
| `POST /wxmini/merchant-mini/goods/save` | 新增/编辑商品 |
| `PUT /wxmini/merchant-mini/goods/status` | 商品上下架 |
| `POST /wxmini/merchant-mini/goods/image/upload` | 商品图片上传 |
| `GET /wxmini/merchant-mini/store/profile` | 门店信息 |
| `PUT /wxmini/merchant-mini/store/profile` | 更新门店信息 |
| `GET /wxmini/merchant-mini/staff/list` | 员工列表 |
| `PUT /wxmini/merchant-mini/staff/permission` | 员工权限修改 |
| `GET /wxmini/merchant-mini/finance/overview` | 财务概览 |

### 仍待补齐的订单操作

| 接口 | 说明 |
|------|------|
| `POST /wxmini/merchant-mini/order/accept/{orderNo}` | 接单 |
| `POST /wxmini/merchant-mini/order/reject/{orderNo}` | 拒单 |
| `POST /wxmini/merchant-mini/order/cancel/{orderNo}` | 商家取消订单 |
| `POST /wxmini/merchant-mini/order/complete/{orderNo}` | 完成订单/履约完成 |
| `POST /wxmini/merchant-mini/order/refund/approve/{orderNo}` | 同意退款 |
| `POST /wxmini/merchant-mini/order/refund/reject/{orderNo}` | 拒绝退款 |

### 仍待补齐的员工、营销、入驻能力

| 接口 | 说明 |
|------|------|
| `POST /wxmini/merchant-mini/staff/add` | 添加员工 |
| `PUT /wxmini/merchant-mini/staff/update` | 编辑员工信息/状态 |
| `GET /wxmini/merchant-mini/marketing/coupon/list` | 优惠券列表 |
| `POST /wxmini/merchant-mini/marketing/coupon/save` | 创建/编辑优惠券 |
| `PUT /wxmini/merchant-mini/marketing/coupon/status` | 启停优惠券 |
| `GET /wxmini/merchant-mini/marketing/promotion/list` | 满减活动列表 |
| `POST /wxmini/merchant-mini/marketing/promotion/save` | 创建/编辑满减活动 |
| `POST /wxmini/merchant-mini/apply/submit` | 提交入驻申请 |
| `GET /wxmini/merchant-mini/apply/status` | 查询审核状态 |

### 财务接口说明

当前仍保留 `finance/overview` 口径，但后续如果结算中心语义最终稳定，建议统一为更清晰的 `settlement/*` 路径，避免“财务概览”和“结算概览”混用。

---

## 四、实现优先级建议

### 第一批：商家端履约核心链路

1. `POST /wxmini/merchant-mini/order/accept/{orderNo}`
2. `POST /wxmini/merchant-mini/order/reject/{orderNo}`
3. `POST /wxmini/merchant-mini/order/cancel/{orderNo}`
4. `POST /wxmini/merchant-mini/order/refund/approve/{orderNo}`
5. `POST /wxmini/merchant-mini/order/refund/reject/{orderNo}`
6. `POST /wxmini/merchant-mini/staff/add`
7. `PUT /wxmini/merchant-mini/staff/update`

### 第二批：C 端首页与商品

1. `GET /wxmini/merchant/list`
2. `GET /wxmini/merchant/detail/{id}`
3. `GET /wxmini/groupon/list`
4. `GET /wxmini/groupon/detail/{id}`
5. `GET /wxmini/template/config`

### 第三批：C 端订单链路

1. `POST /wxmini/order/create`
2. `GET /wxmini/order/list`
3. `GET /wxmini/order/detail/{orderNo}`
4. `POST /wxmini/order/cancel/{orderNo}`

### 第四批：辅助能力

1. `PUT /wxmini/user/info`
2. `POST /wxmini/user/phone/bind`
3. `POST /wxmini/common/upload`

### 第五批：营销、入驻、支付

1. 商家端营销与入驻相关接口
2. `POST /wxmini/pay/order/create`
3. `GET /wxmini/pay/order/query`
