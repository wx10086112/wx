# ruoyi-mall-merchant 模块文档

## 职责

商户管理模块，负责商户信息的 CRUD、商户审核、商户店铺及商户用户的管理。提供后台管理接口，供管理系统前端调用。

---

## 包结构

### domain

| 类名 | 说明 | 关键字段 |
|------|------|----------|
| `Merchant` | 商户主体 | `id`, `name`, `logo`, `contact`, `phone`, `commissionRate`(佣金率), `status`(状态), `balance`(余额), `totalIncome`(总收入), `productCount`, `storeCount` |
| `MerchantStore` | 商户门店 | `id`, `merchantId`, `name`, `contact`, `phone`, `address`, `longitude`, `latitude`, `businessHours`, `avatar`, `status`, `isMain`(是否主店) |
| `MerchantUser` | 商户用户 | `id`, `merchantId`, `username`, `password`, `realName`, `phone`, `role`, `status`, `lastLoginTime` |

### mapper

| 接口名 | 关键方法 |
|--------|----------|
| `MerchantMapper` | `selectMerchantById`, `selectMerchantList`, `insertMerchant`, `updateMerchant`, `deleteMerchantById(s)`, `countActiveMerchant`(统计活跃商户数), `selectMerchantRankByIncome`(按收入排行) |
| `MerchantStoreMapper` | `selectMerchantStoreById`, `selectMerchantStoreList`, `selectMerchantStoreByMerchantId`, `insert/update/delete` |
| `MerchantUserMapper` | `selectMerchantUserById`, `selectMerchantUserByUsername`, `selectMerchantUserList`, `selectMerchantUserByMerchantId`, `insert/update/delete` |

### service

| 接口 / 实现 | 说明 |
|-------------|------|
| `IMerchantService` | 商户服务接口，提供标准 CRUD 方法 |
| `MerchantServiceImpl` | 实现类，注入 `MerchantMapper`，直接委托 Mapper 调用 |

### controller

| 类名 | 基路径 | 权限前缀 |
|------|--------|----------|
| `MallMerchantController` | `/mall/merchant` | `mall:merchant:*` |
| `MallMerchantAuditController` | `/mall/merchant` | `mall:merchant:audit` |

---

## 接口列表

### MallMerchantController (`/mall/merchant`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| `GET` | `/list` | 分页查询商户列表 | `mall:merchant:list` |
| `GET` | `/{id}` | 根据ID查询商户详情 | `mall:merchant:query` |
| `POST` | `/` | 新增商户 | `mall:merchant:add` |
| `PUT` | `/` | 修改商户信息 | `mall:merchant:edit` |
| `DELETE` | `/{ids}` | 批量删除商户 | `mall:merchant:remove` |

### MallMerchantAuditController (`/mall/merchant`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| `GET` | `/audit/list` | 分页查询待审核商户列表 | `mall:merchant:audit` |
| `PUT` | `/audit/{id}/{status}` | 审核商户（通过/拒绝） | `mall:merchant:audit` |

---

## 被依赖

- **ruoyi-admin**: 主启动模块，扫描并加载本模块的 Controller
- **ruoyi-common**: 依赖 `BaseEntity`, `BaseController`, `AjaxResult`, `TableDataInfo`, `@Log`, `@PreAuthorize` 等公共组件
- 其他 mall 模块（如 `ruoyi-mall-product`、`ruoyi-mall-order`）可通过依赖本模块的 `IMerchantService` 或 `MerchantMapper` 查询商户信息

---

## 使用示例

```java
// 注入商户服务
@Autowired
private IMerchantService merchantService;

// 查询商户详情
Merchant merchant = merchantService.selectMerchantById(1L);

// 分页查询（配合 Controller 的 startPage 自动分页）
Merchant query = new Merchant();
query.setName("示例商户");
List<Merchant> list = merchantService.selectMerchantList(query);

// 新增商户
Merchant newMerchant = new Merchant();
newMerchant.setName("新商户");
newMerchant.setContact("张三");
newMerchant.setPhone("13800138000");
newMerchant.setCommissionRate(new BigDecimal("0.05"));
merchantService.insertMerchant(newMerchant);

// 审核接口调用示例
// PUT /mall/merchant/audit/1/1  → 将商户ID=1 审核通过(status=1)
// PUT /mall/merchant/audit/1/2  → 将商户ID=1 审核拒绝(status=2)
```
