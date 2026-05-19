<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>商家详情</span>
        <el-button size="small" icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>

      <div v-loading="loading">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="商家名称">{{ merchant.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ merchant.contact }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ merchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(merchant.status)" size="small">{{ statusText(merchant.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入驻时间">{{ merchant.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品数">{{ merchant.products }}</el-descriptions-item>
          <el-descriptions-item label="月销售额">¥{{ (merchant.monthlySales || 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ merchant.id }}</el-descriptions-item>
        </el-descriptions>

        <!-- 标签页 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="商家ID">{{ merchant.id }}</el-descriptions-item>
              <el-descriptions-item label="商家名称">{{ merchant.name }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ merchant.contact }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ merchant.phone }}</el-descriptions-item>
              <el-descriptions-item label="入驻时间">{{ merchant.createTime }}</el-descriptions-item>
              <el-descriptions-item label="佣金比例">{{ merchant.commissionRate }}%</el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
              <h3 style="margin: 0;">小程序配置</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditMiniApp">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="C端 AppID">{{ merchant.cMiniAppId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="C端 Secret">{{ merchant.cMiniAppSecret ? '******' : '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="商家端 AppID">{{ merchant.mMiniAppId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="商家端 Secret">{{ merchant.mMiniAppSecret ? '******' : '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="微信商户号">{{ merchant.wxPayMchId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="支付API密钥">{{ merchant.wxPayApiKey ? '******' : '未配置' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- Tab 2: 商品列表 -->
          <el-tab-pane label="商品列表" name="products">
            <div class="search-bar">
              <el-input v-model="productQuery.name" placeholder="商品名称" size="small" clearable style="width: 200px; margin-right: 10px;" @keyup.enter.native="loadProducts" />
              <el-select v-model="productQuery.status" placeholder="全部状态" size="small" clearable style="width: 120px; margin-right: 10px;">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
              <el-button type="primary" icon="el-icon-search" size="small" @click="loadProducts">搜索</el-button>
              <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddProduct" style="margin-left: auto;">新增商品</el-button>
            </div>

            <el-table v-loading="productLoading" :data="productList" border size="small">
              <el-table-column label="商品名称" prop="name" min-width="180" show-overflow-tooltip />
              <el-table-column label="分类" prop="categoryId" width="80" />
              <el-table-column label="原价" width="90">
                <template slot-scope="scope">¥{{ scope.row.originalPrice.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="现价" width="90">
                <template slot-scope="scope">
                  <span class="text-danger">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="库存" prop="stock" width="70" align="center" />
              <el-table-column label="销量" prop="sales" width="70" align="center" />
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" @click="handleEditProduct(scope.row)">编辑</el-button>
                  <el-button type="text" size="mini" @click="handleToggleStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
                  <el-button type="text" size="mini" class="text-danger" @click="handleDeleteProduct(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="productQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="productQuery.pageSize"
              :total="productTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleProductSizeChange"
              @current-change="handleProductPageChange"
            />
          </el-tab-pane>

          <!-- Tab 3: 订单记录 -->
          <el-tab-pane label="订单记录" name="orders">
            <el-table v-loading="orderLoading" :data="orderList" border size="small">
              <el-table-column label="订单号" prop="orderNo" width="180" />
              <el-table-column label="用户" prop="userId" width="100" />
              <el-table-column label="金额" width="100">
                <template slot-scope="scope">¥{{ scope.row.payAmount.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template slot-scope="scope">
                  <el-tag :type="orderStatusMap[scope.row.status].type" size="small">{{ orderStatusMap[scope.row.status].text }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="下单时间" prop="createTime" width="160" />
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="orderQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="orderQuery.pageSize"
              :total="orderTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleOrderSizeChange"
              @current-change="handleOrderPageChange"
            />
          </el-tab-pane>

          <!-- Tab 4: 流水记录 -->
          <el-tab-pane label="流水记录" name="flow">
            <el-table v-loading="flowLoading" :data="flowList" border size="small">
              <el-table-column label="类型" prop="type" width="100" />
              <el-table-column label="金额" width="120">
                <template slot-scope="scope">
                  <span :class="scope.row.totalAmount >= 0 ? 'text-success' : 'text-danger'">
                    {{ scope.row.totalAmount >= 0 ? '+' : '' }}¥{{ scope.row.totalAmount.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="关联订单" prop="orderNo" width="160" />
              <el-table-column label="时间" prop="createTime" width="160" />
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="flowQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="flowQuery.pageSize"
              :total="flowTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleFlowSizeChange"
              @current-change="handleFlowPageChange"
            />
          </el-tab-pane>

          <!-- Tab 5: 账户管理 -->
          <el-tab-pane label="账户管理" name="accounts">
            <div class="search-bar">
              <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddAccount">新增账户</el-button>
            </div>

            <el-table v-loading="accountLoading" :data="accountList" border size="small">
              <el-table-column label="用户名" prop="username" width="150" />
              <el-table-column label="姓名" prop="realName" width="120" />
              <el-table-column label="手机号" prop="phone" width="130" />
              <el-table-column label="角色" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.role === 'owner' ? 'warning' : 'primary'" size="mini">
                    {{ scope.row.role === 'owner' ? '管理员' : '成员' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '正常' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最后登录" prop="lastLoginTime" width="160" />
              <el-table-column label="操作" width="200" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" @click="handleResetPwd(scope.row)">重置密码</el-button>
                  <el-button type="text" size="mini" @click="handleToggleAccountStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog :title="productDialogTitle" :visible.sync="productDialogVisible" width="600px" append-to-body>
      <el-form ref="productForm" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-input v-model="productForm.categoryId" placeholder="请输入分类" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现价" prop="price">
              <el-input-number v-model="productForm.price" :min="0" :precision="2" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="productForm.stock" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期(天)" prop="validDays">
              <el-input-number v-model="productForm.validDays" :min="1" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片URL" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入图片路径" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="productDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitProductForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 新增账户弹窗 -->
    <el-dialog title="新增账户" :visible.sync="accountDialogVisible" width="500px" append-to-body>
      <el-form ref="accountForm" :model="accountForm" :rules="accountRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="accountForm.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="accountForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="accountForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="accountForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="accountForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="owner" />
            <el-option label="成员" value="member" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="accountDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAccountForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑小程序配置弹窗 -->
    <el-dialog title="编辑小程序配置" :visible.sync="miniAppDialogVisible" width="600px" append-to-body>
      <el-form ref="miniAppForm" :model="miniAppForm" label-width="120px">
        <el-divider content-position="left">C端小程序（用户下单）</el-divider>
        <el-form-item label="C端 AppID">
          <el-input v-model="miniAppForm.cMiniAppId" placeholder="wx开头的AppID" />
        </el-form-item>
        <el-form-item label="C端 Secret">
          <el-input v-model="miniAppForm.cMiniAppSecret" placeholder="请输入Secret" />
        </el-form-item>
        <el-divider content-position="left">商家端小程序（管理/核销）</el-divider>
        <el-form-item label="商家端 AppID">
          <el-input v-model="miniAppForm.mMiniAppId" placeholder="wx开头的AppID" />
        </el-form-item>
        <el-form-item label="商家端 Secret">
          <el-input v-model="miniAppForm.mMiniAppSecret" placeholder="请输入Secret" />
        </el-form-item>
        <el-divider content-position="left">微信支付</el-divider>
        <el-form-item label="商户号">
          <el-input v-model="miniAppForm.wxPayMchId" placeholder="微信支付商户号" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="miniAppForm.wxPayApiKey" placeholder="微信支付API密钥" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="miniAppDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitMiniAppForm">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantDetail, getProductList, addProduct, updateProduct, deleteProduct, getMerchantOrders, getMerchantFlowList, orderStatusMap, getMerchantUserList, addMerchantUser, resetMerchantUserPwd, changeMerchantUserStatus } from '@/api/merchant'

export default {
  name: 'MerchantDetail',
  data() {
    return {
      loading: false,
      merchant: {},
      activeTab: 'products',
      merchantId: null,
      orderStatusMap: orderStatusMap,

      // 商品
      productLoading: false,
      productList: [],
      productTotal: 0,
      productQuery: { name: '', status: '', pageNum: 1, pageSize: 10 },
      productDialogVisible: false,
      productDialogTitle: '新增商品',
      productForm: { id: null, name: '', category: '', originalPrice: 0, price: 0, stock: 0, validDays: 30, image: '', description: '' },
      productRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
        price: [{ required: true, message: '请输入现价', trigger: 'blur' }]
      },

      // 订单
      orderLoading: false,
      orderList: [],
      orderTotal: 0,
      orderQuery: { pageNum: 1, pageSize: 10 },

      // 流水
      flowLoading: false,
      flowList: [],
      flowTotal: 0,
      flowQuery: { pageNum: 1, pageSize: 10 },

      // 账户管理
      accountLoading: false,
      accountList: [],
      accountDialogVisible: false,
      accountForm: { username: '', password: '', realName: '', phone: '', role: 'member' },
      accountRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },

      // 小程序配置
      miniAppDialogVisible: false,
      miniAppForm: { cMiniAppId: '', cMiniAppSecret: '', mMiniAppId: '', mMiniAppSecret: '', wxPayMchId: '', wxPayApiKey: '' }
    }
  },
  created() {
    this.merchantId = Number(this.$route.params.id)
    if (!this.merchantId) {
      this.$message.error('参数错误')
      return
    }
    this.fetchDetail()
    this.loadProducts()
  },
  methods: {
    async fetchDetail() {
      this.loading = true
      try {
        const res = await getMerchantDetail(this.merchantId)
        this.merchant = res.data
      } catch (e) {
        this.$message.error('获取商家详情失败')
      } finally {
        this.loading = false
      }
    },

    // ========== 商品管理 ==========
    async loadProducts() {
      this.productLoading = true
      try {
        const res = await getProductList({ merchantId: this.merchantId, ...this.productQuery })
        this.productList = res.rows
        this.productTotal = res.total
      } finally {
        this.productLoading = false
      }
    },
    handleProductSizeChange(val) {
      this.productQuery.pageSize = val
      this.loadProducts()
    },
    handleProductPageChange(val) {
      this.productQuery.pageNum = val
      this.loadProducts()
    },
    handleAddProduct() {
      this.productDialogTitle = '新增商品'
      this.productForm = { id: null, name: '', category: '', originalPrice: 0, price: 0, stock: 0, validDays: 30, image: '', description: '' }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    handleEditProduct(row) {
      this.productDialogTitle = '编辑商品'
      this.productForm = { ...row }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    submitProductForm() {
      this.$refs.productForm.validate(async valid => {
        if (!valid) return
        this.productForm.merchantId = this.merchantId
        if (this.productForm.id) {
          await updateProduct(this.productForm)
          this.$message.success('修改成功')
        } else {
          this.productForm.status = 1
          await addProduct(this.productForm)
          this.$message.success('新增成功')
        }
        this.productDialogVisible = false
        this.loadProducts()
      })
    },
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm(`确认${text}该商品？`, '提示', { type: 'warning' }).then(async () => {
        await updateProduct({ id: row.id, status: newStatus })
        this.$message.success(`${text}成功`)
        this.loadProducts()
      }).catch(() => {})
    },
    handleDeleteProduct(row) {
      this.$confirm('确认删除该商品？', '提示', { type: 'warning' }).then(async () => {
        await deleteProduct(row.id)
        this.$message.success('删除成功')
        this.loadProducts()
      }).catch(() => {})
    },

    // ========== 订单记录 ==========
    async loadOrders() {
      this.orderLoading = true
      try {
        const res = await getMerchantOrders(this.merchantId, this.orderQuery)
        this.orderList = res.rows
        this.orderTotal = res.total
      } finally {
        this.orderLoading = false
      }
    },
    handleOrderSizeChange(val) {
      this.orderQuery.pageSize = val
      this.loadOrders()
    },
    handleOrderPageChange(val) {
      this.orderQuery.pageNum = val
      this.loadOrders()
    },

    // ========== 流水记录 ==========
    async loadFlow() {
      this.flowLoading = true
      try {
        const res = await getMerchantFlowList({ merchantId: this.merchantId, ...this.flowQuery })
        this.flowList = res.rows
        this.flowTotal = res.total
      } finally {
        this.flowLoading = false
      }
    },
    handleFlowSizeChange(val) {
      this.flowQuery.pageSize = val
      this.loadFlow()
    },
    handleFlowPageChange(val) {
      this.flowQuery.pageNum = val
      this.loadFlow()
    },

    // ========== 账户管理 ==========
    async loadAccounts() {
      this.accountLoading = true
      try {
        const res = await getMerchantUserList({ merchantId: this.merchantId, pageNum: 1, pageSize: 100 })
        this.accountList = res.rows || []
      } finally {
        this.accountLoading = false
      }
    },
    handleAddAccount() {
      this.accountForm = { username: '', password: '', realName: '', phone: '', role: 'member' }
      this.accountDialogVisible = true
      this.$nextTick(() => { this.$refs.accountForm && this.$refs.accountForm.clearValidate() })
    },
    submitAccountForm() {
      this.$refs.accountForm.validate(async valid => {
        if (!valid) return
        await addMerchantUser({ ...this.accountForm, merchantId: this.merchantId, status: 1 })
        this.$message.success('新增成功')
        this.accountDialogVisible = false
        this.loadAccounts()
      })
    },
    handleResetPwd(row) {
      this.$confirm(`确认将 ${row.username} 的密码重置为 123456？`, '提示', { type: 'warning' }).then(async () => {
        await resetMerchantUserPwd(row.id, '123456')
        this.$message.success('密码已重置为 123456')
      }).catch(() => {})
    },
    handleToggleAccountStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确认${text}账号 ${row.username}？`, '提示', { type: 'warning' }).then(async () => {
        await changeMerchantUserStatus(row.id, newStatus)
        this.$message.success(`${text}成功`)
        this.loadAccounts()
      }).catch(() => {})
    },

    // ========== 小程序配置 ==========
    handleEditMiniApp() {
      this.miniAppForm = {
        id: this.merchant.id,
        cMiniAppId: this.merchant.cMiniAppId || '',
        cMiniAppSecret: this.merchant.cMiniAppSecret || '',
        mMiniAppId: this.merchant.mMiniAppId || '',
        mMiniAppSecret: this.merchant.mMiniAppSecret || '',
        wxPayMchId: this.merchant.wxPayMchId || '',
        wxPayApiKey: this.merchant.wxPayApiKey || ''
      }
      this.miniAppDialogVisible = true
    },
    async submitMiniAppForm() {
      await updateMerchant(this.miniAppForm)
      this.$message.success('保存成功')
      this.miniAppDialogVisible = false
      this.fetchDetail()
    },

    handleBack() {
      this.$router.push({ path: '/merchant/list' })
    },
    statusText(status) {
      const map = { 0: '禁用', 1: '正常', 2: '待审核' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning' }
      return map[status] || 'info'
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'orders' && this.orderList.length === 0) {
        this.loadOrders()
      } else if (val === 'flow' && this.flowList.length === 0) {
        this.loadFlow()
      } else if (val === 'accounts' && this.accountList.length === 0) {
        this.loadAccounts()
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-tabs {
  margin-top: 20px;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.pagination {
  margin-top: 12px;
  text-align: right;
}
.text-danger {
  color: #F56C6C;
}
.text-success {
  color: #67C23A;
}
</style>
