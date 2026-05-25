# 微信小程序模板项目说明

## 项目定位

本项目是一套 **可复用的微信小程序前台模板**。

它的目标不是做“平台型多商户前台”，而是作为一套可以 **复用给不同商家** 的 C 端小程序模板。  
不同商家可以基于这套模板生成自己的小程序前台，展示自己的品牌、门店、商品、套餐、订单和营销内容。

## 系统整体架构

完整业务体系分为 4 个端：

### 1. C 端微信小程序

面向消费者使用。

主要功能：

- 首页浏览
- 门店/分店查看
- 商品/团购套餐浏览
- 下单支付
- 订单查看
- 到店核销
- 优惠券/收藏/地址/评价

### 2. 商家管理小程序

面向商家或店员使用。

主要功能通常包括：

- 商家资料维护
- 商品上架下架
- 订单处理
- 核销
- 活动配置
- 门店运营

### 3. 商家网页管理端

面向商家后台运营。

主要功能通常包括：

- 品牌资料管理
- 门店管理
- 商品/套餐管理
- 活动管理
- 订单管理
- 用户评价管理
- 数据统计

### 4. 总后台管理端

面向平台运营方，用于统一管理所有商家。

主要功能通常包括：

- 商家管理
- 商家审核
- 全局配置
- 统一运营
- 财务/风控/审计
- 模板配置

## 当前仓库范围

当前仓库 **只需要负责微信小程序前台**。

也就是：

- 做消费者使用的小程序端
- 不做商家管理小程序
- 不做网页端商家后台
- 不做平台总后台

因此，当前前端设计应只围绕 **C 端用户交易闭环** 展开。

## 模板与 B 端配置关系

这个项目必须始终按 **统一模板** 理解，不是按“单次定制项目”理解。

也就是：

- C 端微信小程序负责渲染统一模板
- 商家不能直接改 C 端代码
- 商家的页面内容、文案、Banner、门店、商品、套餐、菜单入口、规则说明，应由 **另一个 B 端商家管理小程序** 维护
- 商家管理小程序保存的配置，再由后端接口下发给当前 C 端模板

因此，当前 C 端前端的正确职责不是“写死某个商家的页面”，而是：

- 提供稳定的模板结构
- 定义可配置模块
- 按配置渲染页面
- 接收 B 端配置后的业务数据与 UI 文案

可以把当前 C 端理解成：

- 一套前台模板壳
- 一个渲染容器
- 一个消费者交易闭环客户端

而不是商家自己改源码的前台工程。

## 与平台型多商户前台的区别

这个项目不是“美团/大众点评式平台前台”。

平台型前台的特点是：

- 一个小程序里同时展示很多不同商家
- 用户先选商家，再选商品
- 平台负责统一分发流量

而本项目不是这种模式。

本项目更接近：

- 同一套模板可复用给多个商家
- 每个商家最终有自己的前台小程序
- 用户进入的是某个商家的小程序前台
- 只看该商家的品牌、门店、商品和订单

## 对前台小程序的产品含义

由于这是“商家专属前台模板”，所以页面中的业务含义要做如下修正：

### 1. 首页

首页不应该理解为“平台商家广场”，而应该理解为：

- 当前商家的品牌首页
- 当前商家的门店推荐页
- 当前商家的商品/套餐推荐页

如果商家是连锁门店，则首页的 LBS 能力应服务于：

- 自动定位最近门店
- 查看该商家名下附近分店
- 切换当前服务门店

不是跨品牌搜索所有商家。

### 2. 搜索

搜索建议定义为：

- 搜门店
- 搜套餐
- 搜单品

而不是“跨平台搜所有商家”。

### 3. 商家详情页

如果一个商家只有一个门店，这页可以直接是“商家详情页”。  
如果一个商家有多门店，这页更准确地说应该是：

- 门店详情页
- 分店详情页

它展示的是 **该商家某个门店** 的营业信息、在售商品和评价。

## 项目目录

### 后端

目录：[ruoyi-vue-wxmini-main](F:/wx/ruoyi-xm/ruoyi-vue-wxmini-main)

该目录基于若依框架扩展了微信小程序能力，主要包含：

- `ruoyi-admin`
- `ruoyi-common`
- `ruoyi-framework`
- `ruoyi-system`
- `ruoyi-generator`
- `ruoyi-quartz`
- `ruoyi-wxmini`

其中：

- `ruoyi-wxmini` 负责微信登录、微信支付、微信用户体系、小程序接口鉴权等基础能力
- 它更像“微信接入模块”，不是完整的本地生活业务模块

### 小程序前端

目录：[wx_app](F:/wx/ruoyi-xm/wx_app)

当前是原生微信小程序项目，已有基础目录：

- [app.json](F:/wx/ruoyi-xm/wx_app/app.json)
- [api](F:/wx/ruoyi-xm/wx_app/api)
- [pages](F:/wx/ruoyi-xm/wx_app/pages)
- [components](F:/wx/ruoyi-xm/wx_app/components)
- [utils](F:/wx/ruoyi-xm/wx_app/utils)

## 当前小程序已有页面

当前前端已有页面包括：

- `pages/home/home`
- `pages/order/order`
- `pages/order-detail/order-detail`
- `pages/mine/mine`
- `pages/merchant-detail/merchant-detail`
- `pages/product-detail/product-detail`
- `pages/checkout/checkout`
- `pages/search/search`
- `pages/search-result/search-result`
- `pages/coupon/coupon`
- `pages/favorite/favorite`
- `pages/address/address-list`
- `pages/address/address-edit`
- `pages/review/review-list`
- `pages/review/review-create`
- `pages/contact/contact`
- `pages/join/join-apply`

当前已经完成主 Tab 收敛和核心二级页拆分，后续主要是继续对接真实接口和后台配置。

## 小程序模板建议页面结构

基于当前项目定位，前台小程序建议拆成以下结构。

### 主 Tab 页面

1. 首页
2. 订单
3. 我的

### 核心二级页面

1. 门店详情页
2. 商品/套餐详情页
3. 订单确认/结算页
4. 订单详情页
5. 优惠券页
6. 收藏页
7. 地址管理页
8. 评价页
9. 客服页

## 商家后台应可配置的前台内容

虽然当前仓库不做商家管理端，但前台页面设计必须考虑这些字段未来由后台配置下发。

更准确地说，这些字段未来应由：

- 商家管理小程序
- 商家网页管理端
- 或平台总后台

统一维护，再下发给 C 端模板。

### 商家品牌信息

- 商家名称
- Logo
- 品牌介绍
- 联系方式

### 门店信息

- 门店名称
- 门头图
- 营业时间
- 地址
- 经纬度
- 电话

### 首页内容

- Banner
- 推荐商品
- 热门套餐
- 公告文案
- 活动图
- 首页模块开关
- 排序项配置
- 分类项配置
- 服务门店展示策略

### 商品/套餐信息

- 标题
- 原价 / 现价
- 销量
- 内容说明
- 使用规则
- 有效期
- 预约说明
- 退款规则
- 详情页按钮文案
- 是否展示预约说明 / 限购说明 / 退款说明

### 营销内容

- 优惠券
- 红包
- 秒杀/限时活动
- 推荐位配置

### 页面模板配置

- 首页各区块标题、副标题
- 我的页菜单入口
- 我的页资产卡片入口
- 门店详情页按钮文案
- 商品详情页购买按钮文案
- 结算页说明文案
- 客服入口文案
- 入驻申请入口文案

### 主题与视觉配置

- 品牌主色
- Logo
- 首页 Banner 风格图
- 品牌 Slogan
- 公告文案

## 前台小程序应该解决的核心闭环

当前小程序端需要完成的，是消费者交易闭环：

1. 进入小程序
2. 查看商家品牌和门店
3. 浏览商品/套餐
4. 查看使用规则
5. 提交订单
6. 微信支付
7. 查看订单
8. 到店核销 / 使用
9. 售后 / 退款 / 评价

## 当前开发边界

当前只做微信小程序前台，所以要明确边界：

### 在范围内

- 页面设计
- 交互流程
- 前台接口对接
- 用户下单支付链路
- 订单状态流转展示
- 到店核销展示
- 用户资产与权益

### 不在范围内

- 商家管理端页面
- 网页端商家后台
- 平台总后台
- 商家审核流程后台页面

## 后续开发建议

建议按以下顺序推进微信小程序前台：

1. 重构导航结构
   当前 `product` 不适合继续做一级 Tab，应收敛为首页 / 订单 / 我的

2. 重构首页
   强化品牌展示、门店定位、Banner、筛选、商品推荐

3. 完善门店详情页和商品详情页
   这是转化核心

4. 增加结算页与支付流程
   打通完整下单链路

5. 完善订单页
   包含待支付、待使用、退款/售后、评价等状态

6. 补齐用户中心能力
   优惠券、收藏、地址、客服、评价等

## 总结

这套项目应当理解为：

- 一套可复用给不同商家的微信小程序前台模板
- 多个商家都可以套用
- 商家侧还有独立的小程序管理端、网页管理端和总后台
- 当前仓库只负责微信小程序前台

所以当前的产品设计和技术设计，都应该围绕 **商家专属前台模板** 来展开，而不是误判成“平台型多商户前台”。

---

## 微信小程序前台功能清单

本章节用于指导当前仓库的微信小程序前台开发，重点覆盖：

- 页面清单
- 路由清单
- 接口清单
- 状态枚举
- 核心数据字段

## 一、页面清单

### A. 主 Tab 页面

#### 1. 首页 `Home`

页面作用：

- 展示当前商家的品牌首页
- 展示当前商家门店/分店信息
- 展示主推套餐、商品、活动内容

页面模块建议：

- LBS 自动定位栏
- 当前门店/最近门店展示
- 全局搜索栏
- Banner 轮播图
- 分类/筛选/排序栏
- 商品/套餐列表

#### 2. 订单页 `Orders`

页面作用：

- 展示用户在当前商家小程序内的全部订单
- 承接支付、核销、退款、评价等动作

页面模块建议：

- 状态 Tab
- 订单卡片列表
- 空状态页
- 订单操作按钮

#### 3. 我的页 `Profile`

页面作用：

- 展示用户信息、订单快捷入口、资产权益和常用服务

页面模块建议：

- 用户信息卡片
- 订单快捷入口
- 优惠券/红包入口
- 收货地址入口
- 收藏入口
- 客服入口
- 入驻申请入口

### B. 二级页面

#### 4. 门店详情页 `Merchant Detail`

页面作用：

- 展示当前商家某个门店/分店的完整信息

页面模块建议：

- 门头图 / Logo
- 门店名称
- 营业时间
- 商家电话
- 门店地址
- 导航按钮
- 商品/套餐列表
- 用户评价列表

#### 5. 商品/套餐详情页 `Product Detail`

页面作用：

- 承接用户的购买决策

页面模块建议：

- 商品主图
- 标题/副标题
- 原价 / 活动价
- 销量
- 商品详情说明
- 使用规则
- 有效期
- 预约说明
- 退款规则
- 收藏 / 分享 / 立即抢购

#### 6. 订单确认页 `Checkout`

页面作用：

- 用户付款前最后确认页

页面模块建议：

- 商品摘要
- 数量增减
- 用户手机号
- 优惠券选择
- 实付金额
- 提交订单
- 微信支付

#### 7. 订单详情页 `Order Detail`

页面作用：

- 查看单笔订单详情
- 展示核销码
- 展示订单状态和后续动作

页面模块建议：

- 订单状态
- 商品信息
- 商家信息
- 核销码 / 二维码
- 支付信息
- 下单时间
- 操作按钮

#### 8. 优惠券页 `Coupon`

- 我的优惠券
- 红包列表
- 可用 / 已使用 / 已过期

#### 9. 地址管理页 `Address`

- 地址列表
- 新增地址
- 编辑地址
- 删除地址
- 默认地址

#### 10. 收藏页 `Favorite`

- 收藏的商品
- 收藏的门店

#### 11. 评价页 `Review`

- 评价列表
- 订单完成后评价入口
- 评分、文字评价、图片上传

#### 12. 客服页 `Contact`

- 联系电话
- 在线客服
- 售后入口

#### 13. 入驻申请页 `Join Apply`

- 面向有资源的用户发起商家合作申请

## 二、建议路由清单

当前 `app.json` 需要从现有结构继续演进，建议最终整理为如下路由：

### 主 Tab

- `pages/home/home`
- `pages/order/order`
- `pages/mine/mine`

### 二级页面

- `pages/search/search`
- `pages/search-result/search-result`
- `pages/merchant-detail/merchant-detail`
- `pages/product-detail/product-detail`
- `pages/checkout/checkout`
- `pages/order-detail/order-detail`
- `pages/coupon/coupon`
- `pages/address/address-list`
- `pages/address/address-edit`
- `pages/favorite/favorite`
- `pages/review/review-list`
- `pages/review/review-create`
- `pages/contact/contact`
- `pages/join/join-apply`

### 当前已有路由与建议调整

当前已有：

- `pages/home/home`
- `pages/order/order`
- `pages/order-detail/order-detail`
- `pages/mine/mine`
- `pages/merchant-detail/merchant-detail`
- `pages/product-detail/product-detail`
- `pages/checkout/checkout`
- `pages/search/search`
- `pages/search-result/search-result`
- `pages/coupon/coupon`
- `pages/favorite/favorite`
- `pages/address/address-list`
- `pages/address/address-edit`
- `pages/review/review-list`
- `pages/review/review-create`
- `pages/contact/contact`
- `pages/join/join-apply`

建议：

- 保留 `home/order/mine`
- `product` 已从主 Tab 移除，并重构为 `product-detail`
- 结算页、搜索页、优惠券页、地址页、收藏页、评价页、客服页、入驻页已补齐

## 三、接口清单

当前前端已有接口模块：

- [api/merchant.js](F:/wx/ruoyi-xm/wx_app/api/merchant.js)
- [api/product.js](F:/wx/ruoyi-xm/wx_app/api/product.js)
- [api/order.js](F:/wx/ruoyi-xm/wx_app/api/order.js)
- [api/user.js](F:/wx/ruoyi-xm/wx_app/api/user.js)

但要满足完整模板需求，还需要继续扩展。

### 1. 用户相关

- `GET /wxmini/login`
- `GET /wxmini/user/info`
- `PUT /wxmini/user/info`

建议新增：

- `GET /wxmini/user/coupon/list`
- `GET /wxmini/user/favorite/list`
- `POST /wxmini/user/favorite/toggle`
- `GET /wxmini/user/address/list`
- `POST /wxmini/user/address/save`
- `PUT /wxmini/user/address/update`
- `DELETE /wxmini/user/address/delete/{id}`

### 2. 门店相关

当前已有：

- `GET /wxmini/merchant/list`
- `GET /wxmini/merchant/detail/{id}`
- `GET /wxmini/merchant/album/{merchantId}`

建议新增：

- `GET /wxmini/merchant/current`
- `GET /wxmini/merchant/review/list`
- `GET /wxmini/merchant/branch/list`

### 3. 商品/套餐相关

当前已有：

- `GET /wxmini/groupon/list`
- `GET /wxmini/groupon/detail/{id}`
- `GET /wxmini/product/list`
- `GET /wxmini/product/detail/{id}`

建议新增：

- `GET /wxmini/home/index`
- `GET /wxmini/home/banner`
- `GET /wxmini/category/list`
- `GET /wxmini/search`

### 4. 订单相关

当前已有：

- `GET /wxmini/order/list`
- `GET /wxmini/order/detail/{id}`
- `POST /wxmini/order/create`
- `POST /wxmini/order/cancel/{orderNo}`
- `POST /wxmini/order/writeOff/{code}`

建议新增：

- `POST /wxmini/order/preview`
- `POST /wxmini/order/refund/apply`
- `POST /wxmini/order/review/create`
- `POST /wxmini/order/confirm-use`

### 5. 支付相关

当前已有：

- `POST /wxmini/pay/order/create`
- `GET /wxmini/pay/order/query`

建议新增：

- `POST /wxmini/pay/order/unified`
- `POST /wxmini/pay/notify`

### 6. 微信订阅消息相关

建议新增：

- `POST /wxmini/subscribe/accept`
- `POST /wxmini/subscribe/send/paySuccess`
- `POST /wxmini/subscribe/send/writeOffSuccess`
- `POST /wxmini/subscribe/send/expireRemind`
- `POST /wxmini/subscribe/send/refundProgress`

## 四、订单状态枚举建议

当前 mock 数据中的订单状态较简单：

- `PENDING`
- `PAID`
- `COMPLETED`
- `CANCELLED`

为了支撑完整业务，建议整理为以下状态体系。

### 1. 订单主状态

- `PENDING_PAY`：待支付
- `PAID_UNUSED`：已支付待使用
- `USED_COMPLETED`：已核销已完成
- `REFUNDING`：退款中
- `REFUNDED`：已退款
- `CANCELLED`：已取消
- `CLOSED`：超时关闭

### 2. 前台订单 Tab 状态

- `ALL`
- `PENDING_PAY`
- `UNUSED`
- `AFTER_SALE`

说明：

- `UNUSED` 对应已支付未核销订单
- `AFTER_SALE` 包含退款中、已退款、售后处理中订单

### 3. 订单操作按钮建议

#### 待支付

- 去支付
- 取消订单

#### 待使用

- 查看核销码
- 申请退款

#### 已完成

- 去评价
- 再次购买

#### 售后中

- 查看退款进度

## 五、核心数据字段清单

## 1. 商家/门店字段

- `merchantId`
- `merchantName`
- `logo`
- `coverImage`
- `rating`
- `sales`
- `phone`
- `businessHours`
- `address`
- `latitude`
- `longitude`
- `distance`
- `tags`
- `notice`

## 2. 商品/套餐字段

- `productId`
- `merchantId`
- `title`
- `subtitle`
- `coverImage`
- `originalPrice`
- `salePrice`
- `sales`
- `stock`
- `description`
- `contentDetail`
- `validStartTime`
- `validEndTime`
- `bookingRequired`
- `bookingRule`
- `refundRule`
- `limitRule`
- `shareTitle`
- `shareImage`

## 3. 订单字段

- `orderId`
- `orderNo`
- `userId`
- `merchantId`
- `productId`
- `productTitle`
- `coverImage`
- `buyCount`
- `orderAmount`
- `couponAmount`
- `payAmount`
- `status`
- `writeOffCode`
- `writeOffQrCode`
- `createTime`
- `payTime`
- `writeOffTime`
- `expireTime`
- `refundTime`
- `refundReason`

## 4. 用户字段

- `userId`
- `openId`
- `nickName`
- `avatarUrl`
- `phone`
- `gender`
- `createTime`

## 5. 优惠券字段

- `couponId`
- `couponName`
- `couponType`
- `amount`
- `thresholdAmount`
- `validStartTime`
- `validEndTime`
- `status`

## 6. 地址字段

- `addressId`
- `consignee`
- `phone`
- `province`
- `city`
- `district`
- `detailAddress`
- `latitude`
- `longitude`
- `isDefault`

## 六、页面与接口映射建议

### 首页

建议接口：

- `GET /wxmini/home/index`
- `GET /wxmini/merchant/current`
- `GET /wxmini/category/list`
- `GET /wxmini/template/config`

### 门店详情页

建议接口：

- `GET /wxmini/merchant/detail/{id}`
- `GET /wxmini/merchant/review/list`
- `GET /wxmini/product/list`

### 商品详情页

建议接口：

- `GET /wxmini/product/detail/{id}`

### 结算页

建议接口：

- `POST /wxmini/order/preview`
- `POST /wxmini/order/create`
- `POST /wxmini/pay/order/create`

### 订单页

建议接口：

- `GET /wxmini/order/list`
- `POST /wxmini/order/cancel/{orderNo}`
- `POST /wxmini/order/refund/apply`

### 我的页

建议接口：

- `GET /wxmini/user/info`
- `GET /wxmini/user/coupon/list`
- `GET /wxmini/user/favorite/list`
- `GET /wxmini/user/address/list`

## 九、统一模板配置下发建议

为了确保“商家在另一个 B 端小程序里改内容，C 端自动生效”，建议后端增加统一模板配置接口。

### 建议接口

- `GET /wxmini/template/config`

### 当前代码落点

前端模板统一数据层已拆为：

- [services/template/default-template.js](F:/wx/ruoyi-xm/wx_app/services/template/default-template.js)
- [services/template/mock-template-source.js](F:/wx/ruoyi-xm/wx_app/services/template/mock-template-source.js)
- [services/template/template-service.js](F:/wx/ruoyi-xm/wx_app/services/template/template-service.js)
- [api/template.js](F:/wx/ruoyi-xm/wx_app/api/template.js)

后端模板 DTO 已定义在：

- [dto/template](F:/wx/ruoyi-xm/ruoyi-vue-wxmini-main/ruoyi-wxmini/src/main/java/com/ruoyi/wxmini/dto/template)

当前含义是：

- `default-template.js`：平台默认模板 DTO
- `mock-template-source.js`：当前本地模拟的商家模板覆盖配置
- `template-service.js`：前端统一模板读取入口，后续切真实接口时只改这一层
- `api/template.js`：C 端读取模板配置接口

### 建议返回内容

- `templateMeta`
- `brandInfo`
- `homeConfig`
- `profileConfig`
- `merchantDetailConfig`
- `productDetailConfig`
- `checkoutConfig`

为了和当前前端实现保持一致，建议最终返回 key 使用：

- `templateMeta`
- `brandInfo`
- `home`
- `profile`
- `merchantDetail`
- `productDetail`
- `checkout`
- `featureToggle`

### 建议职责边界

#### B 端商家管理小程序负责

- 维护品牌资料
- 维护门店资料
- 维护商品/套餐
- 维护 Banner
- 维护分类
- 维护首页推荐位
- 维护我的页菜单入口
- 维护按钮文案和说明文案
- 维护是否展示某些模块

#### C 端模板负责

- 按模板配置渲染页面
- 按业务数据渲染门店、商品、订单
- 承接消费者下单、支付、核销、退款、评价流程
- 不承担商家配置编辑职责

### 当前前端已按该方向处理的页面

- 首页
- 我的页
- 门店详情页
- 商品详情页
- 结算页

这些页面已适合后续替换为真实 `template config + business data` 接口模式。

### 当前 DTO 固化字段

已在后端 DTO 中固定以下配置对象：

- `TemplateMetaDto`
- `BrandConfigDto`
- `HomeConfigDto`
- `ProfileConfigDto`
- `MerchantDetailConfigDto`
- `ProductDetailConfigDto`
- `CheckoutConfigDto`
- `FeatureToggleDto`

以及以下子项 DTO：

- `TemplateOptionDto`
- `ProfileOrderEntryDto`
- `ProfileAssetEntryDto`
- `ProfileMenuDto`

## 七、消息通知能力

消息通知不是独立页面，但必须作为前台能力预留。

### 需要支持的通知类型

- 支付成功通知
- 核销成功提醒
- 团购券即将过期提醒
- 退款进度通知

### 前台需做的事情

- 请求订阅消息授权
- 保存用户订阅选择
- 在关键节点引导用户授权

### 后端需提供的事情

- 模板 ID 管理
- 发送记录
- 失败重试
- 定时提醒任务

## 八、当前前端现状与改造重点

### 当前已有

- 首页路由与核心模块已重组
- 商家详情页与商品详情页已打通
- 结算页已具备数量、手机号、优惠券、金额计算能力
- 订单页已具备待支付、待使用、退款/售后、核销完成链路
- 我的页已具备订单快捷入口、资产权益和服务菜单
- 微信登录占位逻辑
- 模板配置统一数据层
- Mock 数据结构

### 当前不足

- 真实接口尚未接入
- 模板配置接口还没有后端 controller/service 落地
- 二级页面仍以 mock 数据驱动为主
- 微信支付、订阅消息、真实核销回调还未联调

### 现阶段优先级

1. 路由结构重组
2. 首页模块完善
3. 商品详情与结算页
4. 订单状态与核销链路
5. 我的页权益模块
