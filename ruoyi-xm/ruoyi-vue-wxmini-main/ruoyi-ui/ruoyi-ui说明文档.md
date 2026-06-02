# ruoyi-ui 运营后台 - 项目说明文档

> 基于 RuoYi-Vue v3.8.9 改造的团购商城运营后台
> 技术栈：Vue 2.6.12 + Element UI 2.15.14 + ECharts 5.4.0 + Vuex 3.6.0 + Vue Router 3.4.9

---

## 一、项目位置

```
f:\wx\ruoyi-xm\ruoyi-vue-wxmini-main\ruoyi-ui
```

---

## 二、启动方式

### 2.1 前置条件

- Node.js >= 8.9（当前环境：v25.8.1）
- npm >= 3.0（当前环境：11.11.0）
- 依赖已安装（node_modules已存在）

### 2.2 启动步骤

```bash
# 1. 进入项目目录
cd E:\ruoyi\ruoyi-xm\ruoyi-vue-wxmini-main\ruoyi-ui
# 2. 安装依赖（如果node_modules丢失）
npm install

# 3. 启动开发服务器
npm run dev
```

启动后自动打开浏览器，访问地址：`http://localhost`

### 2.3 其他命令

```bash
# 生产环境打包
npm run build:prod

# 预览打包结果
npm run preview

# ESLint检查
npm run lint
```

### 2.4 端口与代理配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 前端端口 | 80 | vue.config.js中 `port: 80` |
| 后端地址 | http://localhost:8080 | vue.config.js中 `baseUrl` |
| API前缀 | /dev-api | .env.development中 `VUE_APP_BASE_API` |
| 代理规则 | /dev-api → http://localhost:8080 | 开发环境自动代理 |

> **注意**：前端启动不需要后端先运行。当前使用 Mock 数据，不连后端也能正常访问所有页面。
> 后端运行后，修改API文件从 `index.js`(mock) 切换到 `real.js` 即可对接真实接口。

---

## 三、项目目录结构

```
ruoyi-ui/
├── public/                    # 静态资源（index.html、favicon）
├── src/
│   ├── api/                   # 接口定义层
│   │   ├── login.js           # 登录/注册/验证码（真实axios）
│   │   ├── menu.js            # 菜单接口（真实axios）
│   │   ├── system/            # 系统管理接口（真实axios）
│   │   │   ├── user.js        # 用户CRUD
│   │   │   ├── role.js        # 角色CRUD
│   │   │   ├── menu.js        # 菜单CRUD
│   │   │   ├── config.js      # 参数配置
│   │   │   ├── dept.js        # 部门管理
│   │   │   ├── post.js        # 岗位管理
│   │   │   ├── notice.js      # 通知公告
│   │   │   └── log.js         # 日志接口（mock）
│   │   ├── monitor/           # 监控接口（真实axios）
│   │   │   ├── cache.js       # 缓存监控
│   │   │   ├── server.js      # 服务器监控
│   │   │   ├── job.js         # 定时任务
│   │   │   ├── jobLog.js      # 任务日志
│   │   │   ├── logininfor.js  # 登录日志
│   │   │   ├── operlog.js     # 操作日志
│   │   │   └── online.js      # 在线用户
│   │   ├── tool/gen.js        # 代码生成（真实axios）
│   │   ├── merchant/          # 商家管理（mock）
│   │   │   ├── index.js       # mock模式接口
│   │   │   └── real.js        # 真实后端接口
│   │   ├── order/             # 订单管理（mock）
│   │   │   ├── index.js
│   │   │   └── real.js
│   │   ├── finance/           # 财务管理（mock）
│   │   │   ├── index.js
│   │   │   └── real.js
│   │   ├── data/              # 数据中心（mock）
│   │   │   ├── index.js
│   │   │   └── real.js
│   │   └── analysis/          # 数据分析（mock）
│   │       └── index.js
│   ├── assets/                # 静态资源
│   │   ├── icons/             # SVG图标
│   │   ├── images/            # 图片
│   │   └── styles/            # 全局样式
│   ├── components/            # 公共组件
│   │   ├── Breadcrumb/        # 面包屑
│   │   ├── Crontab/           # Cron表达式构建器
│   │   ├── DictTag/           # 字典标签
│   │   ├── Editor/            # 富文本编辑器
│   │   ├── FileUpload/        # 文件上传
│   │   ├── Hamburger/         # 侧边栏折叠按钮
│   │   ├── ImagePreview/      # 图片预览
│   │   ├── ImageUpload/       # 图片上传
│   │   ├── Pagination/        # 分页组件
│   │   ├── RightToolbar/      # 表格工具栏
│   │   ├── Screenfull/        # 全屏切换
│   │   ├── SvgIcon/           # SVG图标组件
│   │   └── ...                # 其他通用组件
│   ├── directive/             # 自定义指令
│   │   ├── permission/        # v-hasPermi, v-hasRole 权限指令
│   │   ├── dialog/            # v-dialogDrag 弹窗拖拽
│   │   └── module/            # v-clipboard 剪贴板
│   ├── layout/                # 布局框架
│   │   ├── index.vue          # 主布局（侧边栏+顶栏+内容区）
│   │   └── components/        # 布局子组件（Navbar, Sidebar, TagsView等）
│   ├── mock/                  # Mock数据层
│   │   ├── index.js           # mock工具函数
│   │   └── data.js            # 所有模块测试数据
│   ├── plugins/               # 插件
│   │   ├── auth.js            # 权限检查
│   │   ├── modal.js           # 弹窗封装
│   │   ├── tab.js             # 标签页操作
│   │   ├── cache.js           # 缓存
│   │   └── download.js        # 下载
│   ├── router/                # 路由配置
│   │   └── index.js           # 常量路由 + 动态路由
│   ├── store/                 # Vuex状态管理
│   │   ├── index.js           # 根Store
│   │   ├── getters.js         # 全局Getters
│   │   └── modules/           # 模块（app/dict/permission/settings/tagsView/user）
│   ├── utils/                 # 工具函数
│   │   ├── request.js         # Axios封装（拦截器/token/错误处理）
│   │   ├── auth.js            # Token管理
│   │   ├── validate.js        # 校验工具
│   │   ├── ruoyi.js           # 若依通用工具
│   │   ├── jsencrypt.js       # RSA加密
│   │   └── permission.js      # 权限检查
│   ├── views/                 # 页面组件
│   │   ├── index.vue          # 首页（工作台/数据大屏）
│   │   ├── login.vue          # 登录页
│   │   ├── register.vue       # 注册页
│   │   ├── redirect.vue       # 重定向
│   │   ├── error/             # 错误页（401/404）
│   │   ├── merchant/          # 【商家管理】9个页面
│   │   ├── order/             # 【订单管理】6个页面
│   │   ├── finance/           # 【财务管理】5个页面
│   │   ├── data-analysis/     # 【数据分析】3个页面
│   │   ├── goods/             # 【商品管理】4个页面（未接入路由）
│   │   ├── marketing/         # 【营销活动】4个页面（未接入路由）
│   │   ├── data-overview/     # 【数据中心】5个页面（未接入路由）
│   │   ├── ops-log/           # 【日志管理】3个页面
│   │   ├── system/            # 【系统管理】若依原有页面
│   │   ├── monitor/           # 【系统监控】若依原有页面
│   │   ├── tool/              # 【系统工具】若依原有页面
│   │   └── dashboard/         # Dashboard子组件（图表等）
│   ├── App.vue                # 根组件
│   ├── main.js                # 入口文件
│   ├── permission.js          # 路由守卫
│   └── settings.js            # 全局设置
├── sql/                       # 建表SQL（业务表）
├── bin/                       # 构建脚本
├── build/                     # 打包配置
├── package.json               # 依赖配置
├── vue.config.js              # Webpack配置
├── .env.development           # 开发环境变量
├── .env.production            # 生产环境变量
├── babel.config.js            # Babel配置
├── .eslintrc.js               # ESLint配置
└── 功能需求文档.md             # 功能需求说明
```

---

## 四、菜单结构（侧边栏）

当前路由配置（`src/router/index.js`）定义了 5 个一级菜单：

```
├── 工作台 (首页)
├── 商家管理
│   ├── 商家列表
│   │   ├── 添加商户 (隐藏)
│   │   └── 商家详情/:id (隐藏)
│   ├── 商家提现
│   └── 入驻审核
├── 平台财务
│   ├── 全部订单
│   │   └── 订单详情/:id (隐藏)
│   ├── 售后订单
│   ├── 异常订单
│   ├── 平台流水
│   ├── 商家分账
│   ├── 提现记录
│   ├── 财务报表
│   └── 收益统计
├── 数据分析
│   ├── 商家销售排行
│   ├── 销售统计
│   └── 订单统计
└── 系统管理
    ├── 角色权限
    ├── 菜单管理
    ├── 参数配置
    ├── 登录日志
    └── 操作日志
```

---

## 五、页面清单

### 5.1 已接入路由的页面（38个）

| 模块 | 页面 | 文件路径 | 数据来源 |
|------|------|----------|----------|
| 首页 | 工作台 | `views/index.vue` | 硬编码 |
| 商家管理 | 商家列表 | `views/merchant/list.vue` | Mock |
| | 添加商户 | `views/merchant/add.vue` | Mock |
| | 商家详情 | `views/merchant/detail.vue` | Mock |
| | 商家提现 | `views/merchant/withdraw.vue` | Mock |
| | 入驻审核 | `views/merchant/audit.vue` | Mock |
| 平台财务 | 全部订单 | `views/order/all.vue` | Mock |
| | 订单详情 | `views/order/detail.vue` | Mock |
| | 售后订单 | `views/order/after-sale.vue` | Mock |
| | 异常订单 | `views/order/abnormal.vue` | Mock |
| | 平台流水 | `views/finance/platform-flow.vue` | Mock |
| | 商家分账 | `views/finance/profit-sharing.vue` | Mock |
| | 提现记录 | `views/finance/withdraw-record.vue` | Mock |
| | 财务报表 | `views/finance/report.vue` | Mock |
| | 收益统计 | `views/finance/income.vue` | Mock |
| 数据分析 | 商家销售排行 | `views/data-analysis/rank.vue` | Mock |
| | 销售统计 | `views/data-analysis/sales.vue` | Mock |
| | 订单统计 | `views/data-analysis/order-stats.vue` | Mock |
| 系统管理 | 角色权限 | `views/system/role/index.vue` | 真实API |
| | 菜单管理 | `views/system/menu/index.vue` | 真实API |
| | 参数配置 | `views/system/config/index.vue` | 真实API |
| | 登录日志 | `views/ops-log/login.vue` | Mock |
| | 操作日志 | `views/ops-log/operation.vue` | Mock |
| 系统管理(隐藏) | 分配用户 | `views/system/role/authUser.vue` | 真实API |
| | 字典数据 | `views/system/dict/data.vue` | 真实API |
| 系统监控 | 调度日志 | `views/monitor/job/log.vue` | 真实API |
| | (若依原有监控页面) | `views/monitor/` | 真实API |
| 系统工具 | 代码生成 | `views/tool/gen/` | 真实API |
| | 表单构建 | `views/tool/build/` | - |

### 5.2 已创建但未接入路由的页面（11个）

| 模块 | 页面 | 文件路径 | 说明 |
|------|------|----------|------|
| 商品管理 | 商品列表 | `views/goods/list.vue` | 未在router中注册 |
| | 商品分类 | `views/goods/category.vue` | 未在router中注册 |
| | 商品审核 | `views/goods/audit.vue` | 未在router中注册 |
| | 库存管理 | `views/goods/stock.vue` | 未在router中注册 |
| 营销活动 | 活动管理 | `views/marketing/activity.vue` | 未在router中注册 |
| | 轮播图管理 | `views/marketing/banner.vue` | 未在router中注册 |
| | 优惠券管理 | `views/marketing/coupon.vue` | 未在router中注册 |
| | 秒杀管理 | `views/marketing/seckill.vue` | 未在router中注册 |
| 数据中心 | 平台概览 | `views/data-overview/overview.vue` | 未在router中注册 |
| | 今日交易额 | `views/data-overview/today-flow.vue` | 未在router中注册 |
| | 平台总流水 | `views/data-overview/order-trend.vue` | 未在router中注册 |
| | 今日订单数 | `views/data-overview/today-orders.vue` | 未在router中注册(文件存在但未确认) |
| | 商家统计 | `views/data-overview/merchant-stats.vue` | 未在router中注册 |
| | 用户增长 | `views/data-overview/user-growth.vue` | 未在router中注册 |
| 其他 | 错误日志 | `views/ops-log/error.vue` | 未在router中注册 |
| | Dashboard子组件 | `views/dashboard/*.vue` | 组件，非页面 |
| | 首页v1备份 | `views/index_v1.vue` | 备份文件 |

---

## 六、Mock数据 vs 真实API

### 6.1 使用Mock数据的模块

这些模块的 `src/api/` 下的 `index.js` 文件引用 `mock/data.js`，返回模拟数据：

| 模块 | API文件 | Mock数据文件 |
|------|---------|-------------|
| 商家管理 | `api/merchant/index.js` | `mock/data.js` |
| 订单管理 | `api/order/index.js` | `mock/data.js` |
| 财务管理 | `api/finance/index.js` | `mock/data.js` |
| 数据中心 | `api/data/index.js` | `mock/data.js` |
| 数据分析 | `api/analysis/index.js` | `mock/data.js` |
| 系统日志 | `api/system/log.js` | `mock/data.js` |

### 6.2 使用真实API的模块

这些模块直接发axios请求到后端：

| 模块 | API文件 | 对应后端 |
|------|---------|----------|
| 登录认证 | `api/login.js` | `/login`, `/getInfo`等 |
| 菜单 | `api/menu.js` | `/getRouters` |
| 用户管理 | `api/system/user.js` | `/system/user/*` |
| 角色管理 | `api/system/role.js` | `/system/role/*` |
| 菜单管理 | `api/system/menu.js` | `/system/menu/*` |
| 部门管理 | `api/system/dept.js` | `/system/dept/*` |
| 字典管理 | `api/system/dict/*.js` | `/system/dict/*` |
| 配置管理 | `api/system/config.js` | `/system/config/*` |
| 岗位管理 | `api/system/post.js` | `/system/post/*` |
| 通知管理 | `api/system/notice.js` | `/system/notice/*` |
| 监控管理 | `api/monitor/*.js` | `/monitor/*` |
| 代码生成 | `api/tool/gen.js` | `/tool/gen/*` |

### 6.3 切换到真实后端

业务模块已预留 `real.js` 文件，切换方式：
1. 将页面中的 `import { xxx } from '@/api/merchant/index'` 改为 `import { xxx } from '@/api/merchant/real'`
2. 确保后端服务在 `http://localhost:8080` 运行

---

## 七、登录说明

- **登录地址**：`http://localhost/login`
- **默认账号**：`admin` / `123456`
- **测试账号**：`ry` / `123456`
- **验证码**：数学计算题，答案填数字
- **Token存储**：Cookie，key为 `Admin-Token`

---

## 八、全局配置

### 8.1 settings.js

```js
sideTheme: 'theme-dark'    // 侧边栏深色主题
showSettings: false         // 隐藏右侧面板
topNav: false               // 关闭顶部导航
tagsView: true              // 开启标签页
fixedHeader: false          // 头部不固定
sidebarLogo: true           // 显示Logo
dynamicTitle: false         // 不用动态标题
```

### 8.2 环境变量（.env.development）

```
VUE_APP_TITLE = 若依管理系统
VUE_APP_BASE_API = /dev-api
```

---

## 九、构建部署

```bash
# 生产环境打包
npm run build:prod

# 输出目录：dist/
# 将 dist/ 目录内容部署到 Nginx 或其他Web服务器
```

Nginx配置参考：
```nginx
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html/dist;
        index  index.html;
        try_files $uri $uri/ /index.html;  # Vue history模式
    }

    location /prod-api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_pass http://localhost:8080/;
    }
}
```

---

## 十、常见问题

| 问题 | 解决方案 |
|------|----------|
| npm install 报错 | 尝试删除 node_modules 和 package-lock.json 后重新 install |
| 端口80被占用 | 修改 vue.config.js 中 `port` 为其他值（如8081） |
| 页面空白 | 检查浏览器控制台报错，通常是路由或组件import路径问题 |
| 登录后401 | 检查后端是否运行，或Cookie中token是否过期 |
| Mock数据不更新 | Mock有300-600ms随机延迟，数据在 mock/data.js 中修改 |
