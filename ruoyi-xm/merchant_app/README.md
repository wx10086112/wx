# 商家端微信小程序

## 定位

本目录是独立的商家端微信小程序，服务对象为单店店长 / 店员，聚焦日常运营操作，不承担平台总后台和 PC 后台职责。

## 当前范围

- 店长 / 店员登录
- 工作台经营概览
- 团购订单列表
- 扫码核销 / 手动输入核销码
- 核销记录 / 核销异常查询
- 套餐商品管理
- 套餐图片上传
- 门店基础信息维护
- 员工权限配置
- 财务收益 / 分账流水 / 提现申请

## 目录

- `pages/workbench`：工作台
- `pages/order`：订单管理
- `pages/verify`：核销台
- `pages/goods`：商品管理
- `pages/mine`：我的
- `pages/order-detail`：订单详情
- `pages/goods-edit`：套餐编辑
- `pages/store`：门店设置
- `pages/staff`：员工权限
- `pages/verify-records`：核销记录
- `pages/finance`：财务收益

## 当前实现方式

- 先用 `data/mock.js` + `wx.setStorageSync` 维持本地演示数据
- 登录支持店长 / 店员两种角色模拟
- 登录、工作台概览、订单列表、订单详情、核销、核销记录、商品管理、套餐图片、门店设置、员工权限、财务收益已优先走后端接口，失败时自动回退本地 mock
- 商家端复用 `ruoyi-wxmini` 同一套 JWT 内核，通过 `userType=MERCHANT_STAFF` + `permissionCodes` 区分店长 / 店员，不再额外维护第二套认证体系
- 核销会记录成功 / 失败结果；失败会保留异常原因；核销前会按套餐有效期做过期校验
- 财务收益当前按 mock 规则计算：商家货款 90%，平台佣金 10%，完成订单进入 T+1 待结算 / 已结算状态
- 权限粒度已固化为：
  - `stats.view`
  - `order.manage`
  - `verify.scan`
  - `verify.manual`
  - `verify.record`
  - `goods.manage`
  - `store.manage`
  - `staff.manage`
  - `finance.manage`

## 已接入接口

- `POST /wxmini/merchant-mini/auth/login`
- `GET /wxmini/merchant-mini/workbench/overview`
- `GET /wxmini/merchant-mini/order/list`
- `GET /wxmini/merchant-mini/order/detail/{orderNo}`
- `POST /wxmini/merchant-mini/order/write-off/{code}`
- `GET /wxmini/merchant-mini/verify/record/list`
- `GET /wxmini/merchant-mini/goods/list`
- `POST /wxmini/merchant-mini/goods/save`
- `PUT /wxmini/merchant-mini/goods/status`
- `POST /wxmini/merchant-mini/goods/image/upload`
- `GET /wxmini/merchant-mini/store/profile`
- `PUT /wxmini/merchant-mini/store/profile`
- `GET /wxmini/merchant-mini/staff/list`
- `PUT /wxmini/merchant-mini/staff/permission`
- `GET /wxmini/merchant-mini/finance/overview`
- `POST /wxmini/merchant-mini/finance/withdraw`

## 未做 / 后续真实化

- 真实微信登录绑定：当前登录页仍是店长 / 店员演示选择；上线前需要用 `wx.login` 换取 `openid`，再通过商家员工表判断店长或店员身份。
- 入驻资质审核：暂未做商家入驻申请、营业执照上传、法人身份证上传、门头照上传、审核状态流转。
- 真实数据库落库：当前 `merchant-mini` 后端仍是 `MerchantMiniMockServiceImpl` 内存 mock；需要补表结构、Mapper、Service 和事务。
- 真实文件上传：当前套餐图片接口返回 mock URL；需要接若依文件上传、对象存储或服务器本地文件，并处理图片删除/替换。
- 微信支付分账 / 真实提现：当前财务按 mock 规则计算商家 90% / 平台 10%；需要接微信支付分账、T+1 结算、提现出款、退款逆向分账。
- 外卖订单管理：暂未做外卖商品分类、接单 / 拒单、配送进度、发货确认、异常订单提醒。
- 配送设置：暂未做配送范围、基础配送费、分时段配送费、自动 / 手动接单模式。
- PC 商家网页端：本目录只负责商家小程序移动端，PC 端需单独目录和后台路由。
- 平台总后台：商家审核、全平台财务、分账配置、商家/骑手全局管理不在当前小程序目录内。
