# ruoyi-mall-common — 商城公共模块

## 职责
商城各业务模块的共享基础设施，包含认证、授权、支付等通用能力。

## 核心依赖
- `weixin-java-miniapp` 4.7.0 — 微信小程序SDK
- `wx-java-pay-spring-boot-starter` 4.7.0 — 微信支付SDK
- `hutool-all` 5.8.20 — 工具库（JWT实现等）
- `ruoyi-common` — 若依公共模块
- Java 8

## 包结构
- `bo/` — 业务对象
  - `WxMiniAuthContext` — 微信小程序认证上下文（userId, userType, staffId, merchantId, storeId, roleCodes, permissionCodes）
  - `WxPayCreateOrderParam` — 支付创建订单参数
- `config/` — 微信SDK配置
  - `WxMaProperties` — 小程序配置属性
  - `WxMaConfiguration` — 创建 WxMaService / MessageRouter Bean
- `filter/` — 过滤器
  - `WxMiniJwtFilter` — 微信端JWT认证过滤器，拦截 `/wxmini/**` 路径
- `service/` — 服务接口
  - `IWxMiniJwtService` — 微信端JWT令牌创建/解析/验证（支持 userId 和 AuthContext 两种 token 创建方式）
  - `IWxPayBaseService<P>` — 微信支付模板接口（createOrder / reCreateOrder / cancelOrder / queryPayResultAndUpdOrderStatus / handlePayResult）
  - `AbsWxPayBaseService` — 支付模板抽象实现
  - `IWxPayDemoService` — 示例支付子接口，继承 `IWxPayBaseService<WxPayDemoVo>`，演示多态支付隔离模式
  - `IDashboardService` — 工作台数据聚合接口（5个方法：selectDashboardStats / selectTrendData / selectOrderStatusData / selectHotProducts / selectMerchantRank）
- `service/impl/` — 服务实现
  - `WxMiniJwtServiceImpl` — 基于 hutool JWT 的令牌实现
  - `WxPayDemoServiceImpl` — 示例支付实现
- `util/` — 工具类
  - `WxMiniUserContext` — ThreadLocal 用户上下文工具，支持获取 userId / userType / staffId / merchantId / storeId / roleCodes / permissionCodes，以及权限检查（hasPermission / hasAnyPermission）和身份判断（isMerchantStaff / isWxUser）
- `vo/` — 视图对象
  - `WxPayDemoVo` — 示例支付VO
  - `WxPayParamVo` — 支付参数VO

## 被依赖
所有 `mall-*` 模块和 `ruoyi-wxmini` 均依赖本模块。

## 使用示例
```java
// 获取当前微信用户ID
String userId = WxMiniUserContext.getCurrentUserId();

// 判断当前用户是否为微信用户
boolean isWx = WxMiniUserContext.isWxUser();

// 检查权限
boolean hasPerm = WxMiniUserContext.hasPermission("order.manage");

// 检查任意权限
boolean hasAny = WxMiniUserContext.hasAnyPermission("order.manage", "order.view");

// 获取商户ID / 门店ID
Long merchantId = WxMiniUserContext.getCurrentMerchantId();
Long storeId = WxMiniUserContext.getCurrentStoreId();

// 创建JWT令牌
String token = jwtService.createToken(userId);

// 使用AuthContext创建令牌
WxMiniAuthContext ctx = new WxMiniAuthContext();
ctx.setUserId(userId);
ctx.setUserType(WxMiniAuthContext.USER_TYPE_MERCHANT_STAFF);
String token = jwtService.createToken(ctx);
```
