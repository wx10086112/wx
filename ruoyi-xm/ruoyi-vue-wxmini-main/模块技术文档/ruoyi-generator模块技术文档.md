# ruoyi-generator 模块技术文档

## 模块概述

ruoyi-generator是若依框架的代码生成模块，提供基于数据库表的代码自动生成功能，支持单表、树表、主子表等多种模板。

## 实体类说明

### `com.ruoyi.generator.domain.GenTable` -- 业务表实体

对应数据库表 `gen_table`，用于存储代码生成配置信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| tableId | Long | 编号 |
| tableName | String | 表名称 |
| tableComment | String | 表描述 |
| subTableName | String | 关联父表的表名 |
| subTableFkName | String | 本表关联父表的外键名 |
| className | String | 实体类名称(首字母大写) |
| tplCategory | String | 使用的模板（crud单表操作 tree树表操作 sub主子表操作） |
| tplWebType | String | 前端类型（element-ui模版 element-plus模版） |
| packageName | String | 生成包路径 |
| moduleName | String | 生成模块名 |
| businessName | String | 生成业务名 |
| functionName | String | 生成功能名 |
| functionAuthor | String | 生成作者 |
| genType | String | 生成代码方式（0zip压缩包 1自定义路径） |
| genPath | String | 生成路径（不填默认项目路径） |
| pkColumn | GenTableColumn | 主键信息 |
| subTable | GenTable | 子表信息 |
| columns | List<GenTableColumn> | 表列信息 |
| options | String | 其它生成选项 |
| treeCode | String | 树编码字段 |
| treeParentCode | String | 树父编码字段 |
| treeName | String | 树名称字段 |
| parentMenuId | Long | 上级菜单ID字段 |
| parentMenuName | String | 上级菜单名称字段 |

**继承关系**：
- 继承 `BaseEntity`

**验证注解**：
- `@NotBlank(message = "表名称不能为空")`
- `@NotBlank(message = "表描述不能为空")`
- `@NotBlank(message = "实体类名称不能为空")`
- `@NotBlank(message = "生成包路径不能为空")`
- `@NotBlank(message = "生成模块名不能为空")`
- `@NotBlank(message = "生成业务名不能为空")`
- `@NotBlank(message = "生成功能名不能为空")`
- `@NotBlank(message = "作者不能为空")`

**重要方法**：
- `isSub()` - 判断是否为主子表模板
- `isTree()` - 判断是否为树表模板
- `isCrud()` - 判断是否为单表模板
- `isSuperColumn()` - 判断是否为父类字段

---

### `com.ruoyi.generator.domain.GenTableColumn` -- 业务字段实体

对应数据库表 `gen_table_column`，用于存储表字段信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| columnId | Long | 编号 |
| tableId | Long | 归属表编号 |
| columnName | String | 列名称 |
| columnComment | String | 列描述 |
| columnType | String | 列类型 |
| javaType | String | JAVA类型 |
| javaField | String | JAVA字段名 |
| isPk | String | 是否主键（1是） |
| isIncrement | String | 是否自增（1是） |
| isRequired | String | 是否必填（1是） |
| isInsert | String | 是否为插入字段（1是） |
| isEdit | String | 是否编辑字段（1是） |
| isList | String | 是否列表字段（1是） |
| isQuery | String | 是否查询字段（1是） |
| queryType | String | 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围） |
| htmlType | String | 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件） |
| dictType | String | 字典类型 |
| sort | Integer | 排序 |

**继承关系**：
- 继承 `BaseEntity`

**验证注解**：
- `@NotBlank(message = "Java属性不能为空")`

**重要方法**：
- `isPk()` - 判断是否为主键
- `isIncrement()` - 判断是否自增
- `isRequired()` - 判断是否必填
- `isInsert()` - 判断是否插入字段
- `isEdit()` - 判断是否编辑字段
- `isList()` - 判断是否列表字段
- `isQuery()` - 判断是否查询字段
- `isSuperColumn()` - 判断是否为父类字段
- `isUsableColumn()` - 判断是否可用字段
- `readConverterExp()` - 读取转换表达式

## 控制器类

### `com.ruoyi.generator.controller.GenController` -- 代码生成控制器

**主要接口**：
- `GET /generator/list` - 查询代码生成业务表列表
- `POST /generator/list` - 查询代码生成业务表列表
- `GET /generator/db/list` - 查询数据库表列表
- `GET /generator/importTable` - 导入表结构
- `GET /generator/importTableSave` - 保存导入表结构
- `GET /generator/code/{tableName}` - 生成代码（下载方式）
- `GET /generator/genCode/{tableName}` - 生成代码（自定义路径）
- `GET /generator/download/{tableName}` - 预览生成代码
- `GET /generator/edit/{tableId}` - 修改代码生成配置
- `PUT /generator` - 修改保存代码生成配置
- `DELETE /generator/{tableIds}` - 删除代码生成配置

## 服务类

### `com.ruoyi.generator.service.IGenTableService` -- 代码生成业务表服务接口

**主要方法**：
- `selectGenTableList()` - 查询代码生成业务表列表
- `selectDbTableList()` - 查询数据库表列表
- `selectGenTableById()` - 查询业务表信息
- `selectGenTableAll()` - 查询所有业务表信息
- `importGenTable()` - 导入表结构
- `createGenTable()` - 创建业务表
- `updateGenTable()` - 修改业务表
- `deleteGenTableByIds()` - 删除业务表
- `generatorCode()` - 生成代码
- `previewCode()` - 预览代码
- `updateGenTable()` - 更新代码生成配置
- `synchDb()` - 同步数据库

### `com.ruoyi.generator.service.IGenTableColumnService` -- 代码生成业务字段服务接口

**主要方法**：
- `selectGenTableColumnListByTableId()` - 根据表ID查询业务字段列表
- `selectGenTableColumnById()` - 查询业务字段信息
- `deleteGenTableColumnByTableId()` - 根据表ID删除业务字段
- `batchInsertGenTableColumn()` - 批量新增业务字段

## 实现类

### `com.ruoyi.generator.service.impl.GenTableServiceImpl` -- 代码生成业务表服务实现

**核心功能**：
- 数据库表结构解析
- 代码模板渲染
- 文件生成和压缩
- 代码预览功能

### `com.ruoyi.generator.service.impl.GenTableColumnServiceImpl` -- 代码生成业务字段服务实现

**核心功能**：
- 字段类型映射
- Java字段名生成
- 字段验证规则处理

## 工具类

### `com.ruoyi.generator.util.GenUtils` -- 代码生成工具类

**主要功能**：
- 模板初始化
- 数据库类型到Java类型映射
- 字段名转换
- 模板变量设置

### `com.ruoyi.generator.util.VelocityInitializer` -- Velocity模板引擎初始化

**主要功能**：
- Velocity模板引擎初始化
- 全局属性设置
- 模板加载器配置

### `com.ruoyi.generator.util.VelocityUtils` -- Velocity模板工具类

**主要功能**：
- 模板渲染
- 代码生成
- 文件路径处理

## 模板文件

### 模板位置
`resources/vm/template/` 目录下包含各种代码模板：

- `java/domain.java.vm` - 实体类模板
- `java/mapper.java.vm` - Mapper接口模板
- `java/service.java.vm` - Service接口模板
- `java/serviceImpl.java.vm` - Service实现类模板
- `java/controller.java.vm` - Controller模板
- `xml/mapper.xml.vm` - MyBatis映射文件模板
- `vue/api.js.vm` - 前端API模板
- `vue/index.vue.vm` - 前端列表页面模板
- `vue/add-or-update.vue.vm` - 前端新增/编辑页面模板

## 数据库表结构

### gen_table 表结构

```sql
CREATE TABLE `gen_table` (
  `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) DEFAULT NULL COMMENT '关联父表的表名',
  `sub_table_fk_name` varchar(64) DEFAULT NULL COMMENT '本表关联父表的外键名',
  `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作 sub主子表操作）',
  `tpl_web_type` varchar(30) DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';
```

### gen_table_column 表结构

```sql
CREATE TABLE `gen_table_column` (
  `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';
```

## 配置说明

### 代码生成配置

在 `application.yml` 中可以配置：

```yaml
# 代码生成
gen:
  # 作者
  author: ruoyi
  # 默认包名
  packageName: com.ruoyi.project
  # 模块名
  moduleName: system
  # 自动去除表前缀，默认为false
  autoRemovePre: false
  # 表前缀（生成类名不会包含表前缀）
  tablePrefix: sys_
```

## 使用流程

1. **导入表结构**：从数据库中选择需要生成代码的表
2. **配置生成信息**：设置包名、模块名、作者等信息
3. **字段配置**：配置每个字段的显示类型、查询方式等
4. **生成代码**：选择生成方式（下载或自定义路径）
5. **代码预览**：可以预览生成的代码内容

## 模板类型

### CRUD单表操作
- 适用于普通的单表CRUD操作
- 包含增删改查完整功能
- 支持分页、排序、查询

### Tree树表操作
- 适用于树形结构数据
- 包含树形展示功能
- 支持拖拽排序

### Sub主子表操作
- 适用于主子表关联场景
- 支持主表和子表的联合操作
- 包含子表的增删改查

## 字段类型映射

| 数据库类型 | Java类型 | 默认HTML类型 |
|------------|----------|--------------|
| varchar, char | String | input |
| text | String | textarea |
| int, tinyint | Integer | input |
| bigint | Long | input |
| decimal, float, double | BigDecimal | input |
| datetime, timestamp | Date | datetime |
| date | Date | date |

## 注意事项

1. **表名规范**：建议使用下划线命名法
2. **字段注释**：必须填写字段注释，用于生成表单标签
3. **主键字段**：自动识别主键字段
4. **字典类型**：支持字典类型的下拉框
5. **自定义模板**：可以修改或添加自定义模板
6. **代码覆盖**：生成代码时注意不要覆盖已有代码

## 版本信息

- **模块版本**: 4.6.0
- **模板引擎**: Apache Velocity
- **Java版本**: JDK 1.8+
- **Spring Boot版本**: 2.5.x
- **作者**: ruoyi
