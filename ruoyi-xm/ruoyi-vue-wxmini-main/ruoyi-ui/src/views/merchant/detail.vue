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
  </div>
</template>

<script>
import { getMerchantDetail, getProductList, addProduct, updateProduct, deleteProduct, getMerchantOrders, getMerchantFlowList, orderStatusMap } from '@/api/merchant'

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
      flowQuery: { pageNum: 1, pageSize: 10 }
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
