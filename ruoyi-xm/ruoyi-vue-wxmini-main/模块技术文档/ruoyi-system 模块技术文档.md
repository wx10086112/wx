# ruoyi-system 模块 -- 完整技术文档

---

## 1. 整体目录结构

```
ruoyi-system/
|-- pom.xml                                          (Maven 构建配置, artifactId: ruoyi-system, 依赖 ruoyi-common)
|-- src/
    |-- main/
        |-- java/com/ruoyi/
        |   |-- system/
        |   |   |-- domain/
        |   |   |   |-- SysPost.java                  (岗位实体 - 本模块)
        |   |   |   |-- SysConfig.java                (参数配置实体 - 本模块)
        |   |   |   |-- SysNotice.java                (通知公告实体 - 本模块)
        |   |   |   |-- SysOperLog.java               (操作日志实体 - 本模块)
        |   |   |   |-- SysLogininfor.java            (登录日志实体 - 本模块)
        |   |   |   |-- SysUserOnline.java            (在线用户实体 - 本模块)
        |   |   |   |-- SysUserRole.java              (用户-角色关联实体 - 本模块)
        |   |   |   |-- SysUserPost.java              (用户-岗位关联实体 - 本模块)
        |   |   |   |-- SysRoleMenu.java              (角色-菜单关联实体 - 本模块)
        |   |   |   |-- SysRoleDept.java              (角色-部门关联实体 - 本模块)
        |   |   |   |-- SysCache.java                 (缓存信息实体 - 本模块)
        |   |   |   |-- vo/
        |   |   |       |-- RouterVo.java             (路由配置 VO)
        |   |   |       |-- MetaVo.java               (路由显示信息 VO)
        |   |   |-- mapper/
        |   |   |   |-- SysUserMapper.java
        |   |   |   |-- SysRoleMapper.java
        |   |   |   |-- SysDeptMapper.java
        |   |   |   |-- SysMenuMapper.java
        |   |   |   |-- SysPostMapper.java
        |   |   |   |-- SysConfigMapper.java
        |   |   |   |-- SysDictTypeMapper.java
        |   |   |   |-- SysDictDataMapper.java
        |   |   |   |-- SysNoticeMapper.java
        |   |   |   |-- SysOperLogMapper.java
        |   |   |   |-- SysLogininforMapper.java
        |   |   |   |-- SysUserRoleMapper.java
        |   |   |   |-- SysUserPostMapper.java
        |   |   |   |-- SysRoleMenuMapper.java
        |   |   |   |-- SysRoleDeptMapper.java
        |   |   |-- service/
        |   |   |   |-- ISysUserService.java          (接口)
        |   |   |   |-- ISysRoleService.java           (接口)
        |   |   |   |-- ISysDeptService.java           (接口)
        |   |   |   |-- ISysMenuService.java           (接口)
        |   |   |   |-- ISysPostService.java           (接口)
        |   |   |   |-- ISysConfigService.java         (接口)
        |   |   |   |-- ISysDictTypeService.java       (接口)
        |   |   |   |-- ISysDictDataService.java       (接口)
        |   |   |   |-- ISysNoticeService.java         (接口)
        |   |   |   |-- ISysOperLogService.java        (接口)
        |   |   |   |-- ISysLogininforService.java     (接口)
        |   |   |   |-- ISysUserOnlineService.java     (接口)
        |   |   |   |-- impl/
        |   |   |       |-- SysUserServiceImpl.java
        |   |   |       |-- SysRoleServiceImpl.java
        |   |   |       |-- SysDeptServiceImpl.java
        |   |   |       |-- SysMenuServiceImpl.java
        |   |   |       |-- SysPostServiceImpl.java
        |   |   |       |-- SysConfigServiceImpl.java
        |   |   |       |-- SysDictTypeServiceImpl.java
        |   |   |       |-- SysDictDataServiceImpl.java
        |   |   |       |-- SysNoticeServiceImpl.java
        |   |   |       |-- SysOperLogServiceImpl.java
        |   |   |       |-- SysLogininforServiceImpl.java
        |   |   |       |-- SysUserOnlineServiceImpl.java
        |   |-- wxmini/
        |   |       |-- domain/
        |   |       |   |-- UserInfo.java             (微信小程序用户实体)
        |   |       |-- mapper/
        |   |       |   |-- UserInfoMapper.java
        |   |       |-- service/
        |   |       |   |-- IUserInfoService.java     (接口)
        |   |       |   |-- impl/
        |   |       |       |-- UserInfoServiceImpl.java
        |   |-- resources/
        |       |-- mapper/
        |           |-- system/
        |           |   |-- SysUserMapper.xml
        |           |   |-- SysRoleMapper.xml
        |           |   |-- SysDeptMapper.xml
        |           |   |-- SysMenuMapper.xml
        |           |   |-- SysPostMapper.xml
        |           |   |-- SysConfigMapper.xml
        |           |   |-- SysDictTypeMapper.xml
        |           |   |-- SysDictDataMapper.xml
        |           |   |-- SysNoticeMapper.xml
        |           |   |-- SysOperLogMapper.xml
        |           |   |-- SysLogininforMapper.xml
        |           |   |-- SysUserRoleMapper.xml
        |           |   |-- SysUserPostMapper.xml
        |           |   |-- SysRoleMenuMapper.xml
        |           |   |-- SysRoleDeptMapper.xml
        |           |-- wxmini/
        |               |-- UserInfoMapper.xml
```

---

## 2. Domain/Entity 层

### 2.1 公共基类 `BaseEntity` (位于 `ruoyi-common` 模块)
- **包**: `com.ruoyi.common.core.domain`
- **继承字段** (所有继承它的实体都有):

| 字段 | 类型 | 说明 |
|------|------|------|
| searchValue | String | 搜索值 |
| createBy | String | 创建者 |
| createTime | Date | 创建时间 |
| updateBy | String | 更新者 |
| updateTime | Date | 更新时间 |
| remark | String | 备注 |
| params | Map\<String, Object\> | 请求参数 |

---

### 2.2 本模块实体 (位于 `ruoyi-system`)

#### `com.ruoyi.system.domain.SysPost` -- 岗位表

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | Long | 岗位序号 (主键) |
| postCode | String | 岗位编码 |
| postName | String | 岗位名称 |
| postSort | Integer | 岗位排序 |
| status | String | 状态 (0正常 1停用) |
| flag | boolean | 用户是否存在此岗位标识 (默认false) |

**继承自 BaseEntity**: searchValue, createBy, createTime, updateBy, updateTime, remark, params

---

#### `com.ruoyi.system.domain.SysConfig` -- 参数配置表

| 字段 | 类型 | 说明 |
|------|------|------|
| configId | Long | 参数主键 |
| configName | String | 参数名称 |
| configKey | String | 参数键名 |
| configValue | String | 参数键值 |
| configType | String | 系统内置 (Y是 N否) |

**继承自 BaseEntity**

---

#### `com.ruoyi.system.domain.SysNotice` -- 通知公告表

| 字段 | 类型 | 说明 |
|------|------|------|
| noticeId | Long | 公告ID |
| noticeTitle | String | 公告标题 |
| noticeType | String | 公告类型 (1通知 2公告) |
| noticeContent | String | 公告内容 |
| status | String | 公告状态 (0正常 1关闭) |

**继承自 BaseEntity**

---

#### `com.ruoyi.system.domain.SysOperLog` -- 操作日志记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| operId | Long | 日志主键 |
| title | String | 操作模块 |
| businessType | Integer | 业务类型 (0其它 1新增 2修改 3删除) |
| businessTypes | Integer[] | 业务类型数组 |
| method | String | 请求方法 |
| requestMethod | String | 请求方式 |
| operatorType | Integer | 操作类别 (0其它 1后台用户 2手机端用户) |
| operName | String | 操作人员 |
| deptName | String | 部门名称 |
| operUrl | String | 请求URL |
| operIp | String | 操作地址 |
| operLocation | String | 操作地点 |
| operParam | String | 请求参数 |
| jsonResult | String | 返回参数 |
| status | Integer | 操作状态 (0正常 1异常) |
| errorMsg | String | 错误消息 |
| operTime | Date | 操作时间 |
| costTime | Long | 消耗时间 |

**继承自 BaseEntity**

---

#### `com.ruoyi.system.domain.SysLogininfor` -- 系统访问记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| infoId | Long | 访问ID |
| userName | String | 用户账号 |
| status | String | 登录状态 (0成功 1失败) |
| ipaddr | String | 登录IP地址 |
| loginLocation | String | 登录地点 |
| browser | String | 浏览器类型 |
| os | String | 操作系统 |
| msg | String | 提示消息 |
| loginTime | Date | 访问时间 |

**继承自 BaseEntity**

---

#### `com.ruoyi.system.domain.SysUserOnline` -- 当前在线会话

| 字段 | 类型 | 说明 |
|------|------|------|
| tokenId | String | 会话编号 |
| deptName | String | 部门名称 |
| userName | String | 用户名称 |
| ipaddr | String | 登录IP地址 |
| loginLocation | String | 登录地址 |
| browser | String | 浏览器类型 |
| os | String | 操作系统 |
| loginTime | Long | 登录时间 |

**不继承 BaseEntity**

---

#### `com.ruoyi.system.domain.SysUserRole` -- 用户和角色关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| roleId | Long | 角色ID |

---

#### `com.ruoyi.system.domain.SysUserPost` -- 用户和岗位关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| postId | Long | 岗位ID |

---

#### `com.ruoyi.system.domain.SysRoleMenu` -- 角色和菜单关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | Long | 角色ID |
| menuId | Long | 菜单ID |

---

#### `com.ruoyi.system.domain.SysRoleDept` -- 角色和部门关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | Long | 角色ID |
| deptId | Long | 部门ID |

---

#### `com.ruoyi.system.domain.SysCache` -- 缓存信息

| 字段 | 类型 | 说明 |
|------|------|------|
| cacheName | String | 缓存名称 |
| cacheKey | String | 缓存键名 |
| cacheValue | String | 缓存内容 |
| remark | String | 备注 |

**不继承 BaseEntity; 有3个构造方法**

---

### 2.3 `com.ruoyi.system.domain.vo` 包

#### `RouterVo` -- 路由配置信息

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 路由名字 |
| path | String | 路由地址 |
| hidden | boolean | 是否隐藏路由 |
| redirect | String | 重定向地址 |
| component | String | 组件地址 |
| query | String | 路由参数 |
| alwaysShow | Boolean | 是否总是显示 |
| meta | MetaVo | 其他元素 |
| children | List\<RouterVo\> | 子路由 |

---

#### `MetaVo` -- 路由显示信息

| 字段 | 类型 | 说明 |
|------|------|------|
| title | String | 设置该路由在侧边栏和面包屑中展示的名字 |
| icon | String | 设置该路由的图标 |
| noCache | boolean | 是否被 `<keep-alive>` 缓存 |
| link | String | 内链地址 |

---

### 2.4 公共实体 (位于 `ruoyi-common` 模块，被本模块 Mapper 引用)

#### `com.ruoyi.common.core.domain.entity.SysUser` -- 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| deptId | Long | 部门ID |
| userName | String | 用户账号 |
| nickName | String | 用户昵称 |
| email | String | 用户邮箱 |
| phonenumber | String | 手机号码 |
| sex | String | 用户性别 (0男 1女 2未知) |
| avatar | String | 用户头像 |
| password | String | 密码 |
| status | String | 账号状态 (0正常 1停用) |
| delFlag | String | 删除标志 (0存在 2删除) |
| loginIp | String | 最后登录IP |
| loginDate | Date | 最后登录时间 |
| dept | SysDept | 部门对象 |
| roles | List\<SysRole\> | 角色对象 |
| roleIds | Long[] | 角色组 |
| postIds | Long[] | 岗位组 |
| roleId | Long | 角色ID |

**继承自 BaseEntity; 有方法 `isAdmin()` / `static isAdmin(Long)`**

---

#### `com.ruoyi.common.core.domain.entity.SysRole` -- 角色表

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | Long | 角色ID |
| roleName | String | 角色名称 |
| roleKey | String | 角色权限 |
| roleSort | Integer | 角色排序 |
| dataScope | String | 数据范围 (1所有 2自定义 3本部门 4本部门及以下 5仅本人) |
| menuCheckStrictly | boolean | 菜单树选择项是否关联显示 |
| deptCheckStrictly | boolean | 部门树选择项是否关联显示 |
| status | String | 角色状态 (0正常 1停用) |
| delFlag | String | 删除标志 |
| flag | boolean | 用户是否存在此角色标识 |
| menuIds | Long[] | 菜单组 |
| deptIds | Long[] | 部门组(数据权限) |
| permissions | Set\<String\> | 角色菜单权限 |

**继承自 BaseEntity; 有方法 `isAdmin()` / `static isAdmin(Long)`**

---

#### `com.ruoyi.common.core.domain.entity.SysDept` -- 部门表

| 字段 | 类型 | 说明 |
|------|------|------|
| deptId | Long | 部门ID |
| parentId | Long | 父部门ID |
| ancestors | String | 祖级列表 |
| deptName | String | 部门名称 |
| orderNum | Integer | 显示顺序 |
| leader | String | 负责人 |
| phone | String | 联系电话 |
| email | String | 邮箱 |
| status | String | 部门状态 (0正常 1停用) |
| delFlag | String | 删除标志 |
| parentName | String | 父部门名称 |
| children | List\<SysDept\> | 子部门 |

**继承自 BaseEntity**

---

#### `com.ruoyi.common.core.domain.entity.SysMenu` -- 菜单权限表

| 字段 | 类型 | 说明 |
|------|------|------|
| menuId | Long | 菜单ID |
| menuName | String | 菜单名称 |
| parentName | String | 父菜单名称 |
| parentId | Long | 父菜单ID |
| orderNum | Integer | 显示顺序 |
| path | String | 路由地址 |
| component | String | 组件路径 |
| query | String | 路由参数 |
| routeName | String | 路由名称 |
| isFrame | String | 是否为外链 (0是 1否) |
| isCache | String | 是否缓存 (0缓存 1不缓存) |
| menuType | String | 类型 (M目录 C菜单 F按钮) |
| visible | String | 显示状态 (0显示 1隐藏) |
| status | String | 菜单状态 (0正常 1停用) |
| perms | String | 权限字符串 |
| icon | String | 菜单图标 |
| children | List\<SysMenu\> | 子菜单 |

**继承自 BaseEntity**

---

#### `com.ruoyi.common.core.domain.entity.SysDictType` -- 字典类型表

| 字段 | 类型 | 说明 |
|------|------|------|
| dictId | Long | 字典主键 |
| dictName | String | 字典名称 |
| dictType | String | 字典类型 |
| status | String | 状态 (0正常 1停用) |

**继承自 BaseEntity**

---

#### `com.ruoyi.common.core.domain.entity.SysDictData` -- 字典数据表

| 字段 | 类型 | 说明 |
|------|------|------|
| dictCode | Long | 字典编码 |
| dictSort | Long | 字典排序 |
| dictLabel | String | 字典标签 |
| dictValue | String | 字典键值 |
| dictType | String | 字典类型 |
| cssClass | String | 样式属性 |
| listClass | String | 表格字典样式 |
| isDefault | String | 是否默认 (Y是 N否) |
| status | String | 状态 (0正常 1停用) |

**继承自 BaseEntity**

---

### 2.5 微信小程序实体

#### `com.ruoyi.wxmini.domain.UserInfo` -- 小程序用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| userId | String | 平台用户ID |
| userName | String | 用户名 |
| userType | String | 用户类型 |
| phone | String | 手机号 |
| openId | String | 微信用户唯一标识 |
| unionId | String | 微信全平台用户唯一标识 |
| avatarUrl | String | 用户头像 |

**继承自 BaseEntity**

---

## 3. Mapper 层 (所有接口及其方法)

### 3.1 SysUserMapper -- 用户表数据层

```java
List<SysUser> selectUserList(SysUser sysUser);
List<SysUser> selectAllocatedList(SysUser user);
List<SysUser> selectUnallocatedList(SysUser user);
SysUser selectUserByUserName(String userName);
SysUser selectUserById(Long userId);
int insertUser(SysUser user);
int updateUser(SysUser user);
int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar);
int resetUserPwd(@Param("userName") String userName, @Param("password") String password);
int deleteUserById(Long userId);
int deleteUserByIds(Long[] userIds);
SysUser checkUserNameUnique(String userName);
SysUser checkPhoneUnique(String phonenumber);
SysUser checkEmailUnique(String email);
```

---

### 3.2 SysRoleMapper -- 角色表数据层

```java
List<SysRole> selectRoleList(SysRole role);
List<SysRole> selectRolePermissionByUserId(Long userId);
List<SysRole> selectRoleAll();
List<Long> selectRoleListByUserId(Long userId);
SysRole selectRoleById(Long roleId);
List<SysRole> selectRolesByUserName(String userName);
SysRole checkRoleNameUnique(String roleName);
SysRole checkRoleKeyUnique(String roleKey);
int updateRole(SysRole role);
int insertRole(SysRole role);
int deleteRoleById(Long roleId);
int deleteRoleByIds(Long[] roleIds);
```

---

### 3.3 SysDeptMapper -- 部门管理数据层

```java
List<SysDept> selectDeptList(SysDept dept);
List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);
SysDept selectDeptById(Long deptId);
List<SysDept> selectChildrenDeptById(Long deptId);
int selectNormalChildrenDeptById(Long deptId);
int hasChildByDeptId(Long deptId);
int checkDeptExistUser(Long deptId);
SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);
int insertDept(SysDept dept);
int updateDept(SysDept dept);
void updateDeptStatusNormal(Long[] deptIds);
int updateDeptChildren(@Param("depts") List<SysDept> depts);
int deleteDeptById(Long deptId);
```

---

### 3.4 SysMenuMapper -- 菜单表数据层

```java
List<SysMenu> selectMenuList(SysMenu menu);
List<String> selectMenuPerms();
List<SysMenu> selectMenuListByUserId(SysMenu menu);
List<String> selectMenuPermsByRoleId(Long roleId);
List<String> selectMenuPermsByUserId(Long userId);
List<SysMenu> selectMenuTreeAll();
List<SysMenu> selectMenuTreeByUserId(Long userId);
List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);
SysMenu selectMenuById(Long menuId);
int hasChildByMenuId(Long menuId);
int insertMenu(SysMenu menu);
int updateMenu(SysMenu menu);
int deleteMenuById(Long menuId);
SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);
```

---

### 3.5 SysPostMapper -- 岗位信息数据层

```java
List<SysPost> selectPostList(SysPost post);
List<SysPost> selectPostAll();
SysPost selectPostById(Long postId);
List<Long> selectPostListByUserId(Long userId);
List<SysPost> selectPostsByUserName(String userName);
int deletePostById(Long postId);
int deletePostByIds(Long[] postIds);
int updatePost(SysPost post);
int insertPost(SysPost post);
SysPost checkPostNameUnique(String postName);
SysPost checkPostCodeUnique(String postCode);
```

---

### 3.6 SysConfigMapper -- 参数配置数据层

```java
SysConfig selectConfig(SysConfig config);
SysConfig selectConfigById(Long configId);
List<SysConfig> selectConfigList(SysConfig config);
SysConfig checkConfigKeyUnique(String configKey);
int insertConfig(SysConfig config);
int updateConfig(SysConfig config);
int deleteConfigById(Long configId);
int deleteConfigByIds(Long[] configIds);
```

---

### 3.7 SysDictTypeMapper -- 字典类型数据层

```java
List<SysDictType> selectDictTypeList(SysDictType dictType);
List<SysDictType> selectDictTypeAll();
SysDictType selectDictTypeById(Long dictId);
SysDictType selectDictTypeByType(String dictType);
int deleteDictTypeById(Long dictId);
int deleteDictTypeByIds(Long[] dictIds);
int insertDictType(SysDictType dictType);
int updateDictType(SysDictType dictType);
SysDictType checkDictTypeUnique(String dictType);
```

---

### 3.8 SysDictDataMapper -- 字典数据数据层

```java
List<SysDictData> selectDictDataList(SysDictData dictData);
List<SysDictData> selectDictDataByType(String dictType);
String selectDictLabel(@Param("dictType") String dictType, @Param("dictValue") String dictValue);
SysDictData selectDictDataById(Long dictCode);
int countDictDataByType(String dictType);
int deleteDictDataById(Long dictCode);
int deleteDictDataByIds(Long[] dictCodes);
int insertDictData(SysDictData dictData);
int updateDictData(SysDictData dictData);
int updateDictDataType(@Param("oldDictType") String oldDictType, @Param("newDictType") String newDictType);
```

---

### 3.9 SysNoticeMapper -- 通知公告数据层

```java
SysNotice selectNoticeById(Long noticeId);
List<SysNotice> selectNoticeList(SysNotice notice);
int insertNotice(SysNotice notice);
int updateNotice(SysNotice notice);
int deleteNoticeById(Long noticeId);
int deleteNoticeByIds(Long[] noticeIds);
```

---

### 3.10 SysOperLogMapper -- 操作日志数据层

```java
void insertOperlog(SysOperLog operLog);
List<SysOperLog> selectOperLogList(SysOperLog operLog);
int deleteOperLogByIds(Long[] operIds);
SysOperLog selectOperLogById(Long operId);
void cleanOperLog();
```

---

### 3.11 SysLogininforMapper -- 系统访问日志数据层

```java
void insertLogininfor(SysLogininfor logininfor);
List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);
int deleteLogininforByIds(Long[] infoIds);
void cleanLogininfor();
```

---

### 3.12 SysUserRoleMapper -- 用户与角色关联表数据层

```java
int deleteUserRoleByUserId(Long userId);
int deleteUserRole(Long[] ids);
int countUserRoleByRoleId(Long roleId);
int batchUserRole(List<SysUserRole> userRoleList);
int deleteUserRoleInfo(SysUserRole userRole);
int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
```

---

### 3.13 SysUserPostMapper -- 用户与岗位关联表数据层

```java
int deleteUserPostByUserId(Long userId);
int countUserPostById(Long postId);
int deleteUserPost(Long[] ids);
int batchUserPost(List<SysUserPost> userPostList);
```

---

### 3.14 SysRoleMenuMapper -- 角色与菜单关联表数据层

```java
int checkMenuExistRole(Long menuId);
int deleteRoleMenuByRoleId(Long roleId);
int deleteRoleMenu(Long[] ids);
int batchRoleMenu(List<SysRoleMenu> roleMenuList);
```

---

### 3.15 SysRoleDeptMapper -- 角色与部门关联表数据层

```java
int deleteRoleDeptByRoleId(Long roleId);
int deleteRoleDept(Long[] ids);
int selectCountRoleDeptByDeptId(Long deptId);
int batchRoleDept(List<SysRoleDept> roleDeptList);
```

---

### 3.16 UserInfoMapper (wxmini) -- 小程序用户信息

```java
UserInfo selectUserInfoById(Long id);
List<UserInfo> selectUserInfoList(UserInfo userInfo);
int insertUserInfo(UserInfo userInfo);
int updateUserInfo(UserInfo userInfo);
int deleteUserInfoById(Long id);
int deleteUserInfoByIds(Long[] ids);
UserInfo selectUserInfoByOpenId(String openId);
UserInfo selectUserInfoByUserId(String userId);
```

---

## 4. Service 层

### 4.1 ISysUserService -- 用户业务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysUser> selectUserList(SysUser user)` | 查询用户列表 |
| `List<SysUser> selectAllocatedList(SysUser user)` | 查询已分配用户角色列表 |
| `List<SysUser> selectUnallocatedList(SysUser user)` | 查询未分配用户角色列表 |
| `SysUser selectUserByUserName(String userName)` | 通过用户名查询 |
| `SysUser selectUserById(Long userId)` | 通过ID查询 |
| `String selectUserRoleGroup(String userName)` | 查询用户所属角色组 |
| `String selectUserPostGroup(String userName)` | 查询用户所属岗位组 |
| `boolean checkUserNameUnique(SysUser user)` | 校验用户名是否唯一 |
| `boolean checkPhoneUnique(SysUser user)` | 校验手机号是否唯一 |
| `boolean checkEmailUnique(SysUser user)` | 校验邮箱是否唯一 |
| `void checkUserAllowed(SysUser user)` | 校验用户是否允许操作 |
| `void checkUserDataScope(Long userId)` | 校验用户是否有数据权限 |
| `int insertUser(SysUser user)` | 新增用户 |
| `boolean registerUser(SysUser user)` | 注册用户 |
| `int updateUser(SysUser user)` | 修改用户 |
| `void insertUserAuth(Long userId, Long[] roleIds)` | 新增用户角色信息 |
| `int updateUserStatus(SysUser user)` | 修改用户状态 |
| `int updateUserProfile(SysUser user)` | 修改用户基本信息 |
| `boolean updateUserAvatar(String userName, String avatar)` | 修改用户头像 |
| `int resetPwd(SysUser user)` | 重置用户密码 |
| `int resetUserPwd(String userName, String password)` | 重置用户密码 |
| `int deleteUserById(Long userId)` | 通过ID删除用户 |
| `int deleteUserByIds(Long[] userIds)` | 批量删除用户 |
| `String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)` | 导入用户 |

**实现类**: `SysUserServiceImpl`
- 依赖: SysUserMapper, SysRoleMapper, SysPostMapper, SysUserRoleMapper, SysUserPostMapper, ISysConfigService, ISysDeptService, Validator
- 注解: `@Service`, `@Transactional` (insert/update/delete 方法)
- 额外私有方法: `insertUserRole(SysUser)`, `insertUserPost(SysUser)`, `insertUserRole(Long, Long[])`

---

### 4.2 ISysRoleService -- 角色业务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysRole> selectRoleList(SysRole role)` | 查询角色列表 |
| `List<SysRole> selectRolesByUserId(Long userId)` | 根据用户ID查询角色列表 |
| `Set<String> selectRolePermissionByUserId(Long userId)` | 查询角色权限 |
| `List<SysRole> selectRoleAll()` | 查询所有角色 |
| `List<Long> selectRoleListByUserId(Long userId)` | 根据用户ID查询角色ID列表 |
| `SysRole selectRoleById(Long roleId)` | 通过ID查询角色 |
| `boolean checkRoleNameUnique(SysRole role)` | 校验角色名称是否唯一 |
| `boolean checkRoleKeyUnique(SysRole role)` | 校验角色权限字符是否唯一 |
| `void checkRoleAllowed(SysRole role)` | 校验角色是否允许操作 |
| `void checkRoleDataScope(Long... roleIds)` | 校验角色是否有数据权限 |
| `int countUserRoleByRoleId(Long roleId)` | 查询角色使用数量 |
| `int insertRole(SysRole role)` | 新增角色 |
| `int updateRole(SysRole role)` | 修改角色 |
| `int updateRoleStatus(SysRole role)` | 修改角色状态 |
| `int authDataScope(SysRole role)` | 授权数据权限 |
| `int deleteRoleById(Long roleId)` | 通过ID删除角色 |
| `int deleteRoleByIds(Long[] roleIds)` | 批量删除角色 |
| `int deleteAuthUser(SysUserRole userRole)` | 取消授权用户 |
| `int deleteAuthUsers(Long roleId, Long[] userIds)` | 批量取消授权用户 |
| `int insertAuthUsers(Long roleId, Long[] userIds)` | 授权用户 |

**实现类**: `SysRoleServiceImpl`
- 依赖: SysRoleMapper, SysRoleMenuMapper, SysUserRoleMapper, SysRoleDeptMapper
- 额外私有方法: `insertRoleMenu(SysRole)`, `insertRoleDept(SysRole)`

---

### 4.3 ISysDeptService -- 部门管理服务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysDept> selectDeptList(SysDept dept)` | 查询部门列表 |
| `List<TreeSelect> selectDeptTreeList(SysDept dept)` | 查询部门树结构列表 |
| `List<SysDept> buildDeptTree(List<SysDept> depts)` | 构建前端所需要树结构 |
| `List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts)` | 构建前端所需要下拉树结构 |
| `List<Long> selectDeptListByRoleId(Long roleId)` | 根据角色ID查询部门ID列表 |
| `SysDept selectDeptById(Long deptId)` | 通过ID查询部门 |
| `int selectNormalChildrenDeptById(Long deptId)` | 查询正常的子部门数量 |
| `boolean hasChildByDeptId(Long deptId)` | 是否存在子部门 |
| `boolean checkDeptExistUser(Long deptId)` | 部门是否存在用户 |
| `boolean checkDeptNameUnique(SysDept dept)` | 校验部门名称是否唯一 |
| `void checkDeptDataScope(Long deptId)` | 校验部门是否有数据权限 |
| `int insertDept(SysDept dept)` | 新增部门 |
| `int updateDept(SysDept dept)` | 修改部门 |
| `int deleteDeptById(Long deptId)` | 删除部门 |

**实现类**: `SysDeptServiceImpl`
- 依赖: SysDeptMapper, SysRoleMapper
- 额外私有方法: `updateParentDeptStatusNormal(SysDept)`, `updateDeptChildren(Long, String, String)`, `recursionFn(List, SysDept)`, `getChildList(List, SysDept)`, `hasChild(List, SysDept)`

---

### 4.4 ISysMenuService -- 菜单业务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysMenu> selectMenuList(Long userId)` | 查询菜单列表 |
| `List<SysMenu> selectMenuList(SysMenu menu, Long userId)` | 查询菜单列表 |
| `Set<String> selectMenuPermsByUserId(Long userId)` | 根据用户ID查询权限 |
| `Set<String> selectMenuPermsByRoleId(Long roleId)` | 根据角色ID查询权限 |
| `List<SysMenu> selectMenuTreeByUserId(Long userId)` | 根据用户ID查询菜单树信息 |
| `List<Long> selectMenuListByRoleId(Long roleId)` | 根据角色ID查询菜单ID列表 |
| `List<RouterVo> buildMenus(List<SysMenu> menus)` | 构建前端路由所需要的菜单 |
| `List<SysMenu> buildMenuTree(List<SysMenu> menus)` | 构建前端所需要树结构 |
| `List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus)` | 构建前端所需要下拉树结构 |
| `SysMenu selectMenuById(Long menuId)` | 通过ID查询菜单 |
| `boolean hasChildByMenuId(Long menuId)` | 是否存在子菜单 |
| `boolean checkMenuExistRole(Long menuId)` | 菜单是否存在角色 |
| `int insertMenu(SysMenu menu)` | 新增菜单 |
| `int updateMenu(SysMenu menu)` | 修改菜单 |
| `int deleteMenuById(Long menuId)` | 删除菜单 |
| `boolean checkMenuNameUnique(SysMenu menu)` | 校验菜单名称是否唯一 |

**实现类**: `SysMenuServiceImpl`
- 依赖: SysMenuMapper, SysRoleMapper, SysRoleMenuMapper
- 额外公有方法: `getRouteName(SysMenu)`, `getRouteName(String, String)`, `getRouterPath(SysMenu)`, `getComponent(SysMenu)`, `isMenuFrame(SysMenu)`, `isInnerLink(SysMenu)`, `isParentView(SysMenu)`, `getChildPerms(List, int)`, `innerLinkReplaceEach(String)`
- 额外私有方法: `recursionFn(List, SysDept)`, `getChildList(List, SysDept)`, `hasChild(List, SysDept)`

---

### 4.5 ISysPostService -- 岗位信息服务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysPost> selectPostList(SysPost post)` | 查询岗位列表 |
| `List<SysPost> selectPostAll()` | 查询所有岗位 |
| `SysPost selectPostById(Long postId)` | 通过ID查询岗位 |
| `List<Long> selectPostListByUserId(Long userId)` | 根据用户ID查询岗位ID列表 |
| `boolean checkPostNameUnique(SysPost post)` | 校验岗位名称是否唯一 |
| `boolean checkPostCodeUnique(SysPost post)` | 校验岗位编码是否唯一 |
| `int countUserPostById(Long postId)` | 查询岗位使用数量 |
| `int deletePostById(Long postId)` | 通过ID删除岗位 |
| `int deletePostByIds(Long[] postIds)` | 批量删除岗位 |
| `int insertPost(SysPost post)` | 新增岗位 |
| `int updatePost(SysPost post)` | 修改岗位 |

**实现类**: `SysPostServiceImpl`
- 依赖: SysPostMapper, SysUserPostMapper

---

### 4.6 ISysConfigService -- 参数配置服务层

| 方法签名 | 说明 |
|---------|------|
| `SysConfig selectConfigById(Long configId)` | 通过ID查询参数配置 |
| `String selectConfigByKey(String configKey)` | 根据键名查询参数配置 |
| `boolean selectCaptchaEnabled()` | 校验验证码是否开启 |
| `List<SysConfig> selectConfigList(SysConfig config)` | 查询参数配置列表 |
| `int insertConfig(SysConfig config)` | 新增参数配置 |
| `int updateConfig(SysConfig config)` | 修改参数配置 |
| `void deleteConfigByIds(Long[] configIds)` | 批量删除参数配置 |
| `void loadingConfigCache()` | 加载参数缓存数据 |
| `void clearConfigCache()` | 清理参数缓存数据 |
| `void resetConfigCache()` | 重置参数缓存数据 |
| `boolean checkConfigKeyUnique(SysConfig config)` | 校验参数键名是否唯一 |

**实现类**: `SysConfigServiceImpl`
- 依赖: SysConfigMapper, RedisCache
- `@PostConstruct init()` -- 启动时加载缓存
- 额外私有方法: `getCacheKey(String)`

---

### 4.7 ISysDictTypeService -- 字典类型业务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysDictType> selectDictTypeList(SysDictType dictType)` | 查询字典类型列表 |
| `List<SysDictType> selectDictTypeAll()` | 查询所有字典类型 |
| `List<SysDictData> selectDictDataByType(String dictType)` | 根据类型查询字典数据 |
| `SysDictType selectDictTypeById(Long dictId)` | 通过ID查询字典类型 |
| `SysDictType selectDictTypeByType(String dictType)` | 根据类型查询字典类型 |
| `void deleteDictTypeByIds(Long[] dictIds)` | 批量删除字典类型 |
| `void loadingDictCache()` | 加载字典缓存数据 |
| `void clearDictCache()` | 清理字典缓存数据 |
| `void resetDictCache()` | 重置字典缓存数据 |
| `int insertDictType(SysDictType dictType)` | 新增字典类型 |
| `int updateDictType(SysDictType dictType)` | 修改字典类型 |
| `boolean checkDictTypeUnique(SysDictType dictType)` | 校验字典类型是否唯一 |

**实现类**: `SysDictTypeServiceImpl`
- 依赖: SysDictTypeMapper, SysDictDataMapper
- `@PostConstruct init()` -- 启动时加载缓存

---

### 4.8 ISysDictDataService -- 字典数据业务层

| 方法签名 | 说明 |
|---------|------|
| `List<SysDictData> selectDictDataList(SysDictData dictData)` | 查询字典数据列表 |
| `String selectDictLabel(String dictType, String dictValue)` | 异步查询字典标签 |
| `SysDictData selectDictDataById(Long dictCode)` | 通过ID查询字典数据 |
| `void deleteDictDataByIds(Long[] dictCodes)` | 批量删除字典数据 |
| `int insertDictData(SysDictData dictData)` | 新增字典数据 |
| `int updateDictData(SysDictData dictData)` | 修改字典数据 |

**实现类**: `SysDictDataServiceImpl`
- 依赖: SysDictDataMapper

---

### 4.9 ISysNoticeService -- 公告服务层

| 方法签名 | 说明 |
|---------|------|
| `SysNotice selectNoticeById(Long noticeId)` | 通过ID查询公告 |
| `List<SysNotice> selectNoticeList(SysNotice notice)` | 查询公告列表 |
| `int insertNotice(SysNotice notice)` | 新增公告 |
| `int updateNotice(SysNotice notice)` | 修改公告 |
| `int deleteNoticeById(Long noticeId)` | 通过ID删除公告 |
| `int deleteNoticeByIds(Long[] noticeIds)` | 批量删除公告 |

**实现类**: `SysNoticeServiceImpl`
- 依赖: SysNoticeMapper

---

### 4.10 ISysOperLogService -- 操作日志服务层

| 方法签名 | 说明 |
|---------|------|
| `void insertOperlog(SysOperLog operLog)` | 新增操作日志 |
| `List<SysOperLog> selectOperLogList(SysOperLog operLog)` | 查询操作日志列表 |
| `int deleteOperLogByIds(Long[] operIds)` | 批量删除操作日志 |
| `SysOperLog selectOperLogById(Long operId)` | 通过ID查询操作日志 |
| `void cleanOperLog()` | 清空操作日志 |

**实现类**: `SysOperLogServiceImpl`
- 依赖: SysOperLogMapper

---

### 4.11 ISysLogininforService -- 系统访问日志服务层

| 方法签名 | 说明 |
|---------|------|
| `void insertLogininfor(SysLogininfor logininfor)` | 新增登录日志 |
| `List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)` | 查询登录日志列表 |
| `int deleteLogininforByIds(Long[] infoIds)` | 批量删除登录日志 |
| `void cleanLogininfor()` | 清空登录日志 |

**实现类**: `SysLogininforServiceImpl`
- 依赖: SysLogininforMapper

---

### 4.12 ISysUserOnlineService -- 在线用户服务层

| 方法签名 | 说明 |
|---------|------|
| `SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user)` | 通过IP地址查询 |
| `SysUserOnline selectOnlineByUserName(String userName, LoginUser user)` | 通过用户名查询 |
| `SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user)` | 通过IP和用户名查询 |
| `SysUserOnline loginUserToUserOnline(LoginUser user)` | LoginUser 转为 SysUserOnline |

**实现类**: `SysUserOnlineServiceImpl` (无 Mapper 依赖，纯逻辑转换)

---

### 4.13 IUserInfoService (wxmini) -- 小程序用户信息服务层

| 方法签名 | 说明 |
|---------|------|
| `List<UserInfo> selectUserInfoList(UserInfo userInfo)` | 查询用户列表 |
| `int insertUserInfo(UserInfo userInfo)` | 新增用户 |
| `int updateUserInfo(UserInfo userInfo)` | 修改用户 |
| `UserInfo selectUserInfoByOpenId(String openId)` | 通过OpenId查询 |
| `UserInfo selectUserInfoByUserId(String userId)` | 通过平台用户ID查询 |

**实现类**: `UserInfoServiceImpl`
- 依赖: UserInfoMapper, RedisCache
- 额外私有方法: `getWxUserCacheKey(String)`

---

## 5. Mapper XML 文件位置

所有文件位于 `src/main/resources/mapper/` 目录下：

### 5.1 system/ 包 (15 个文件)

| 文件路径 | 对应的 Mapper 接口 |
|----------|-------------------|
| `src/main/resources/mapper/system/SysUserMapper.xml` | com.ruoyi.system.mapper.SysUserMapper |
| `src/main/resources/mapper/system/SysRoleMapper.xml` | com.ruoyi.system.mapper.SysRoleMapper |
| `src/main/resources/mapper/system/SysDeptMapper.xml` | com.ruoyi.system.mapper.SysDeptMapper |
| `src/main/resources/mapper/system/SysMenuMapper.xml` | com.ruoyi.system.mapper.SysMenuMapper |
| `src/main/resources/mapper/system/SysPostMapper.xml` | com.ruoyi.system.mapper.SysPostMapper |
| `src/main/resources/mapper/system/SysConfigMapper.xml` | com.ruoyi.system.mapper.SysConfigMapper |
| `src/main/resources/mapper/system/SysDictTypeMapper.xml` | com.ruoyi.system.mapper.SysDictTypeMapper |
| `src/main/resources/mapper/system/SysDictDataMapper.xml` | com.ruoyi.system.mapper.SysDictDataMapper |
| `src/main/resources/mapper/system/SysNoticeMapper.xml` | com.ruoyi.system.mapper.SysNoticeMapper |
| `src/main/resources/mapper/system/SysOperLogMapper.xml` | com.ruoyi.system.mapper.SysOperLogMapper |
| `src/main/resources/mapper/system/SysLogininforMapper.xml` | com.ruoyi.system.mapper.SysLogininforMapper |
| `src/main/resources/mapper/system/SysUserRoleMapper.xml` | com.ruoyi.system.mapper.SysUserRoleMapper |
| `src/main/resources/mapper/system/SysUserPostMapper.xml` | com.ruoyi.system.mapper.SysUserPostMapper |
| `src/main/resources/mapper/system/SysRoleMenuMapper.xml` | com.ruoyi.system.mapper.SysRoleMenuMapper |
| `src/main/resources/mapper/system/SysRoleDeptMapper.xml` | com.ruoyi.system.mapper.SysRoleDeptMapper |

### 5.2 wxmini/ 包 (1 个文件)

| 文件路径 | 对应的 Mapper 接口 |
|----------|-------------------|
| `src/main/resources/mapper/wxmini/UserInfoMapper.xml` | com.ruoyi.wxmini.mapper.UserInfoMapper |

---

## 6. 模块依赖总结

| 依赖项 | 说明 |
|--------|------|
| **Maven** | `ruoyi-system` 依赖于 `ruoyi-common` (通用工具模块) |
| **数据表映射** | 所有实体均对应后台数据库表，表名见各实体类注释 (如 `sys_user`, `sys_role`, `sys_dept` 等) |
| **缓存机制** | SysConfigServiceImpl 和 SysDictTypeServiceImpl 使用 Redis 做缓存，UserInfoServiceImpl 也使用 Redis 缓存微信用户信息 |
| **数据权限** | SysUserServiceImpl, SysRoleServiceImpl, SysDeptServiceImpl 的方法上标注有 `@DataScope` 注解，通过 AOP 实现数据权限过滤 |
| **事务管理** | 涉及到多表操作的 Service 方法使用 `@Transactional` 注解保证原子性 |

---

## 7. 被其他模块依赖情况

| 模块 | 调用本模块方式 |
|------|---------------|
| `ruoyi-admin` (管理后台 Controller) | 通过 `@Autowired` 注入 Service 接口，如 `ISysUserService`, `ISysRoleService` 等 |
| `ruoyi-wxmini` (小程序后端接口) | 通过 `@Autowired` 注入 wxmini 包下的 Service，如 `IUserInfoService` |

---

**文档生成时间**: 2026
**文档版本**: 1.0
