# ruoyi-mall-product 商品模块

## 职责

微信团购商城的商品核心模块，负责商品(Product)、商品分类(ProductCategory)、团购活动(GrouponActivity)的领域建模与 CRUD 管理。

## 包结构

### domain（实体类）

| 类名 | 关键字段 |
|------|---------|
| `Product` | id, merchantId, categoryId, grouponId, name, coverImage, images, price, originalPrice, stock, sales, status, validDays, description, storeIds, sort |
| `ProductCategory` | id, merchantId, name, sort, status |
| `GrouponActivity` | id, merchantId, name, coverImage, description, startTime, endTime, status, totalSold, limitPerUser |

均继承 `BaseEntity`（含 createBy, createTime, updateBy, updateTime, remark, params）。

### mapper

**ProductMapper**

| 方法 | 说明 |
|------|------|
| `selectProductById(Long id)` | 按 ID 查商品 |
| `selectProductList(Product product)` | 条件查询列表 |
| `selectProductByMerchantId(Long merchantId)` | 按商户查商品 |
| `insertProduct(Product product)` | 新增 |
| `updateProduct(Product product)` | 更新 |
| `deleteProductById(Long id)` | 单删 |
| `deleteProductByIds(Long[] ids)` | 批删 |
| `countProductByMerchantId(Long merchantId)` | 统计商户商品数 |
| `selectHotProducts(int limit)` | 热销商品（按销量降序，JOIN merchant）|

**ProductCategoryMapper**

| 方法 | 说明 |
|------|------|
| `selectProductCategoryById(Long id)` | 按 ID 查分类 |
| `selectProductCategoryList(ProductCategory)` | 条件查询列表 |
| `selectProductCategoryByMerchantId(Long merchantId)` | 按商户查分类 |
| `insertProductCategory(ProductCategory)` | 新增 |
| `updateProductCategory(ProductCategory)` | 更新 |
| `deleteProductCategoryById(Long id)` | 单删 |
| `deleteProductCategoryByIds(Long[] ids)` | 批删 |

### service

**IProductService / ProductServiceImpl**（纯委托 Mapper）

| 方法 | 说明 |
|------|------|
| `selectProductById(Long id)` | 按 ID 查询 |
| `selectProductList(Product product)` | 条件列表查询 |
| `insertProduct(Product product)` | 新增 |
| `updateProduct(Product product)` | 更新 |
| `deleteProductById(Long id)` | 单删 |
| `deleteProductByIds(Long[] ids)` | 批删 |

> 注意：ProductCategoryMapper 和 GrouponActivity 目前仅有 Mapper 层，未封装 Service。

### controller

**MallProductController** — `@RequestMapping("/mall/product")`

| 接口 | 路径 | 权限 | 说明 |
|------|------|------|------|
| `GET /list` | `/mall/product/list` | `mall:product:list` | 分页列表查询 |
| `GET /{id}` | `/mall/product/{id}` | `mall:product:query` | 按 ID 查询详情 |
| `POST` | `/mall/product` | `mall:product:add` | 新增商品 |
| `PUT` | `/mall/product` | `mall:product:edit` | 修改商品 |
| `DELETE /{ids}` | `/mall/product/{ids}` | `mall:product:remove` | 批量删除 |

## 被依赖

| 依赖方 | 依赖方式 | 说明 |
|--------|---------|------|
| `ruoyi-admin` | Maven 依赖 + 直接注入 `ProductMapper` | `DashboardServiceImpl` 使用 `ProductMapper` 获取热销商品数据做仪表盘统计 |
| `ruoyi-mall-order` | Maven 依赖（`pom.xml` 中声明）| 目前未直接 import product 包下的类，预留编译依赖 |

## 使用示例

```java
// Controller 层调用示例
@Autowired
private IProductService productService;

// 查询商品列表（带分页）
@GetMapping("/list")
public TableDataInfo list(Product product) {
    startPage();
    List<Product> list = productService.selectProductList(product);
    return getDataTable(list);
}

// 新增商品
@PostMapping
public AjaxResult add(@RequestBody Product product) {
    return toAjax(productService.insertProduct(product));
}

// Dashboard 中直接使用 Mapper（绕过 Service 层）
@Autowired
private ProductMapper productMapper;

List<Map> hotProducts = productMapper.selectHotProducts(10);
```
