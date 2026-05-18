# ruoyi-mall-marketing 营销模块

## 职责

营销活动管理，负责轮播图（Banner）和优惠券（Coupon）的领域模型定义。

## 当前状态

**预留模块（Stub）** — 仅定义了领域实体，无 Mapper、Service、Controller 实现。

## 包结构

```
com.ruoyi.mall.marketing
└── domain
    ├── Banner.java        # 轮播图：title, image, linkType/linkId/linkUrl, sort, status, position
    └── Coupon.java        # 优惠券：merchantId, name, type, discountValue, minAmount,
                            #         totalCount, usedCount, startTime/endTime, status
```

共 2 个类。

## 模块依赖

| 依赖 | 说明 |
|------|------|
| ruoyi-mall-common | 公共模块（基础实体、工具类） |

## 规划中的功能

- Banner 管理：CRUD、排序、上下架、按位置投放
- 优惠券管理：模板创建、发放、领取、核销、过期处理
- 营销活动（满减、折扣等扩展）
