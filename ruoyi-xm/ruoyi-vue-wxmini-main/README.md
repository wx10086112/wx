# SaaS 多端商家系统后端与运营后台

本目录是 SaaS 多端商家系统的后端主工程，同时包含平台运营后台前端 `ruoyi-ui`。

当前前端目录结构已经调整为：

- C 端页面与商家端页面统一位于 `../wx_app`
- 商家端前端不再单独维护 `merchant_app` 目录
- 商家端现行页面路径为 `../wx_app/pages/merchant/*`

后端接口边界不变，仍然按 C 端和商家端分别提供 API。

## 工程组成

| 目录 | 说明 |
|------|------|
| `ruoyi-admin` | 后端启动模块，聚合加载若依系统接口、商城后台接口和各业务模块。 |
| `ruoyi-ui` | 平台运营后台 Web 前端，基于 Vue 2 + Element UI。 |
| `ruoyi-common` | 若依公共工具和基础组件。 |
| `ruoyi-framework` | Spring Security、Web、安全过滤器、权限控制等框架能力。 |
| `ruoyi-system` | 若依系统管理能力，如用户、角色、菜单、字典、参数配置。 |
| `ruoyi-quartz` | 定时任务。 |
| `ruoyi-generator` | 若依代码生成器。 |
| `ruoyi-wxmini` | 微信小程序接入层，包含 C 端接口、商家端接口、模板配置、上传、微信回调等。 |
| `ruoyi-mall-common` | 商城公共能力：微信端 JWT、认证上下文、支付抽象、通用 BO/VO。 |
| `ruoyi-mall-user` | 用户域：微信用户、用户信息、地址、购物车、收藏、优惠券。 |
| `ruoyi-mall-merchant` | 商家域：商家主体、门店、商家员工、审核。 |
| `ruoyi-mall-product` | 商品域：商品、分类、团购活动。 |
| `ruoyi-mall-order` | 订单域：订单、订单项、退款/售后、统计。 |
| `ruoyi-mall-finance` | 财务域：交易流水、平台收入、商家提现、财务报表。 |
| `ruoyi-mall-marketing` | 营销域：Banner、优惠券等。 |
| `ruoyi-mall-pay` | 支付域：支付记录、微信支付下单、查询、回调。 |
| `project-docs` | 项目文档、数据库脚本、后端接入规划和模块重构文档。 |
| `sql` | 若依系统 SQL 和补充脚本。 |

## 多端 API 边界

| 调用方 | 前端位置 | API 前缀 | 鉴权方式 | 说明 |
|--------|----------|----------|----------|------|
| C 端小程序 | `wx_app/pages/*` | `/wxmini/**` | `Wx-Authorization: {token}` | 消费者登录、商家/商品浏览、下单、支付、订单、模板配置。 |
| 商家端小程序 | `wx_app/pages/merchant/*` | `/wxmini/merchant-mini/**` | `Wx-Authorization: Bearer {token}` | 商家员工登录、工作台、订单处理、核销、商品、门店、员工、财务、营销。 |
| 运营后台 | `ruoyi-ui` | `/mall/**`、`/system/**` 等 | `Authorization: Bearer {token}` | 平台运营管理、若依系统管理、商家审核、订单监管、财务管理。 |
| 微信服务器 | - | `/wxmini/portal/{appid}`、`/wxmini/pay/notify` | 微信签名/回调校验 | 微信消息、支付通知等服务端回调。 |

## 业务域关系

```text
平台运营后台
  ├─ 管理商家、门店、商品、订单、售后、财务、营销、模板
  └─ 审核入驻、审批提现、查看数据分析

商家端页面（wx_app/pages/merchant）
  ├─ 商家员工登录
  ├─ 管理本商家商品、门店、员工、营销
  ├─ 处理本商家订单、核销和退款
  └─ 查看本商家财务收益

C 端页面（wx_app/pages）
  ├─ 微信用户登录
  ├─ 浏览当前商家/门店商品
  ├─ 下单、支付、查看订单
  └─ 到店出示核销码、评价、售后
```

## SaaS 化建模要求

后续新增业务应优先检查以下字段和边界：

- 平台级数据：由后台账号管理，不绑定单一商家。
- 商家级数据：必须带 `merchantId`。
- 门店级数据：必须带 `storeId`，并同时能追溯 `merchantId`。
- C 端用户数据：必须带 `userId`。
- 商家员工数据：必须带 `staffId`、`merchantId`、权限码，必要时带 `storeId`。
- 模板配置：应支持平台默认配置、商家覆盖配置、门店级扩展配置。

## 当前实现状态

| 领域 | 状态 |
|------|------|
| 微信登录/用户 | 基础能力已具备，部分用户信息更新和手机号绑定路径仍需统一。 |
| C 端商品/商家/订单 | 已有 Controller、DTO、mock 降级和部分真实接口。 |
| 商家端 | 前端已并入 `wx_app`，后端已有登录、工作台、订单、核销、商品、门店、员工、财务等接口雏形，部分操作仍待实现。 |
| 运营后台 | 页面和若依框架完整，商城业务页面存在 mock/真实接口混用。 |
| 支付 | 前端流程已具备，后端支付模块仍需完成真实微信支付 V3 下单、回调、查询和退款。 |
| 营销 | 优惠券、Banner 等实体已预留，业务层和接口需继续补齐。 |
| 财务 | 流水、分账、提现、报表已有模块基础，需与支付、退款、结算联动。 |

## 启动说明

### 后端

```bash
cd F:\wx\ruoyi-xm\ruoyi-vue-wxmini-main
mvn clean package -DskipTests
```

启动入口：

```text
ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java
```

默认端口：`8080`。

### 运营后台

```bash
cd F:\wx\ruoyi-xm\ruoyi-vue-wxmini-main\ruoyi-ui
npm run dev
```

默认访问：`http://localhost`。

## 继续建设优先级

1. 统一 C 端、商家端、后台三套订单状态枚举和数据库状态值。
2. 完成真实微信支付、支付回调、退款、支付记录和订单状态联动。
3. 补齐商家端营销、员工新增编辑、入驻申请、退款审核等真实接口。
4. 将运营后台业务页面从 mock 切换到真实 API。
5. 增加租户隔离校验，确保所有商家端接口只能访问本商家/本门店数据。
6. 补齐定时任务：待支付关闭、核销码过期退款、分账结算、订阅消息提醒。

