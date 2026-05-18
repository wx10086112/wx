# ruoyi-mall-user — 用户域模块

## 职责
微信小程序用户管理：登录注册、用户信息、收货地址、购物车、收藏、优惠券。

## 包结构
- `domain/` — 实体类
  - UserInfo — 用户信息（userId, userName, userType, phone, openId, unionId, avatarUrl）
  - MallUser — 后台用户统计表（nickname, phone, avatar, gender, city, openId, status, totalOrders, totalAmount）
  - UserAddress — 收货地址（userId, name, phone, province/city/district/detail, isDefault）
  - Cart — 购物车（userId productId, merchantId, quantity, checked）
  - UserCoupon — 用户优惠券（userId, couponId, merchantId, status, useTime, orderNo）
  - UserFavorite — 收藏（userId, targetType, targetId）
- `bo/` — WxUserInfo（sessionKey, openId, userName, userType, phone, avatarUrl, apiToken；wapper() 方法封装微信会话+用户信息）
- `mapper/` — UserInfoMapper, MallUserMapper
  - UserInfoMapper 按 openId / userId 查询
  - MallUserMapper 含 `countTotal()` / `countTodayNew()` 统计方法（@Select 注解）
- `service/` — IUserInfoService + UserInfoServiceImpl
  - selectUserInfoByUserId() 优先读 Redis 缓存（key: `wx_user:{userId}`），miss 后回源 DB 并写回缓存
  - insert/update 操作同步更新 Redis 缓存
- `controller/` — 4 个控制器

## 接口列表
| 控制器 | 方法 | 路径 | 说明 |
|--------|------|------|------|
| WxLoginController | GET | /wxmini/login | 微信登录，自动注册新用户，返回 WxUserInfo（含 JWT apiToken） |
| WxMaUserController | GET | /wxmini/user/info | 获取/更新微信用户信息（头像、昵称） |
| WxMaUserController | GET | /wxmini/user/phone | 获取并绑定用户手机号 |
| WxPortalController | GET | /wxmini/portal/{appid} | 微信服务器 GET 验证回调 |
| WxPortalController | POST | /wxmini/portal/{appid} | 微信服务器 POST 消息回调，路由至 WxMaMessageRouter |
| MallUserController | GET | /mall/user/list | 后台用户列表（需权限 mall:user:list，支持分页） |

## 被依赖
- `ruoyi-admin`: DashboardServiceImpl 注入 MallUserMapper，用于 dashboard 用户统计

## 核心流程
```
微信登录：code -> wxMaService.getSessionInfo(code) -> 查 UserInfo（openId）
  -> 不存在则自动注册（UUID.userId + openId + unionId）
  -> wapper() 封装 WxUserInfo -> jwtService.createToken(userId) 生成令牌
  -> 返回 { sessionKey, openId, userName, userType, phone, avatarUrl, apiToken }
```

## 使用示例
```java
// 微信登录
GET /wxmini/login?appid=xxx&code=jscode
// 返回: AjaxResult { openId, sessionKey, apiToken, userId, userName }

// 通过 openId 查询用户
UserInfo user = userInfoService.selectUserInfoByOpenId(openId);
```
