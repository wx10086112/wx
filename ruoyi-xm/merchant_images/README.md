# 商家图片存储目录

## 目录结构

按 **商家ID → 图片类型** 二级分类：

```
merchant_images/
├── 1/                      # 商家ID=1
│   ├── avatar/             # 商家头像
│   │   └── avatar.jpg
│   ├── store/              # 门店照片
│   │   ├── store_01.jpg
│   │   └── store_02.jpg
│   ├── banner/             # 轮播图
│   │   ├── banner_01.jpg
│   │   └── banner_02.jpg
│   └── product/            # 商品图片
│       ├── 101/            # 商品ID=101
│       │   ├── main.jpg    # 主图
│       │   └── detail_01.jpg
│       └── 102/            # 商品ID=102
├── 2/                      # 商家ID=2
│   ├── avatar/
│   ├── store/
│   └── product/
└── ...
```

## 图片类型说明

| 目录 | 用途 | 说明 |
|------|------|------|
| `avatar/` | 商家头像 | 1张，正方形 |
| `store/` | 门店照片 | 多张，展示门店环境 |
| `banner/` | 轮播图 | 多张，首页展示 |
| `product/` | 商品图片 | 按商品ID再分子目录 |

## 使用说明

- 新商家入驻时自动创建 `{merchant_id}/` 及其子目录
- 商品上架时自动创建 `{merchant_id}/product/{product_id}/`
- 后端上传接口自动按此结构存储
