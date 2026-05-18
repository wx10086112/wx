# ruoyi-mall-order 订单模块

## 职责

管理微信团购商城的**订单**和**售后/退款**核心业务：订单 CRUD、订单项查询、退款审核、异常单/售后单列表、销售统计查询。

## 包结构

```
com.ruoyi.mall.order
├── controller
│   ├── MallOrderController        # 订单管理接口
│   ├── MallOrderExtendController  # 异常单/售后单列表（扩展）
│   └── MallAfterSaleController    # 售后/退款审核接口
├── domain
│   ├── MallOrder      # 订单主表
│   ├── OrderItem      # 订单项
│   └── RefundRecord   # 退款记录
├── mapper
│   ├── MallOrderMapper
│   ├── OrderItemMapper
│   └── RefundRecordMapper
└── service
    ├── IMallOrderService
    └── impl/MallOrderServiceImpl
```

### domain 类名和关键字段

| 类 | 关键字段 |
|---|---|
| **MallOrder** | `id`, `orderNo`, `merchantId`, `userId`, `storeId`, `totalAmount`, `payAmount`, `commission`, `merchantIncome`, `couponId`, `couponAmount`, `grouponId`, `status`(0待付款/1待使用/2已使用/3已完成/4售后/5异常/6已取消), `writeOffCode`, `payTime`, `useTime`, `completeTime`, `cancelTime`, `refundTime` |
| **OrderItem** | `id`, `orderId`, `orderNo`, `merchantId`, `productId`, `productName`, `productImage`, `price`, `quantity`, `subtotal` |
| **RefundRecord** | `id`, `orderNo`, `refundNo`, `merchantId`, `userId`, `paymentRecordId`, `refundAmount`, `refundReason`, `refundType`, `status`(0待审核/1审核通过/2已退款/3已拒绝), `auditTime`, `refundTime`, `rejectReason`, `operator` |

### mapper 关键方法

**MallOrderMapper** — `selectMallOrderById`, `selectMallOrderByOrderNo`, `selectMallOrderList`, `selectMallOrderByMerchantId`, `insertMallOrder`, `updateMallOrder`, `deleteMallOrderById`, `countOrderByMerchantIdAndStatus`, `selectSalesStats`(总单量/总金额/总佣金/总商家收入), `selectOrderStatsByStatus`(按状态分组统计), `selectDailyOrderStats`(近30天每日统计), `countByStatus`, `countTodayOrders`, `sumTodayAmount`, `sumTotalFlow`, `selectDailyStatsForWeek`

**OrderItemMapper** — `selectOrderItemById`, `selectOrderItemList`, `selectOrderItemByOrderId`, `selectOrderItemByOrderNo`, `insertOrderItem`, `updateOrderItem`, `deleteOrderItemById`

**RefundRecordMapper** — `selectRefundRecordById`, `selectRefundRecordByRefundNo`, `selectRefundRecordList`, `insertRefundRecord`, `updateRefundRecord`, `deleteRefundRecordById`, `sumRefundTotal`(已退款总额)

### service 方法（IMallOrderService）

| 方法 | 说明 |
|---|---|
| `selectMallOrderById(Long id)` | 按ID查订单 |
| `selectMallOrderList(MallOrder)` | 条件查询订单列表 |
| `updateMallOrder(MallOrder)` | 更新订单（含 updateTime） |
| `selectOrderItemListByOrderId(Long)` | 按订单ID查订单项 |
| `selectOrderItemListByOrderNo(String)` | 按订单号查订单项 |
| `selectRefundList(RefundRecord)` | 条件查询退款记录 |
| `selectRefundById(Long id)` | 按ID查退款记录 |
| `handleRefund(Long id, Integer status, String operator, String rejectReason)` | 审批退款（通过/拒绝） |

### controller 接口

| 接口路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `GET /mall/order/list` | `list` | `mall:order:list` | 分页查询订单列表 |
| `GET /mall/order/{id}` | `getInfo` | `mall:order:query` | 查询订单详情（含订单项） |
| `PUT /mall/order` | `edit` | `mall:order:edit` | 修改订单 |
| `GET /mall/order/abnormal/list` | `abnormalList` | `mall:order:list` | 异常订单列表(status=5) |
| `GET /mall/order/after-sale/list` | `afterSaleList` | `mall:order:list` | 售后订单列表(status=4) |
| `GET /mall/after-sale/list` | `list` | `mall:order:list` | 分页查询退款记录 |
| `GET /mall/after-sale/{id}` | `getInfo` | `mall:order:query` | 退款详情 |
| `POST /mall/after-sale/handle/{id}/{status}` | `handle` | `mall:order:edit` | 审批退款 |

## 模块依赖

- **ruoyi-mall-common** — 通用工具类、常量
- **ruoyi-mall-product** — 商品模块（订单关联商品信息）

## 被依赖

以下模块在 pom.xml 中声明了对 `ruoyi-mall-order` 的依赖：

| 模块 | 引用方式 |
|---|---|
| **ruoyi-admin** | 注入 `MallOrderMapper`，用于 `DashboardServiceImpl`（后台仪表盘统计）、`MallDashboardExtendController` |
| **ruoyi-mall-finance** | 注入 `RefundRecordMapper`，用于 `MallFinanceExtendController`（财务退款统计） |
| **ruoyi-mall-pay** | pom 依赖但暂未 import 本模块类 |

## 使用示例

```java
// 后台仪表盘统计今日订单数
@Autowired
private MallOrderMapper mallOrderMapper;

int todayOrders = mallOrderMapper.countTodayOrders();
BigDecimal todayAmount = mallOrderMapper.sumTodayAmount();

// 查看订单详情（含订单项）
@Autowired
private IMallOrderService orderService;

MallOrder order = orderService.selectMallOrderById(1L);
List<OrderItem> items = orderService.selectOrderItemListByOrderId(1L);

// 审批退款
orderService.handleRefund(1L, 1, "admin", null);  // 通过
orderService.handleRefund(2L, 3, "admin", "理由不充分");  // 拒绝
```
