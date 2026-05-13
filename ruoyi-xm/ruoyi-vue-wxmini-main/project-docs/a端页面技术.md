# 零点科技团购商城 - 前端模块技术文档（ruoyi-ui）

> 版本：v1.0
> 日期：2026-05-10
> 技术栈：Vue 2.6.12 + Element UI 2.15.14 + Vuex 3.6.0 + Vue Router 3.4.9 + ECharts 5.4.0 + Axios 0.28.1

---

## 一、项目配置

### 1.1 settings.js 全局配置

```js
sideTheme: 'theme-dark'   // 侧边栏暗色主题
showSettings: false        // 隐藏设置面板
topNav: false              // 顶部导航关闭
tagsView: true             // 标签页开启
fixedHeader: false         // 头部不固定
sidebarLogo: true          // 显示Logo
dynamicTitle: false        // 不使用动态标题
errorLog: 'production'     // 仅生产环境记录错误日志
```

### 1.2 main.js 全局注册

**Vue.prototype 原型方法:**
- `getDicts` — 获取字典数据
- `getConfigKey` — 获取配置值
- `parseTime` — 时间格式化
- `resetForm` — 重置表单
- `addDateRange` — 添加日期范围参数
- `selectDictLabel` — 字典值转标签
- `selectDictLabels` — 多字典值转标签
- `download` — 文件下载
- `handleTree` — 构建树形结构

**全局组件:**
- `DictTag`, `Pagination`, `RightToolbar`, `Editor`, `FileUpload`, `ImageUpload`, `ImagePreview`

**Element UI:** size默认 `medium`，由Cookie控制

### 1.3 核心依赖

| 包名 | 版本 | 用途 |
|------|------|------|
| vue | 2.6.12 | 核心框架 |
| element-ui | 2.15.14 | UI组件库 |
| echarts | 5.4.0 | 图表 |
| axios | 0.28.1 | HTTP请求 |
| vuex | 3.6.0 | 状态管理 |
| vue-router | 3.4.9 | 路由 |
| vue-count-to | 1.0.13 | 数字动画 |
| jsencrypt | 3.0.0-rc.1 | RSA加密 |
| js-cookie | 3.0.1 | Cookie管理 |
| nprogress | 0.2.0 | 进度条 |
| vue-cropper | 0.5.5 | 图片裁剪 |
| clipboard | 2.0.8 | 复制到剪贴板 |
| screenfull | 5.0.2 | 全屏切换 |

---

## 二、路由系统（src/router/index.js）

### 2.1 constantRoutes 常量路由（始终加载）

| 路径 | 组件 | 隐藏 | 说明 |
|------|------|------|------|
| `/redirect` | Layout | 是 | 重定向父路由 |
| `/redirect/:path(.*)` | `@/views/redirect` | 是 | 重定向子路由 |
| `/login` | `@/views/login` | 是 | 登录页 |
| `/register` | `@/views/register` | 是 | 注册页 |
| `/404` | `@/views/error/404` | 是 | 404页 |
| `/401` | `@/views/error/401` | 是 | 401页 |
| `/` → redirect `index` | Layout | 否 | 根路径重定向 |
| `index` | `@/views/index` | 否 | 首页(数据大屏) |
| `/user/profile` | `@/views/system/user/profile/index` | 是 | 个人中心 |

### 2.2 业务菜单路由（6大模块）

#### 数据中心 `/data-overview`（icon: chart）

| 子路径 | 组件 | 路由名 | Meta |
|--------|------|--------|------|
| `overview` | `data-overview/overview` | PlatformOverview | `{ title: '平台概览' }` |
| `today-flow` | `data-overview/today-flow` | TodayFlow | `{ title: '今日交易额' }` |
| `order-trend` | `data-overview/order-trend` | PlatformTotalFlow | `{ title: '平台总流水' }` |
| `today-orders` | `data-overview/today-orders` | TodayOrders | `{ title: '今日订单数' }` |
| `merchant-stats` | `data-overview/merchant-stats` | MerchantStats | `{ title: '商家统计' }` |
| `user-growth` | `data-overview/user-growth` | UserGrowth | `{ title: '用户增长' }` |

#### 商家中心 `/merchant`（icon: peoples）

| 子路径 | 组件 | 路由名 | Meta | 隐藏 |
|--------|------|--------|------|------|
| `list` | `merchant/list` | MerchantList | `{ title: '商家列表' }` | 否 |
| `detail/:id` | `merchant/detail` | MerchantDetail | `{ title: '商家详情', activeMenu: '/merchant/list' }` | 是 |
| `level` | `merchant/level` | MerchantLevel | `{ title: '商家等级' }` | 否 |
| `flow` | `merchant/flow` | MerchantFlow | `{ title: '商家流水' }` | 否 |
| `withdraw` | `merchant/withdraw` | MerchantWithdraw | `{ title: '提现管理' }` | 否 |

#### 订单中心 `/order`（icon: money）

| 子路径 | 组件 | 路由名 | Meta | 隐藏 |
|--------|------|--------|------|------|
| `all` | `order/all` | OrderAll | `{ title: '全部订单' }` | 否 |
| `detail/:id` | `order/detail` | OrderDetail | `{ title: '订单详情', activeMenu: '/order/all' }` | 是 |
| `after-sale` | `order/after-sale` | OrderAfterSale | `{ title: '售后订单' }` | 否 |
| `abnormal` | `order/abnormal` | OrderAbnormal | `{ title: '异常订单' }` | 否 |

#### 财务中心 `/finance`（icon: money）

| 子路径 | 组件 | 路由名 | Meta |
|--------|------|--------|------|
| `platform-flow` | `finance/platform-flow` | PlatformIncome | `{ title: '平台收益' }` |
| `profit-sharing` | `finance/profit-sharing` | ProfitSharing | `{ title: '商家分账' }` |
| `withdraw-record` | `finance/withdraw-record` | WithdrawRecord | `{ title: '提现记录' }` |
| `report` | `finance/report` | FinanceReport | `{ title: '财务报表' }` |
| `income` | `finance/income` | IncomeStats | `{ title: '收益统计' }` |

#### 数据分析 `/data-analysis`（icon: chart）

| 子路径 | 组件 | 路由名 | Meta |
|--------|------|--------|------|
| `rank` | `data-analysis/rank` | SalesRank | `{ title: '商家销售排行' }` |
| `sales` | `data-analysis/sales` | SalesStats | `{ title: '销售统计' }` |
| `order-stats` | `data-analysis/order-stats` | OrderStats | `{ title: '订单统计' }` |

#### 系统管理 `/system`（icon: system）

| 子路径 | 组件 | 路由名 | Meta |
|--------|------|--------|------|
| `user` | `system/user/index` | AdminManage | `{ title: '管理员管理' }` |
| `role` | `system/role/index` | RolePermission | `{ title: '角色权限' }` |
| `menu` | `system/menu/index` | MenuConfig | `{ title: '菜单管理' }` |
| `config` | `system/config/index` | ParamConfig | `{ title: '参数配置' }` |
| `login-log` | `ops-log/login` | LoginLog | `{ title: '登录日志' }` |
| `operation-log` | `ops-log/operation` | OperationLog | `{ title: '操作日志' }` |

### 2.3 dynamicRoutes 动态路由（权限控制，隐藏）

| 路径 | 子路径 | 组件 | 权限 | Meta |
|------|--------|------|------|------|
| `/system/user-auth` | `role/:userId(\\d+)` | `system/user/authRole` | `system:user:edit` | `{ title: '分配角色' }` |
| `/system/role-auth` | `user/:roleId(\\d+)` | `system/role/authUser` | `system:role:edit` | `{ title: '分配用户' }` |
| `/system/dict-data` | `index/:dictId(\\d+)` | `system/dict/data` | `system:dict:list` | `{ title: '字典数据' }` |
| `/monitor/job-log` | `index/:jobId(\\d+)` | `monitor/job/log` | `monitor:job:list` | `{ title: '调度日志' }` |
| `/tool/gen-edit` | `index/:tableId(\\d+)` | `tool/gen/editTable` | `tool:gen:edit` | `{ title: '修改生成配置' }` |

---

## 三、Vuex状态管理（src/store/）

### 3.1 模块结构

```
store/
├── index.js          ← 根Store（6个命名空间模块）
├── getters.js        ← 全局Getters
└── modules/
    ├── app.js        ← 应用状态（侧边栏/设备/尺寸）
    ├── dict.js       ← 字典缓存
    ├── permission.js ← 权限路由（已改为本地静态）
    ├── settings.js   ← 主题/布局设置
    ├── tagsView.js   ← 标签页管理
    └── user.js       ← 用户信息/Token/权限
```

### 3.2 getters.js 全局Getters

| Getter | 来源 |
|--------|------|
| `sidebar` | `state.app.sidebar` |
| `size` | `state.app.size` |
| `device` | `state.app.device` |
| `dict` | `state.dict.dict` |
| `visitedViews` | `state.tagsView.visitedViews` |
| `cachedViews` | `state.tagsView.cachedViews` |
| `token` | `state.user.token` |
| `avatar` | `state.user.avatar` |
| `name` | `state.user.name` |
| `introduction` | `state.user.introduction` |
| `roles` | `state.user.roles` |
| `permissions` | `state.user.permissions` |
| `permission_routes` | `state.permission.routes` |
| `topbarRouters` | `state.permission.topbarRouters` |
| `defaultRoutes` | `state.permission.defaultRoutes` |
| `sidebarRouters` | `state.permission.sidebarRouters` |

### 3.3 app 模块（应用状态）

**State:** `sidebar: { opened, withoutAnimation, hide }`, `device: 'desktop'`, `size: 'medium'`

| Mutation | 参数 | 说明 |
|----------|------|------|
| `TOGGLE_SIDEBAR` | - | 切换侧边栏 |
| `CLOSE_SIDEBAR` | withoutAnimation | 关闭侧边栏 |
| `TOGGLE_DEVICE` | device | 切换设备模式 |
| `SET_SIZE` | size | 设置组件尺寸 |
| `SET_SIDEBAR_HIDE` | status | 隐藏侧边栏 |

| Action | 说明 |
|--------|------|
| `toggleSideBar` | 切换侧边栏 |
| `closeSideBar` | 关闭侧边栏 |
| `toggleDevice` | 切换设备 |
| `setSize` | 设置尺寸 |
| `toggleSideBarHide` | 隐藏侧边栏 |

### 3.4 dict 模块（字典缓存）

**State:** `dict: []`

| Mutation | 说明 |
|----------|------|
| `SET_DICT({key, value})` | 设置字典缓存 |
| `REMOVE_DICT(key)` | 移除指定字典 |
| `CLEAN_DICT` | 清空所有字典 |

### 3.5 settings 模块（主题设置）

**State:** `title`, `theme`, `sideTheme`, `showSettings`, `topNav`, `tagsView`, `fixedHeader`, `sidebarLogo`, `dynamicTitle`

| Mutation / Action | 说明 |
|-------------------|------|
| `CHANGE_SETTING({key, value})` | 修改设置 |
| `changeSetting(data)` | 批量修改 |
| `setTitle(title)` | 设置标题 |

### 3.6 tagsView 模块（标签页）

**State:** `visitedViews: []`, `cachedViews: []`, `iframeViews: []`

| Action | 说明 |
|--------|------|
| `addView` | 添加标签页 |
| `delView` | 删除标签页 |
| `delOthersViews` | 删除其他标签页 |
| `delAllViews` | 删除所有标签页 |
| `updateVisitedView` | 更新标签页 |
| `delRightTags` | 关闭右侧 |
| `delLeftTags` | 关闭左侧 |

### 3.7 user 模块（用户信息）

**State:** `token`, `id`, `name`, `avatar`, `roles: []`, `permissions: []`

| Action | 调用API | 说明 |
|--------|---------|------|
| `Login` | `login()` | 登录获取token |
| `GetInfo` | `getInfo()` | 获取用户信息/角色/权限 |
| `LogOut` | `logout()` | 退出登录 |
| `FedLogOut` | - | 强制退出 |

### 3.8 permission 模块（权限路由 ★已改造）

**State:** `routes: []`, `addRoutes: []`, `defaultRoutes: []`, `topbarRouters: []`, `sidebarRouters: []`

**GenerateRoutes Action（已改为本地静态路由）:**
```js
GenerateRoutes({ commit }) {
  return new Promise(resolve => {
    // 使用本地静态路由（不请求后端菜单接口）
    const sidebarRoutes = constantRoutes.filter(r => !r.hidden)
    const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
    const rewriteRoutes = [{ path: '*', redirect: '/404', hidden: true }]
    router.addRoutes(asyncRoutes)
    commit('SET_ROUTES', rewriteRoutes)
    commit('SET_SIDEBAR_ROUTERS', constantRoutes)
    commit('SET_DEFAULT_ROUTES', sidebarRoutes)
    commit('SET_TOPBAR_ROUTES', sidebarRoutes)
    resolve(rewriteRoutes)
  })
}
```

**导出工具函数:** `filterDynamicRoutes(routes)`, `loadView(view)`

---

## 四、API接口层（src/api/）

### 4.1 原有若依API（真实axios请求）

#### 登录认证 `src/api/login.js`
| 函数 | 方法 | URL | 说明 |
|------|------|-----|------|
| `login(username, password, code, uuid)` | POST | `/login` | 登录 |
| `register(data)` | POST | `/register` | 注册 |
| `getInfo()` | GET | `/getInfo` | 获取用户信息 |
| `logout()` | POST | `/logout` | 退出登录 |
| `getCodeImg()` | GET | `/captchaImage` | 获取验证码 |

#### 菜单 `src/api/menu.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `getRouters()` | GET | `/getRouters` |

#### 系统管理—用户 `src/api/system/user.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listUser(query)` | GET | `/system/user/list` |
| `getUser(userId)` | GET | `/system/user/{userId}` |
| `addUser(data)` | POST | `/system/user` |
| `updateUser(data)` | PUT | `/system/user` |
| `delUser(userId)` | DELETE | `/system/user/{userId}` |
| `resetUserPwd(userId, password)` | PUT | `/system/user/resetPwd` |
| `changeUserStatus(userId, status)` | PUT | `/system/user/changeStatus` |
| `getUserProfile()` | GET | `/system/user/profile` |
| `updateUserProfile(data)` | PUT | `/system/user/profile` |
| `updateUserPwd(oldPassword, newPassword)` | PUT | `/system/user/profile/updatePwd` |
| `uploadAvatar(data)` | POST | `/system/user/profile/avatar` |
| `getAuthRole(userId)` | GET | `/system/user/authRole/{userId}` |
| `updateAuthRole(data)` | PUT | `/system/user/authRole` |
| `deptTreeSelect()` | GET | `/system/user/deptTree` |

#### 系统管理—角色 `src/api/system/role.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listRole(query)` | GET | `/system/role/list` |
| `getRole(roleId)` | GET | `/system/role/{roleId}` |
| `addRole(data)` | POST | `/system/role` |
| `updateRole(data)` | PUT | `/system/role` |
| `dataScope(data)` | PUT | `/system/role/dataScope` |
| `changeRoleStatus(roleId, status)` | PUT | `/system/role/changeStatus` |
| `delRole(roleId)` | DELETE | `/system/role/{roleId}` |
| `allocatedUserList(query)` | GET | `/system/role/authUser/allocatedList` |
| `unallocatedUserList(query)` | GET | `/system/role/authUser/unallocatedList` |
| `authUserCancel(data)` | PUT | `/system/role/authUser/cancel` |
| `authUserCancelAll(data)` | PUT | `/system/role/authUser/cancelAll` |
| `authUserSelectAll(data)` | PUT | `/system/role/authUser/selectAll` |
| `deptTreeSelect(roleId)` | GET | `/system/role/deptTree/{roleId}` |

#### 系统管理—菜单 `src/api/system/menu.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listMenu(query)` | GET | `/system/menu/list` |
| `getMenu(menuId)` | GET | `/system/menu/{menuId}` |
| `treeselect()` | GET | `/system/menu/treeselect` |
| `roleMenuTreeselect(roleId)` | GET | `/system/menu/roleMenuTreeselect/{roleId}` |
| `addMenu(data)` | POST | `/system/menu` |
| `updateMenu(data)` | PUT | `/system/menu` |
| `delMenu(menuId)` | DELETE | `/system/menu/{menuId}` |

#### 系统管理—部门 `src/api/system/dept.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listDept(query)` | GET | `/system/dept/list` |
| `listDeptExcludeChild(deptId)` | GET | `/system/dept/list/exclude/{deptId}` |
| `getDept(deptId)` | GET | `/system/dept/{deptId}` |
| `addDept(data)` | POST | `/system/dept` |
| `updateDept(data)` | PUT | `/system/dept` |
| `delDept(deptId)` | DELETE | `/system/dept/{deptId}` |

#### 系统管理—字典数据 `src/api/system/dict/data.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listData(query)` | GET | `/system/dict/data/list` |
| `getData(dictCode)` | GET | `/system/dict/data/{dictCode}` |
| `getDicts(dictType)` | GET | `/system/dict/data/type/{dictType}` |
| `addData(data)` | POST | `/system/dict/data` |
| `updateData(data)` | PUT | `/system/dict/data` |
| `delData(dictCode)` | DELETE | `/system/dict/data/{dictCode}` |

#### 系统管理—字典类型 `src/api/system/dict/type.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listType(query)` | GET | `/system/dict/type/list` |
| `getType(dictId)` | GET | `/system/dict/type/{dictId}` |
| `addType(data)` | POST | `/system/dict/type` |
| `updateType(data)` | PUT | `/system/dict/type` |
| `delType(dictId)` | DELETE | `/system/dict/type/{dictId}` |
| `refreshCache()` | DELETE | `/system/dict/type/refreshCache` |
| `optionselect()` | GET | `/system/dict/type/optionselect` |

#### 系统管理—参数配置 `src/api/system/config.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listConfig(query)` | GET | `/system/config/list` |
| `getConfig(configId)` | GET | `/system/config/{configId}` |
| `getConfigKey(configKey)` | GET | `/system/config/configKey/{configKey}` |
| `addConfig(data)` | POST | `/system/config` |
| `updateConfig(data)` | PUT | `/system/config` |
| `delConfig(configId)` | DELETE | `/system/config/{configId}` |
| `refreshCache()` | DELETE | `/system/config/refreshCache` |

#### 系统管理—岗位 `src/api/system/post.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listPost(query)` | GET | `/system/post/list` |
| `getPost(postId)` | GET | `/system/post/{postId}` |
| `addPost(data)` | POST | `/system/post` |
| `updatePost(data)` | PUT | `/system/post` |
| `delPost(postId)` | DELETE | `/system/post/{postId}` |

#### 系统管理—通知 `src/api/system/notice.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listNotice(query)` | GET | `/system/notice/list` |
| `getNotice(noticeId)` | GET | `/system/notice/{noticeId}` |
| `addNotice(data)` | POST | `/system/notice` |
| `updateNotice(data)` | PUT | `/system/notice` |
| `delNotice(noticeId)` | DELETE | `/system/notice/{noticeId}` |

#### 监控管理 `src/api/monitor/`
| 文件 | 函数 | 方法 | URL |
|------|------|------|-----|
| cache.js | `getCache()` | GET | `/monitor/cache` |
| cache.js | `listCacheName()` | GET | `/monitor/cache/getNames` |
| cache.js | `listCacheKey(cacheName)` | GET | `/monitor/cache/getKeys/{cacheName}` |
| cache.js | `getCacheValue(cacheName, cacheKey)` | GET | `/monitor/cache/getValue/{cacheName}/{cacheKey}` |
| cache.js | `clearCacheName(cacheName)` | DELETE | `/monitor/cache/clearCacheName/{cacheName}` |
| cache.js | `clearCacheKey(cacheKey)` | DELETE | `/monitor/cache/clearCacheKey/{cacheKey}` |
| cache.js | `clearCacheAll()` | DELETE | `/monitor/cache/clearCacheAll` |
| server.js | `getServer()` | GET | `/monitor/server` |
| job.js | `listJob(query)` | GET | `/monitor/job/list` |
| job.js | `getJob(jobId)` | GET | `/monitor/job/{jobId}` |
| job.js | `addJob(data)` | POST | `/monitor/job` |
| job.js | `updateJob(data)` | PUT | `/monitor/job` |
| job.js | `delJob(jobId)` | DELETE | `/monitor/job/{jobId}` |
| job.js | `changeJobStatus(jobId, status)` | PUT | `/monitor/job/changeStatus` |
| job.js | `runJob(jobId, jobGroup)` | PUT | `/monitor/job/run` |
| jobLog.js | `listJobLog(query)` | GET | `/monitor/jobLog/list` |
| jobLog.js | `delJobLog(jobLogId)` | DELETE | `/monitor/jobLog/{jobLogId}` |
| jobLog.js | `cleanJobLog()` | DELETE | `/monitor/jobLog/clean` |
| logininfor.js | `list(query)` | GET | `/monitor/logininfor/list` |
| logininfor.js | `delLogininfor(infoId)` | DELETE | `/monitor/logininfor/{infoId}` |
| logininfor.js | `unlockLogininfor(userName)` | GET | `/monitor/logininfor/unlock/{userName}` |
| logininfor.js | `cleanLogininfor()` | DELETE | `/monitor/logininfor/clean` |
| operlog.js | `list(query)` | GET | `/monitor/operlog/list` |
| operlog.js | `delOperlog(operId)` | DELETE | `/monitor/operlog/{operId}` |
| operlog.js | `cleanOperlog()` | DELETE | `/monitor/operlog/clean` |
| online.js | `list(query)` | GET | `/monitor/online/list` |
| online.js | `forceLogout(tokenId)` | DELETE | `/monitor/online/{tokenId}` |

#### 代码生成 `src/api/tool/gen.js`
| 函数 | 方法 | URL |
|------|------|-----|
| `listTable(query)` | GET | `/tool/gen/list` |
| `listDbTable(query)` | GET | `/tool/gen/db/list` |
| `getGenTable(tableId)` | GET | `/tool/gen/{tableId}` |
| `updateGenTable(data)` | PUT | `/tool/gen` |
| `importTable(data)` | POST | `/tool/gen/importTable` |
| `createTable(data)` | POST | `/tool/gen/createTable` |
| `previewTable(tableId)` | GET | `/tool/gen/preview/{tableId}` |
| `delTable(tableId)` | DELETE | `/tool/gen/{tableId}` |
| `genCode(tableName)` | GET | `/tool/gen/genCode/{tableName}` |
| `synchDb(tableName)` | GET | `/tool/gen/synchDb/{tableName}` |

### 4.2 自定义业务API（Mock模式，待接入真实后端）

#### 数据中心 `src/api/data/index.js`
| 函数 | 说明 |
|------|------|
| `getDashboardStats()` | 获取仪表盘统计数据 |
| `getTrendData()` | 获取趋势数据 |
| `getMerchantRankList()` | 获取商家排行 |

#### 商家中心 `src/api/merchant/index.js`
| 函数 | 参数 | 说明 |
|------|------|------|
| `getMerchantList(query)` | query: {pageNum, pageSize, name?, level?, status?} | 商家列表(分页) |
| `getMerchantDetail(id)` | id | 商家详情 |
| `getMerchantLevels()` | - | 商家等级列表 |
| `getMerchantFlowList(query)` | query: {pageNum, pageSize, merchantName?, type?, dateRange?} | 商家流水列表 |
| `getWithdrawList(query)` | query: {pageNum, pageSize, merchantName?, status?} | 提现列表 |
| `auditMerchant(id, status)` | id, status | 审核商家 |

#### 订单中心 `src/api/order/index.js`
| 函数 | 参数 | 说明 |
|------|------|------|
| `getOrderList(query)` | query: {pageNum, pageSize, orderNo?, merchantName?, userName?, status?} | 订单列表(支持状态筛选) |
| `getOrderDetail(id)` | id | 订单详情 |
| `getAfterSaleList(query)` | query: {pageNum, pageSize, orderNo?, status?} | 售后列表 |
| `getAbnormalOrderList(query)` | query: {pageNum, pageSize, orderNo?} | 异常订单列表 |

#### 财务中心 `src/api/finance/index.js`
| 函数 | 参数 | 说明 |
|------|------|------|
| `getPlatformFlowList(query)` | query: {pageNum, pageSize, type?, dateRange?} | 平台流水列表 |
| `getProfitShareList(query)` | query: {pageNum, pageSize, merchantName?, status?} | 分账列表 |
| `getWithdrawList(query)` | query: {pageNum, pageSize, merchantName?, status?} | 提现记录列表 |
| `getFinanceReport()` | - | 财务报表 |
| `getIncomeStats()` | - | 收益统计 |

#### 数据分析 `src/api/analysis/index.js`
| 函数 | 说明 |
|------|------|
| `getMerchantRankList()` | 商家销售排行 |
| `getSalesStats()` | 销售统计 |
| `getOrderStats()` | 订单统计 |

#### 系统管理—日志 `src/api/system/log.js`（Mock）
| 函数 | 说明 |
|------|------|
| `getLoginLogList(query)` | 登录日志列表 |
| `getOperationLogList(query)` | 操作日志列表 |

---

## 五、Mock数据体系（src/mock/）

### 5.1 工具函数 `src/mock/index.js`

| 导出函数 | 签名 | 说明 |
|----------|------|------|
| `mockSuccess(data, msg)` | `(data, msg='操作成功')` | 返回Promise，300-600ms延迟后resolve `{code: 200, msg, data}` |
| `mockPage(list, pageNum, pageSize)` | `(list, pageNum=1, pageSize=10)` | 分页切割list，包装为mockSuccess响应 |
| `mockError(msg)` | `(msg='操作失败')` | 返回Promise reject `{code: 500, msg}` |

### 5.2 测试数据 `src/mock/data.js`

#### 数据中心数据
| 数据名 | 结构 |
|--------|------|
| `dashboardStats` | `{todayAmount: 45920, totalFlow: 3680000, todayOrders: 328, merchantCount: 156, userTotal: 12680, userTodayNew: 86}` |
| `trendData` | `{dates: [7天], orderCounts: [], amounts: [], completedCounts: []}` |
| `merchantRankList` | 8条 `{id, name, sales, orders, rating}` |

#### 商家中心数据
| 数据名 | 数量 | 结构 |
|--------|------|------|
| `merchantList` | 8条 | `{id, name, contact, phone, level, status, createTime, products, monthlySales}` |
| `merchantLevels` | 4条 | `{id, name, commission, description, merchantCount}` (S/A/B/C) |
| `merchantFlowList` | 6条 | `{id, merchantName, type, amount, time, orderId}` |
| `withdrawList` | 4条 | `{id, merchantName, amount, status, applyTime, bankName, bankAccount}` |

#### 订单中心数据
| 数据名 | 数量 | 结构 |
|--------|------|------|
| `orderList` | 8条 | `{id, orderNo, merchantName, userName, goodsName, amount, payAmount, status, createTime, payTime}` |
| `orderStatusMap` | - | `{0: 待付款, 1: 已付款, 2: 已完成, 3: 已退款, 4: 售后中, 5: 异常}` |
| `afterSaleList` | 3条 | `{id, orderNo, merchantName, userName, reason, amount, status, applyTime}` |
| `abnormalOrderList` | 2条 | `{id, orderNo, merchantName, userName, issue, amount, createTime, status}` |

#### 财务中心数据
| 数据名 | 数量 | 结构 |
|--------|------|------|
| `platformFlowList` | 5条 | `{id, type, amount, merchantName, orderNo, time, commission}` |
| `profitShareList` | 3条 | `{id, merchantName, orderNo, orderAmount, commissionRate, commission, merchantIncome, status, time}` |
| `financeReport` | - | `{totalRevenue, totalCommission, totalWithdraw, totalRefund, netProfit, monthlyData: [{month, revenue, commission, orders}]}` |
| `incomeStats` | - | `{todayIncome, monthIncome, totalIncome, todayCommission, monthCommission, totalCommission}` |

#### 数据分析数据
| 数据名 | 结构 |
|--------|------|
| `salesStats` | `{totalSales, totalOrders, avgOrderAmount, conversionRate, categoryData: [{name, sales, percent}]}` |
| `orderStats` | `{totalOrders, completedOrders, refundOrders, abnormalOrders, dailyData: [{date, newOrders, completed, refund}]}` |

#### 系统日志数据
| 数据名 | 数量 | 结构 |
|--------|------|------|
| `loginLogList` | 5条 | `{id, userName, ip, location, browser, os, status, loginTime}` |
| `operationLogList` | 5条 | `{id, operator, module, operation, method, ip, time, status, costTime}` |

---

## 六、页面清单（src/views/）

### 6.1 首页 `index.vue`
- **组件**: 3个CountTo统计卡片 + 3个ECharts图表(订单趋势折线/营收柱状/订单状态饼图) + el-table热销TOP5 + 快捷入口卡片
- **API**: 无（硬编码数据）
- **生命周期**: mounted初始化图表，beforeDestroy销毁

### 6.2 登录页 `login.vue`
- **组件**: el-form（用户名/密码/验证码）+ 验证码图片 + 记住密码
- **API**: `getCodeImg()`, Vuex `Login`
- **品牌**: "零点科技后台管理"

### 6.3 数据中心页面

| 文件 | 组件 | ECharts | API调用 |
|------|------|---------|---------|
| `data-overview/overview.vue` | 6个CountTo卡片 + 1个折线图 | 面积图(7天趋势) | `getDashboardStats()`, `getTrendData()` |
| `data-overview/today-flow.vue` | 3个统计卡片 + 1个柱状图 | 24小时分布柱状图 | `getDashboardStats()` |
| `data-overview/order-trend.vue` | 3个统计卡片 + 1个折线图 | 12个月流水折线图 | `getDashboardStats()` |
| `data-overview/today-orders.vue` | 4个统计卡片 + 1个饼图 | 订单状态分布饼图 | `getDashboardStats()` |
| `data-overview/merchant-stats.vue` | 4个统计卡片 + 2个饼图 | 等级/类型分布饼图 | `getDashboardStats()` |
| `data-overview/user-growth.vue` | 4个统计卡片 + 1个面积图 | 30天新增用户面积图 | `getDashboardStats()` |

### 6.4 商家中心页面

| 文件 | 搜索表单 | 表格列 | 弹窗 | API调用 |
|------|----------|--------|------|---------|
| `merchant/list.vue` | 商家名称/等级/状态 | ID/名称/联系人/电话/等级tag/状态tag/商品数/月销售额/入驻时间 | 审核弹窗(通过/拒绝) | `getMerchantList()`, `auditMerchant()` |
| `merchant/detail.vue` | - | el-descriptions(9字段) + el-tabs(基本信息/商品/订单/流水占位) | - | `getMerchantDetail(id)` |
| `merchant/level.vue` | - | 等级名称/佣金比例/描述/商家数量 | 编辑弹窗(el-input-number佣金) | `getMerchantLevels()` |
| `merchant/flow.vue` | 商家名称/类型/时间范围 | 商家/类型tag/金额(正绿负红)/关联订单号/时间 | - | `getMerchantFlowList()` |
| `merchant/withdraw.vue` | 商家名称/状态 | 商家/金额/银行/银行账号(脱敏)/申请时间/状态tag | 详情弹窗/拒绝弹窗 | `getWithdrawList()` |

### 6.5 订单中心页面

| 文件 | 搜索表单 | 表格列 | 弹窗 | API调用 |
|------|----------|--------|------|---------|
| `order/all.vue` | 订单号/商家/用户/状态/时间 | 订单号/商家/用户/商品/金额/实付/状态tag/下单/支付时间 | - | `getOrderList()` |
| `order/detail.vue` | - | 状态卡片(图标+描述+操作按钮) + el-descriptions(商品/支付/用户) | - | `getOrderDetail(id)` |
| `order/after-sale.vue` | 订单号/状态 | 订单号/商家/用户/原因/金额/申请时间/状态tag | 处理弹窗(通过/拒绝+备注) | `getAfterSaleList()` |
| `order/abnormal.vue` | 订单号/状态 | 订单号/商家/用户/异常原因/金额/创建时间/状态tag | 处理弹窗(处理方式+备注) | `getAbnormalOrderList()` |

### 6.6 财务中心页面

| 文件 | 组件 | API调用 |
|------|------|---------|
| `finance/platform-flow.vue` | 搜索(类型/时间) + el-table(类型tag/金额正负色/商家/订单号/佣金/时间) | `getPlatformFlowList()` |
| `finance/profit-sharing.vue` | 搜索(商家/状态) + el-table(商家/订单号/金额/佣金比例/佣金/商家收入/状态tag) | `getProfitShareList()` |
| `finance/withdraw-record.vue` | 搜索(商家/状态) + el-table(商家/金额/银行/账号脱敏/申请时间/状态tag/完成时间) | `getWithdrawList()` |
| `finance/report.vue` | 5个汇总指标卡(累计营收/佣金/提现/退款/净利润) + 月度明细el-table | `getFinanceReport()` |
| `finance/income.vue` | 6个指标卡(今日/本月/累计收入 + 今日/本月/累计佣金) | `getIncomeStats()` |

### 6.7 数据分析页面

| 文件 | 组件 | ECharts | API调用 |
|------|------|---------|---------|
| `data-analysis/rank.vue` | 搜索(商家名称) + el-table(排名奖杯/名称/销售额/订单数/评分el-rate) | - | `getMerchantRankList()` |
| `data-analysis/sales.vue` | 4个统计卡 + 饼图(品类占比) + 柱状+折线双Y轴(7日趋势) + el-table+进度条 | 饼图+柱状折线 | `getSalesStats()` |
| `data-analysis/order-stats.vue` | 4个统计卡 + 堆叠柱状图 + el-table+进度条(完成率) | 堆叠柱状图 | `getOrderStats()` |

### 6.8 系统管理页面

| 文件 | 搜索表单 | API调用 |
|------|----------|---------|
| `ops-log/login.vue` | 用户名/状态/时间范围 | `getLoginLogList()` |
| `ops-log/operation.vue` | 操作人/模块/状态/时间范围 | `getOperationLogList()` |

系统管理的user/role/menu/config页面复用若依原有实现。

---

## 七、公共组件（src/components/）

| 组件 | 路径 | 说明 |
|------|------|------|
| Breadcrumb | `Breadcrumb/index.vue` | 面包屑导航 |
| Crontab | `Crontab/` (8个子组件) | Cron表达式构建器 |
| DictTag | `DictTag/index.vue` | 字典标签渲染 |
| Editor | `Editor/index.vue` | 富文本编辑器(Quill) |
| FileUpload | `FileUpload/index.vue` | 文件上传 |
| Hamburger | `Hamburger/index.vue` | 侧边栏折叠按钮 |
| HeaderSearch | `HeaderSearch/index.vue` | 顶部搜索 |
| IconSelect | `IconSelect/index.vue` | 图标选择器 |
| ImagePreview | `ImagePreview/index.vue` | 图片预览(缩放) |
| ImageUpload | `ImageUpload/index.vue` | 图片上传 |
| Pagination | `Pagination/index.vue` | 分页封装 |
| PanThumb | `PanThumb/index.vue` | 缩略图 |
| ParentView | `ParentView/index.vue` | 嵌套路由空wrapper |
| RightPanel | `RightPanel/index.vue` | 右侧设置面板 |
| RightToolbar | `RightToolbar/index.vue` | 表格工具栏(刷新/列/密度) |
| Screenfull | `Screenfull/index.vue` | 全屏切换 |
| SizeSelect | `SizeSelect/index.vue` | Element UI尺寸选择 |
| SvgIcon | `SvgIcon/index.vue` | SVG图标组件 |
| ThemePicker | `ThemePicker/index.vue` | 主题色选择器 |
| TopNav | `TopNav/index.vue` | 顶部导航栏 |
| iFrame | `iFrame/index.vue` | iFrame封装 |

---

## 八、布局系统（src/layout/）

```
layout/
├── index.vue           ← 主布局壳（sidebar+navbar+tags-view+app-main+settings）
├── components/
│   ├── AppMain.vue     ← router-view + keep-alive
│   ├── Navbar.vue      ← 顶部栏（折叠/面包屑/全屏/尺寸/主题/用户头像下拉）
│   ├── Sidebar/
│   │   ├── index.vue   ← 侧边栏菜单(el-menu)，遍历sidebarRouters
│   │   ├── SidebarItem.vue  ← 递归菜单项渲染
│   │   ├── Logo.vue    ← 侧边栏Logo
│   │   ├── Link.vue    ← 链接wrapper(内部/外部)
│   │   └── Item.vue    ← 菜单项icon+title渲染
│   ├── TagsView/
│   │   ├── index.vue   ← 标签页栏（右键菜单：关闭/关闭其他/全部/左侧/右侧）
│   │   └── ScrollPane.vue  ← 标签滚动容器
│   ├── Settings/
│   │   └── index.vue   ← 布局设置面板
│   ├── IframeToggle/
│   │   └── index.vue   ← iframe视图切换
│   └── InnerLink/
│       └── index.vue   ← 内部链接iframe
└── mixins/
    └── ResizeHandler   ← 响应式处理
```

---

## 九、自定义指令（src/directives/）

| 指令 | 文件 | 说明 |
|------|------|------|
| `v-hasPermi` | `permission/hasPermi.js` | 权限检查，无权限则移除DOM元素。检查`store.getters.permissions`，`*:*:*`为超级权限 |
| `v-hasRole` | `permission/hasRole.js` | 角色检查，无角色则移除DOM。`admin`为超级角色 |
| `v-clipboard` | `module/clipboard.js` | 复制/剪切到剪贴板，支持success/error回调 |
| `v-dialogDrag` | `dialog/drag.js` | el-dialog可拖拽 |
| `v-dialogDragWidth` | `dialog/dragWidth.js` | 对话框宽度拖拽调整 |
| `v-dialogDragHeight` | `dialog/dragHeight.js` | 对话框高度拖拽调整 |

---

## 十、工具函数（src/utils/）

### 10.1 request.js（Axios封装）
- `baseURL`: `process.env.VUE_APP_BASE_API`
- `timeout`: 10000ms
- **请求拦截**: 添加Bearer token，GET参数映射为URL查询，防重复提交(1s窗口, 5MB限制)
- **响应拦截**: 401重登录，500错误提示，601警告，成功返回`res.data`
- **导出**: `service`(axios实例), `isRelogin`, `download(url, params, filename, config)`

### 10.2 auth.js
| 函数 | 说明 |
|------|------|
| `getToken()` | `Cookies.get('Admin-Token')` |
| `setToken(token)` | `Cookies.set('Admin-Token', token)` |
| `removeToken()` | `Cookies.remove('Admin-Token')` |

### 10.3 validate.js
| 函数 | 签名 | 说明 |
|------|------|------|
| `isPathMatch` | (pattern, path) => bool | 正则路径匹配 |
| `isEmpty` | (value) => bool | 空值检查 |
| `isHttp` | (url) => bool | http/https前缀检查 |
| `isExternal` | (path) => bool | 外部链接检查 |
| `validUsername` | (str) => bool | 用户名校验 |
| `validURL` | (url) => bool | URL校验 |
| `validEmail` | (email) => bool | 邮箱校验 |
| `isString` / `isArray` | (any) => bool | 类型检查 |

### 10.4 ruoyi.js
| 函数 | 签名 | 说明 |
|------|------|------|
| `parseTime` | (time, pattern?) | 日期格式化，默认`{y}-{m}-{d} {h}:{i}:{s}` |
| `resetForm` | (refName) | 调用`$refs[refName].resetFields()` |
| `addDateRange` | (params, dateRange, propName?) | 添加beginTime/endTime |
| `selectDictLabel` | (datas, value) | 字典值转标签 |
| `selectDictLabels` | (datas, value, separator?) | 多字典值转标签 |
| `handleTree` | (data, id?, parentId?, children?) | 扁平数组构建树 |
| `blobValidate` | (data) | 检查blob类型 |

### 10.5 permission.js
| 函数 | 说明 |
|------|------|
| `checkPermi(value)` | 权限数组检查 |
| `checkRole(value)` | 角色数组检查 |

### 10.6 其他工具
| 文件 | 说明 |
|------|------|
| `errorCode.js` | 错误码映射(401/403/404/default) |
| `scroll-to.js` | 平滑滚动(easeInOutQuad) |
| `jsencrypt.js` | RSA加解密 |
| `dict/` | 字典数据管理系统(6个文件) |
| `generator/` | 代码生成器工具(6个文件) |

---

## 十一、权限控制（src/permission.js）

**路由守卫流程:**
1. NProgress开始
2. Token存在:
   - 目标是`/login` → 重定向到`/`
   - 在白名单中 → 放行
   - 角色未加载 → dispatch `GetInfo` → `GenerateRoutes` → `addRoutes` → 重定向回原目标
   - 角色已加载 → 放行
3. Token不存在:
   - 在白名单中 → 放行
   - 否则 → 重定向到`/login?redirect={path}`

**白名单:** `/login`, `/register`

---

## 附录：数据状态映射

### 订单状态
| 值 | 文本 | 标签颜色 |
|----|------|----------|
| 0 | 待付款 | warning(橙色) |
| 1 | 已付款 | primary(蓝色) |
| 2 | 已完成 | success(绿色) |
| 3 | 已退款 | info(灰色) |
| 4 | 售后中 | danger(红色) |
| 5 | 异常 | danger(红色) |

### 商家状态
| 值 | 文本 | 标签颜色 |
|----|------|----------|
| 0 | 禁用 | info(灰色) |
| 1 | 正常 | success(绿色) |
| 2 | 待审核 | warning(橙色) |

### 商家等级
| 值 | 标签颜色 |
|----|----------|
| S | danger(红色) |
| A | warning(橙色) |
| B | primary(蓝色) |
| C | info(灰色) |

### 提现状态
| 值 | 文本 | 标签颜色 |
|----|------|----------|
| 0 | 待审核 | warning(橙色) |
| 1 | 已审核 | primary(蓝色) |
| 2 | 已完成 | success(绿色) |
| 3 | 已拒绝 | danger(红色) |

### 售后状态
| 值 | 文本 | 标签颜色 |
|----|------|----------|
| 0 | 待处理 | warning(橙色) |
| 1 | 处理中 | primary(蓝色) |
| 2 | 已完成 | success(绿色) |

### 日志状态
| 值 | 文本 | 标签颜色 |
|----|------|----------|
| 0 | 成功 | success(绿色) |
| 1 | 失败 | danger(红色) |
