# ruoyi-wxmini 模块技术文档

## 模块概述

ruoyi-wxmini是若依框架的微信小程序模块，提供了完整的微信小程序后端支持，包括微信登录、微信支付、消息推送等功能。

## 配置类

### `com.ruoyi.wxmini.config.WxMaConfiguration` -- 微信小程序配置

**主要功能**：
- 微信小程序服务配置
- 消息路由器配置
- 各种消息处理器配置

**依赖注入**：
- `WxMaProperties` - 微信小程序配置属性

**Bean定义**：
- `wxMaService()` - 微信小程序服务
- `wxMaMessageRouter()` - 消息路由器

**消息处理器**：
- `logHandler` - 日志处理器
- `subscribeMsgHandler` - 订阅消息处理器
- `textHandler` - 文本消息处理器
- `picHandler` - 图片消息处理器
- `qrcodeHandler` - 二维码处理器

---

### `com.ruoyi.wxmini.config.WxMaProperties` -- 微信小程序配置属性

**配置前缀**：`wx.miniapp`

**配置项**：
- `configs` - 配置列表

**Config内部类**：
| 字段 | 类型 | 说明 |
|------|------|------|
| appid | String | 微信小程序的appid |
| secret | String | 微信小程序的Secret |
| token | String | 微信小程序消息服务器配置的token |
| aesKey | String | 微信小程序消息服务器配置的EncodingAESKey |
| msgDataFormat | String | 消息格式，XML或者JSON |

## 控制器类

### `com.ruoyi.wxmini.controller.WxLoginController` -- 微信登录控制器

**主要接口**：
- `POST /wxmini/login` - 微信小程序登录
- `POST /wxmini/refreshToken` - 刷新Token
- `POST /wxmini/logout` - 退出登录

**功能说明**：
- 处理微信小程序的code换取openid和session_key
- 生成JWT Token
- 用户信息缓存

### `com.ruoyi.wxmini.controller.WxMaUserController` -- 微信用户控制器

**主要接口**：
- `GET /wxmini/user/info` - 获取用户信息
- `PUT /wxmini/user/info` - 更新用户信息
- `POST /wxmini/user/bind` - 绑定手机号

**功能说明**：
- 微信用户信息管理
- 用户信息缓存
- 手机号绑定

### `com.ruoyi.wxmini.controller.WxPayController` -- 微信支付控制器

**主要接口**：
- `POST /wxmini/pay/createOrder` - 创建支付订单
- `POST /wxmini/pay/notify` - 支付回调
- `POST /wxmini/pay/queryOrder` - 查询订单状态
- `POST /wxmini/pay/refund` - 申请退款

**功能说明**：
- 微信支付订单创建
- 支付结果通知处理
- 订单状态查询
- 退款处理

### `com.ruoyi.wxmini.controller.WxPortalController` -- 微信消息推送控制器

**主要接口**：
- `POST /wxmini/portal/{appid}` - 微信消息推送入口

**功能说明**：
- 处理微信服务器推送的消息
- 消息路由分发
- 自动回复处理

## 服务类

### `com.ruoyi.wxmini.service.IWxMiniJwtService` -- 微信小程序JWT服务接口

**主要方法**：
- `createToken()` - 创建Token
- `refreshToken()` - 刷新Token
- `validateToken()` - 验证Token
- `getUserInfo()` - 获取用户信息

### `com.ruoyi.wxmini.service.IWxPayBaseService` -- 微信支付基础服务接口

**主要方法**：
- `createOrder()` - 创建支付订单
- `queryOrder()` - 查询订单
- `closeOrder()` - 关闭订单
- `refundOrder()` - 申请退款

### `com.ruoyi.wxmini.service.IWxPayDemoService` -- 微信支付示例服务接口

**主要方法**：
- `createDemoOrder()` - 创建示例订单
- `processPayment()` - 处理支付
- `handleCallback()` - 处理回调

## 实现类

### `com.ruoyi.wxmini.service.impl.WxMiniJwtServiceImpl` -- 微信小程序JWT服务实现

**核心功能**：
- JWT Token生成和验证
- 用户信息缓存
- Token刷新机制
- 用户上下文管理

### `com.ruoyi.wxmini.service.impl.WxPayDemoServiceImpl` -- 微信支付示例服务实现

**核心功能**：
- 支付订单创建
- 支付结果处理
- 订单状态管理
- 支付回调处理

### `com.ruoyi.wxmini.service.AbsWxPayBaseService` -- 微信支付基础服务抽象类

**核心功能**：
- 微信支付API封装
- 统一下单接口
- 订单查询接口
- 退款接口
- 签名验证

## 数据对象

### `com.ruoyi.wxmini.bo.WxUserInfo` -- 微信用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| openId | String | 微信用户唯一标识 |
| unionId | String | 微信开放平台唯一标识 |
| nickName | String | 昵称 |
| avatarUrl | String | 头像URL |
| gender | Integer | 性别 |
| city | String | 城市 |
| province | String | 省份 |
| country | String | 国家 |
| language | String | 语言 |

### `com.ruoyi.wxmini.bo.WxPayCreateOrderParam` -- 微信支付创建订单参数

| 字段 | 类型 | 说明 |
|------|------|------|
| openId | String | 用户openId |
| outTradeNo | String | 商户订单号 |
| totalFee | Integer | 订单总金额（分） |
| body | String | 商品描述 |
| tradeType | String | 交易类型 |
| notifyUrl | String | 通知地址 |

### `com.ruoyi.wxmini.vo.WxPayDemoVo` -- 微信支付示例视图对象

| 字段 | 类型 | 说明 |
|------|------|------|
| appId | String | 小程序ID |
| timeStamp | String | 时间戳 |
| nonceStr | String | 随机字符串 |
| package | String | 统一下单接口返回的prepay_id参数值 |
| signType | String | 签名类型 |
| paySign | String | 支付签名 |

### `com.ruoyi.wxmini.vo.WxPayParamVo` -- 微信支付参数视图对象

| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单ID |
| orderNo | String | 订单号 |
| amount | BigDecimal | 支付金额 |
| description | String | 订单描述 |

## 过滤器

### `com.ruoyi.wxmini.filter.WxMiniJwtFilter` -- 微信小程序JWT过滤器

**主要功能**：
- JWT Token验证
- 用户身份认证
- 请求拦截处理
- 用户上下文设置

**处理流程**：
1. 从请求头获取Token
2. 验证Token有效性
3. 解析用户信息
4. 设置用户上下文
5. 放行或拒绝请求

## 工具类

### `com.ruoyi.wxmini.util.WxMiniUserContext` -- 微信小程序用户上下文

**主要方法**：
- `setCurrentUser()` - 设置当前用户
- `getCurrentUser()` - 获取当前用户
- `clearCurrentUser()` - 清除当前用户
- `getUserId()` - 获取用户ID
- `getOpenId()` - 获取OpenId

**功能说明**：
- 线程安全的用户上下文管理
- 基于ThreadLocal实现
- 支持多线程环境

## 配置文件

### application.yml配置示例

```yaml
# 微信小程序配置
wx:
  miniapp:
    configs:
      - appid: wx1234567890abcdef
        secret: abcdef1234567890abcdef1234567890
        token: your_token
        aesKey: your_aes_key
        msgDataFormat: JSON
```

## 数据库表结构

### 微信用户表（建议结构）

```sql
CREATE TABLE `wx_mini_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `open_id` varchar(100) NOT NULL COMMENT '微信用户唯一标识',
  `union_id` varchar(100) DEFAULT NULL COMMENT '微信开放平台唯一标识',
  `nick_name` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `language` varchar(20) DEFAULT NULL COMMENT '语言',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信小程序用户表';
```

### 微信支付订单表（建议结构）

```sql
CREATE TABLE `wx_pay_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '商户订单号',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付订单号',
  `open_id` varchar(100) NOT NULL COMMENT '用户openId',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `description` varchar(500) DEFAULT NULL COMMENT '订单描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待支付 1已支付 2已关闭 3已退款）',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付订单表';
```

## 使用示例

### 微信登录

```java
@PostMapping("/login")
public AjaxResult login(@RequestBody WxLoginParam param) {
    // 1. 通过code获取session_key和openid
    WxMaJscode2SessionResult session = wxMiniService.jsCode2Session(param.getCode());
    
    // 2. 创建或更新用户信息
    WxMiniUser user = wxMiniUserService.saveOrUpdateUser(session);
    
    // 3. 生成JWT Token
    String token = wxMiniJwtService.createToken(user);
    
    // 4. 返回Token和用户信息
    return AjaxResult.success(Map.of(
        "token", token,
        "user", user
    ));
}
```

### 微信支付

```java
@PostMapping("/createOrder")
public AjaxResult createOrder(@RequestBody WxPayCreateOrderParam param) {
    // 1. 创建支付订单
    WxPayOrder order = wxPayService.createOrder(param);
    
    // 2. 调用微信支付统一下单
    WxPayUnifiedOrderResult result = wxPayService.unifiedOrder(order);
    
    // 3. 生成小程序支付参数
    WxPayDemoVo payVo = wxPayService.createPayParam(result);
    
    return AjaxResult.success(payVo);
}
```

### 消息推送

```java
@PostMapping("/portal/{appid}")
public String portal(@PathVariable String appid, @RequestBody String requestBody) {
    // 1. 解析微信消息
    WxMaMessage message = wxMaService.parseMessage(requestBody);
    
    // 2. 路由消息到对应处理器
    WxMaMessageRouter router = wxMaMessageRouter(wxMaService);
    WxMaXmlOutMessage outMessage = router.route(message);
    
    // 3. 返回响应消息
    return outMessage != null ? outMessage.toXml() : "";
}
```

## 注意事项

1. **配置安全**：微信小程序的AppSecret要妥善保管
2. **Token管理**：JWT Token要有合理的过期时间
3. **支付安全**：支付回调要验证签名
4. **消息处理**：消息推送要在5秒内响应
5. **用户隐私**：用户信息要符合隐私政策
6. **错误处理**：网络请求要有重试机制
7. **日志记录**：关键操作要有日志记录
8. **数据缓存**：用户信息可以适当缓存

## 依赖说明

### 主要依赖

```xml
<!-- 微信小程序SDK -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-miniapp</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- 微信支付SDK -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-pay</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

## 版本信息

- **模块版本**: 4.6.0
- **微信小程序SDK版本**: 4.5.0
- **Java版本**: JDK 1.8+
- **Spring Boot版本**: 2.5.x
- **作者**: ruoyi
