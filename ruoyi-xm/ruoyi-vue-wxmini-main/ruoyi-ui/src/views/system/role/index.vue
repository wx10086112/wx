<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="角色名称" prop="roleName">
        <el-input
          v-model="queryParams.roleName"
          placeholder="请输入角色名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="权限字符" prop="roleKey">
        <el-input
          v-model="queryParams.roleKey"
          placeholder="请输入权限字符"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="角色状态"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:role:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:role:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:role:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:role:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="角色编号" prop="roleId" width="120" />
      <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="权限字符" prop="roleKey" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="角色归属" prop="roleScope" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.roleScope === 'PLATFORM'" type="primary" size="small">平台</el-tag>
          <el-tag v-else-if="scope.row.roleScope === 'DISTRIBUTOR'" type="warning" size="small">分销商</el-tag>
          <el-tag v-else-if="scope.row.roleScope === 'MERCHANT'" type="success" size="small">商家</el-tag>
          <el-tag v-else type="info" size="small">平台</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="显示顺序" prop="roleSort" width="100" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope" v-if="scope.row.roleId !== 1">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:role:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:role:remove']"
          >删除</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:role:edit']">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleDataScope" icon="el-icon-circle-check"
                v-hasPermi="['system:role:edit']">数据范围</el-dropdown-item>
              <el-dropdown-item command="handleAuthUser" icon="el-icon-user"
                v-hasPermi="['system:role:edit']">分配后台账号</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-alert
        title="角色归属决定可分配的数据范围和菜单权限，请先选择角色归属。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 15px"
      />
      <el-row :gutter="20">
        <!-- 左侧：角色基础信息 -->
        <el-col :span="10">
          <el-form ref="form" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
            <el-form-item prop="roleKey">
              <span slot="label">
                <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)" placement="top">
                  <i class="el-icon-question"></i>
                </el-tooltip>
                权限字符
              </span>
              <el-input v-model="form.roleKey" placeholder="请输入权限字符" />
            </el-form-item>
            <el-form-item label="角色顺序" prop="roleSort">
              <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="角色归属">
              <el-radio-group v-model="form.roleScope" @change="handleRoleScopeChange">
                <el-radio label="PLATFORM">平台</el-radio>
                <el-radio label="DISTRIBUTOR">分销商</el-radio>
                <el-radio label="MERCHANT">商家</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="所属分销商" v-if="form.roleScope === 'DISTRIBUTOR' || form.roleScope === 'MERCHANT'" prop="distributorId">
              <el-select v-model="form.distributorId" placeholder="请选择所属分销商" filterable style="width: 100%">
                <el-option v-for="item in distributorOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属商家" v-if="form.roleScope === 'MERCHANT'" prop="merchantId">
              <el-select v-model="form.merchantId" placeholder="请选择所属商家" filterable style="width: 100%">
                <el-option v-for="item in filteredMerchantOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-form>
        </el-col>
        <!-- 右侧：菜单权限 -->
        <el-col :span="14">
          <div style="margin-bottom: 8px; font-weight: 500; color: #606266;">菜单权限</div>
          <div style="margin-bottom: 8px;">
            <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
            <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</el-checkbox>
            <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">父子联动</el-checkbox>
          </div>
          <el-tree
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            ref="menu"
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            empty-text="加载中，请稍候"
            :props="defaultProps"
            :default-expand-all="menuExpand"
            style="max-height: 420px; overflow-y: auto;"
          ></el-tree>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 分配数据范围对话框 -->
    <el-dialog :title="title" :visible.sync="openDataScope" width="600px" append-to-body>
      <el-alert
        :title="dataScopeAlertText"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 15px"
      />
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" :disabled="true" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="form.roleKey" :disabled="true" />
        </el-form-item>
        <el-form-item label="角色归属">
          <el-tag v-if="form.roleScope === 'PLATFORM'" type="primary">平台</el-tag>
          <el-tag v-else-if="form.roleScope === 'DISTRIBUTOR'" type="warning">分销商</el-tag>
          <el-tag v-else-if="form.roleScope === 'MERCHANT'" type="success">商家</el-tag>
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScopeType" @change="handleDataScopeTypeChange" style="width: 100%">
            <el-option
              v-for="item in currentDataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <!-- 分销商角色选择指定商家 -->
        <el-form-item label="指定商家" v-if="form.dataScopeType === 'DISTRIBUTOR_CUSTOM'" prop="deptIds">
          <el-select v-model="form.deptIds" multiple placeholder="请选择可管理的商家" filterable style="width: 100%">
            <el-option v-for="item in filteredMerchantOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <!-- 平台角色自定义部门权限 -->
        <el-form-item label="部门权限" v-show="form.dataScope === '2'" prop="deptIds">
          <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</el-checkbox>
          <el-tree
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            ref="dept"
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            empty-text="加载中，请稍候"
            :props="defaultProps"
          ></el-tree>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDataScope">确 定</el-button>
        <el-button @click="cancelDataScope">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRole, getRole, delRole, addRole, updateRole, dataScope, changeRoleStatus, deptTreeSelect, listDistributorOptions, listMerchantOptions } from "@/api/system/role"
import { treeselect as menuTreeselect, roleMenuTreeselect } from "@/api/system/menu"

// 菜单归属分类（根据菜单ID前缀或ID范围判断）
// 需要根据实际菜单ID配置，这里给出默认映射
const PLATFORM_MENU_KEYWORDS = ['distributor', 'merchant', 'platform', 'system:config', 'system:dept', 'system:menu', 'system:notice']
const DISTRIBUTOR_MENU_KEYWORDS = ['distributor:merchant', 'distributor:order', 'distributor:settlement', 'distributor:analysis']

export default {
  name: "Role",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 角色表格数据
      roleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示弹出层（数据权限）
      openDataScope: false,
      menuExpand: false,
      menuNodeAll: false,
      deptExpand: true,
      deptNodeAll: false,
      // 日期范围
      dateRange: [],
      // 业务数据范围选项（动态）
      bizDataScopeOptions: {
        PLATFORM: [
          { value: "ALL", label: "全平台数据" },
          { value: "DISTRIBUTOR_CUSTOM", label: "指定分销商" },
          { value: "MERCHANT_CUSTOM", label: "指定商家" }
        ],
        DISTRIBUTOR: [
          { value: "DISTRIBUTOR_SELF", label: "当前分销商及名下商家" },
          { value: "MERCHANT_CUSTOM", label: "指定商家" }
        ],
        MERCHANT: [
          { value: "MERCHANT_SELF", label: "当前商家" }
        ]
      },
      // 菜单列表
      menuOptions: [],
      // 部门列表
      deptOptions: [],
      // 分销商选项
      distributorOptions: [],
      // 商家选项
      merchantOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleName: undefined,
        roleKey: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 表单校验
      rules: {
        roleName: [
          { required: true, message: "角色名称不能为空", trigger: "blur" }
        ],
        roleKey: [
          { required: true, message: "权限字符不能为空", trigger: "blur" }
        ],
        roleSort: [
          { required: true, message: "角色顺序不能为空", trigger: "blur" }
        ]
      }
    }
  },
  computed: {
    // 当前角色归属可选的数据范围选项
    currentDataScopeOptions() {
      const scope = this.form.roleScope || 'PLATFORM'
      return this.bizDataScopeOptions[scope] || this.bizDataScopeOptions.PLATFORM
    },
    // 根据分销商过滤商家列表
    filteredMerchantOptions() {
      if (this.form.roleScope === 'DISTRIBUTOR' && this.form.distributorId) {
        return this.merchantOptions.filter(m => m.distributorId === this.form.distributorId)
      }
      return this.merchantOptions
    },
    // 数据范围弹窗提示文案
    dataScopeAlertText() {
      const scope = this.form.roleScope
      if (scope === 'DISTRIBUTOR') {
        return '分销商角色的数据范围默认为当前分销商及名下商家，可指定具体商家。'
      } else if (scope === 'MERCHANT') {
        return '商家角色的数据范围固定为当前商家，不可修改。'
      }
      return '平台角色可选择全平台数据、指定分销商或指定商家作为数据范围。'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询角色列表 */
    getList() {
      this.loading = true
      listRole(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.roleList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    /** 查询菜单树结构 */
    getMenuTreeselect() {
      menuTreeselect().then(response => {
        this.menuOptions = response.data
        this.filterMenuByRoleScope(this.form.roleScope)
      })
    },
    /** 加载分销商/商家选项 */
    loadBizOptions() {
      listDistributorOptions().then(response => {
        this.distributorOptions = response.data || []
      })
      listMerchantOptions().then(response => {
        this.merchantOptions = response.data || []
      })
    },
    /** 角色归属变更 */
    handleRoleScopeChange(value) {
      this.form.distributorId = undefined
      this.form.merchantId = undefined
      // 设置默认数据范围
      if (value === 'PLATFORM') {
        this.form.dataScopeType = 'ALL'
      } else if (value === 'DISTRIBUTOR') {
        this.form.dataScopeType = 'DISTRIBUTOR_SELF'
      } else if (value === 'MERCHANT') {
        this.form.dataScopeType = 'MERCHANT_SELF'
      }
      // 过滤菜单树
      this.filterMenuByRoleScope(value)
    },
    /** 根据角色归属过滤菜单树 */
    filterMenuByRoleScope(roleScope) {
      if (!this.$refs.menu || !this.menuOptions.length) return
      this.$nextTick(() => {
        this.setMenuDisabled(this.menuOptions, roleScope)
      })
    },
    /** 递归设置菜单节点禁用状态 */
    setMenuDisabled(menuList, roleScope) {
      if (!menuList || !menuList.length) return
      menuList.forEach(menu => {
        const node = this.$refs.menu.getNode(menu.id)
        if (!node) return
        // 平台角色：不禁用任何菜单
        if (roleScope === 'PLATFORM') {
          node.disabled = false
        }
        // 分销商角色：禁用平台专属菜单
        else if (roleScope === 'DISTRIBUTOR') {
          if (this.isPlatformOnlyMenu(menu)) {
            node.disabled = true
          } else {
            node.disabled = false
          }
        }
        // 商家角色：只允许商家菜单
        else if (roleScope === 'MERCHANT') {
          if (!this.isMerchantMenu(menu)) {
            node.disabled = true
          } else {
            node.disabled = false
          }
        }
        // 递归处理子菜单
        if (menu.children && menu.children.length) {
          this.setMenuDisabled(menu.children, roleScope)
        }
      })
    },
    /** 判断是否为平台专属菜单 */
    isPlatformOnlyMenu(menu) {
      const label = (menu.label || '').toLowerCase()
      const perms = (menu.perms || '').toLowerCase()
      // 系统管理类菜单都是平台专属
      const platformKeywords = ['系统', 'system', '平台', 'platform', '分销商管理', 'distributor:list']
      return platformKeywords.some(kw => label.includes(kw) || perms.includes(kw))
    },
    /** 判断是否为商家可用菜单 */
    isMerchantMenu(menu) {
      const label = (menu.label || '').toLowerCase()
      const perms = (menu.perms || '').toLowerCase()
      // 商家可用：商品、团购、订单、核销、门店、结算
      const merchantKeywords = ['商品', 'product', '团购', 'groupbuy', '订单', 'order', '核销', 'verify', '门店', 'store', '结算', 'settlement']
      return merchantKeywords.some(kw => label.includes(kw) || perms.includes(kw))
    },
    /** 数据范围类型变更 */
    handleDataScopeTypeChange(value) {
      if (value !== 'DISTRIBUTOR_CUSTOM') {
        this.form.deptIds = []
        if (this.$refs.dept) {
          this.$refs.dept.setCheckedKeys([])
        }
      }
    },
    // 所有菜单节点数据
    getMenuAllCheckedKeys() {
      // 目前被选中的菜单节点
      let checkedKeys = this.$refs.menu.getCheckedKeys()
      // 半选中的菜单节点
      let halfCheckedKeys = this.$refs.menu.getHalfCheckedKeys()
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
      return checkedKeys
    },
    // 所有部门节点数据
    getDeptAllCheckedKeys() {
      // 目前被选中的部门节点
      let checkedKeys = this.$refs.dept.getCheckedKeys()
      // 半选中的部门节点
      let halfCheckedKeys = this.$refs.dept.getHalfCheckedKeys()
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
      return checkedKeys
    },
    /** 根据角色ID查询菜单树结构 */
    getRoleMenuTreeselect(roleId) {
      return roleMenuTreeselect(roleId).then(response => {
        this.menuOptions = response.menus
        return response
      })
    },
    /** 根据角色ID查询部门树结构 */
    getDeptTree(roleId) {
      return deptTreeSelect(roleId).then(response => {
        this.deptOptions = response.depts
        return response
      })
    },
    // 角色状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗？').then(function() {
        return changeRoleStatus(row.roleId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 取消按钮（数据权限）
    cancelDataScope() {
      this.openDataScope = false
      this.reset()
    },
    // 表单重置
    reset() {
      if (this.$refs.menu != undefined) {
        this.$refs.menu.setCheckedKeys([])
      }
      this.menuExpand = false,
      this.menuNodeAll = false,
      this.deptExpand = true,
      this.deptNodeAll = false,
      this.form = {
        roleId: undefined,
        roleName: undefined,
        roleKey: undefined,
        roleSort: 0,
        status: "0",
        roleScope: "PLATFORM",
        dataScopeType: "ALL",
        distributorId: undefined,
        merchantId: undefined,
        menuIds: [],
        deptIds: [],
        menuCheckStrictly: true,
        deptCheckStrictly: true,
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.roleId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleDataScope":
          this.handleDataScope(row)
          break
        case "handleAuthUser":
          this.handleAuthUser(row)
          break
        default:
          break
      }
    },
    // 树权限（展开/折叠）
    handleCheckedTreeExpand(value, type) {
      if (type == 'menu') {
        let treeList = this.menuOptions
        for (let i = 0; i < treeList.length; i++) {
          this.$refs.menu.store.nodesMap[treeList[i].id].expanded = value
        }
      } else if (type == 'dept') {
        let treeList = this.deptOptions
        for (let i = 0; i < treeList.length; i++) {
          this.$refs.dept.store.nodesMap[treeList[i].id].expanded = value
        }
      }
    },
    // 树权限（全选/全不选）
    handleCheckedTreeNodeAll(value, type) {
      if (type == 'menu') {
        this.$refs.menu.setCheckedNodes(value ? this.menuOptions: [])
      } else if (type == 'dept') {
        this.$refs.dept.setCheckedNodes(value ? this.deptOptions: [])
      }
    },
    // 树权限（父子联动）
    handleCheckedTreeConnect(value, type) {
      if (type == 'menu') {
        this.form.menuCheckStrictly = value ? true: false
      } else if (type == 'dept') {
        this.form.deptCheckStrictly = value ? true: false
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getMenuTreeselect()
      this.loadBizOptions()
      this.open = true
      this.title = "添加角色"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      this.loadBizOptions()
      const roleId = row.roleId || this.ids
      const roleMenu = this.getRoleMenuTreeselect(roleId)
      getRole(roleId).then(response => {
        this.form = response.data
        // 兼容旧数据：如果没有 dataScopeType，根据 dataScope 推断
        if (!this.form.dataScopeType) {
          if (this.form.dataScope === '1') this.form.dataScopeType = 'ALL'
          else if (this.form.dataScope === '2') this.form.dataScopeType = 'DISTRIBUTOR_CUSTOM'
          else this.form.dataScopeType = 'ALL'
        }
        this.open = true
        this.$nextTick(() => {
          roleMenu.then(res => {
            let checkedKeys = res.checkedKeys
            checkedKeys.forEach((v) => {
                this.$nextTick(()=>{
                    this.$refs.menu.setChecked(v, true ,false)
                })
            })
            // 菜单加载完成后按角色归属过滤
            this.filterMenuByRoleScope(this.form.roleScope)
          })
        })
      })
      this.title = "修改角色"
    },
    /** 选择角色权限范围触发 */
    dataScopeSelectChange(value) {
      if(value !== '2') {
        this.$refs.dept.setCheckedKeys([])
      }
    },
    /** 分配数据范围操作 */
    handleDataScope(row) {
      this.reset()
      const deptTreeSelect = this.getDeptTree(row.roleId)
      getRole(row.roleId).then(response => {
        this.form = response.data
        // 兼容旧数据
        if (!this.form.dataScopeType) {
          if (this.form.dataScope === '1') this.form.dataScopeType = 'ALL'
          else if (this.form.dataScope === '5') this.form.dataScopeType = 'MERCHANT_SELF'
          else this.form.dataScopeType = 'ALL'
        }
        // 加载分销商下的商家
        if (this.form.roleScope === 'DISTRIBUTOR') {
          this.loadBizOptions()
        }
        this.openDataScope = true
        this.$nextTick(() => {
          deptTreeSelect.then(res => {
            if (this.$refs.dept) {
              this.$refs.dept.setCheckedKeys(res.checkedKeys)
            }
          })
        })
      })
      this.title = "分配数据范围"
    },
    /** 分配用户操作（改为分配后台账号） */
    handleAuthUser: function(row) {
      const roleId = row.roleId
      this.$router.push("/system/role-auth/user/" + roleId)
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.roleId != undefined) {
            this.form.menuIds = this.getMenuAllCheckedKeys()
            updateRole(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            this.form.menuIds = this.getMenuAllCheckedKeys()
            addRole(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 提交按钮（数据权限） */
    submitDataScope: function() {
      if (this.form.roleId != undefined) {
        // 如果是自定义部门权限，获取部门选中项
        if (this.form.dataScope === '2') {
          this.form.deptIds = this.getDeptAllCheckedKeys()
        }
        dataScope(this.form).then(response => {
          this.$modal.msgSuccess("修改成功")
          this.openDataScope = false
          this.getList()
        })
      }
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const roleIds = row.roleId || this.ids
      this.$modal.confirm('是否确认删除角色编号为"' + roleIds + '"的数据项？').then(function() {
        return delRole(roleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/role/export', {
        ...this.queryParams
      }, `role_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
