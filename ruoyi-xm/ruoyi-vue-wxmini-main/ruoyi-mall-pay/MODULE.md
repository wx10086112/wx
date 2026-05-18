# ruoyi-mall-pay 支付模块

## 职责

微信小程序支付集成，处理支付下单、回调通知、订单查询。

## 当前状态

**基础实现** — PaymentRecord 领域模型已定义，WxPayController 已搭建框架（核心逻辑仍为 TODO）。

## 包结构

```
com.ruoyi.mall.pay
├── controller
│   └── WxPayController.java        # 微信支付接口（3 个端点）
└── domain
    └── PaymentRecord.java          # 支付记录：orderNo, merchantId, userId, amount,
                                     #           payType, transactionId, outTradeNo,
                                     #           payStatus, payTime, notifyResult
```

共 2 个类。

## Controller 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/wxmini/pay/order/create` | 创建支付订单（接收 WxPayCreateOrderParam） |
| GET  | `/wxmini/pay/order/query`  | 查询支付订单状态（按 orderNo） |
| POST | `/wxmini/pay/notify`       | 微信支付回调通知（解析 XML，返回应答） |

> 注：三个端点核心逻辑目前均为 TODO 占位。

## 模块依赖

| 依赖 | 说明 |
|------|------|
| ruoyi-mall-common | 公共模块（WxMiniUserContext、基础实体） |
| ruoyi-mall-order  | 订单模块（支付与订单联动） |

## 规划中的功能

- 统一下单完整实现（调用 WxPayService）
- 回调处理：验签、更新 PaymentRecord 和订单状态
- 退款接口
- 支付统计与对账
