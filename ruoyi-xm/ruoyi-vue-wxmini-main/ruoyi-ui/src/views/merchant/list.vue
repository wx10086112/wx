<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>商家列表</span>
        <el-button v-hasPermi="['mall:merchant:add']" style="float: right; padding: 3px 0;" type="text" icon="el-icon-plus" @click="handleAdd">添加商户</el-button>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.name" placeholder="请输入商家ID" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="停止合作" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="商家名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="distributorName" label="所属分销商" width="120" align="center" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ scope.row.distributorName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="90" align="center" />
        <el-table-column prop="phone" label="联系电话" width="130" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" label="商品数" width="80" align="center" />
        <el-table-column prop="totalIncome" label="总营收" width="110" align="center">
          <template slot-scope="scope">
            <span>¥{{ (scope.row.totalIncome || 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="110" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-hasPermi="['mall:merchant:audit']" v-if="scope.row.status === 2" type="text" size="small" icon="el-icon-s-check" class="audit-btn" @click="handleAudit(scope.row)">审核</el-button>
            <el-button v-if="isPlatform" v-hasPermi="['mall:merchant:edit']" type="text" size="small" icon="el-icon-connection" @click="handleAssign(scope.row)">分配</el-button>
            <el-button v-if="scope.row.status === 1" v-hasPermi="['mall:merchant:edit']" type="text" size="small" icon="el-icon-close" class="text-warning" @click="handleStop(scope.row)">停止合作</el-button>
            <el-button v-if="scope.row.status === 3" v-hasPermi="['mall:merchant:edit']" type="text" size="small" icon="el-icon-check" class="text-success" @click="handleResume(scope.row)">恢复合作</el-button>
            <el-button v-if="isPlatform" v-hasPermi="['mall:merchant:remove']" type="text" size="small" icon="el-icon-delete" class="text-danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog title="商家审核" :visible.sync="auditDialogVisible" width="500px" :close-on-click-modal="false">
      <div class="audit-info">
        <p><strong>商家名称：</strong>{{ auditRow.name }}</p>
        <p><strong>联系人：</strong>{{ auditRow.contact }}</p>
        <p><strong>联系电话：</strong>{{ auditRow.phone }}</p>
      </div>
      <el-divider />
      <div v-if="auditAction === 'reject'" class="reject-reason">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </div>
      <div v-if="!auditAction" class="audit-actions">
        <el-button type="success" icon="el-icon-check" @click="auditAction = 'approve'">通过</el-button>
        <el-button type="danger" icon="el-icon-close" @click="auditAction = 'reject'">拒绝</el-button>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelAudit">取 消</el-button>
        <el-button v-if="auditAction === 'approve'" type="success" :loading="auditLoading" @click="submitAudit(1)">确认通过</el-button>
        <el-button v-if="auditAction === 'reject'" type="danger" :loading="auditLoading" @click="submitAudit(0)">确认拒绝</el-button>
      </span>
    </el-dialog>

    <!-- 分配分销商弹窗 -->
    <el-dialog title="分配分销商" :visible.sync="assignDialogVisible" width="450px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="当前商家">
          <span>{{ assignRow.name }}</span>
        </el-form-item>
        <el-form-item label="当前归属">
          <span>{{ assignRow.distributorName || '无（平台直属）' }}</span>
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="assignDistributorId" placeholder="请选择分销商" clearable filterable style="width: 100%;">
            <el-option label="无（平台直属）" :value="null" />
            <el-option v-for="d in distributorOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="submitAssign">确认分配</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantList, auditMerchant, deleteMerchant, updateMerchant, stopMerchant, resumeMerchant } from '@/api/merchant'
import { listDistributor } from '@/api/distributor'

export default {
  name: 'MerchantList',
  data() {
    return {
      loading: false,
      tableList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      queryParams: {
        name: '',
        status: undefined
      },
      auditDialogVisible: false,
      auditRow: {},
      auditAction: '',
      rejectReason: '',
      auditLoading: false,
      assignDialogVisible: false,
      assignRow: {},
      assignDistributorId: null,
      assignLoading: false,
      distributorOptions: []
    }
  },
  computed: {
    isPlatform() {
      return this.$store.state.user.accountType !== 'DISTRIBUTOR'
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getMerchantList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.queryParams.name || undefined,
          status: this.queryParams.status !== '' && this.queryParams.status !== undefined ? this.queryParams.status : undefined
        })
        this.tableList = res.rows
        this.total = res.total
      } catch (e) {
        this.$message.error('获取商家列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = { name: '', status: undefined }
      this.pageNum = 1
      this.fetchData()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.fetchData()
    },
    handleAdd() {
      this.$router.push({ path: '/merchant/add' })
    },
    handleDetail(row) {
      this.$router.push({ path: `/merchant/detail/${row.id}` })
    },
    handleAudit(row) {
      this.auditRow = { ...row }
      this.auditAction = ''
      this.rejectReason = ''
      this.auditDialogVisible = true
    },
    cancelAudit() {
      this.auditDialogVisible = false
      this.auditAction = ''
      this.rejectReason = ''
    },
    async submitAudit(status) {
      if (status === 0 && !this.rejectReason.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      this.auditLoading = true
      try {
        await auditMerchant(this.auditRow.id, status)
        this.$message.success(status === 1 ? '审核通过' : '已拒绝')
        this.auditDialogVisible = false
        this.auditAction = ''
        this.rejectReason = ''
        this.fetchData()
      } catch (e) {
        this.$message.error('操作失败')
      } finally {
        this.auditLoading = false
      }
    },
    statusText(status) {
      const map = { 0: '禁用', 1: '正常', 2: '待审核', 3: '停止合作' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
      return map[status] || 'info'
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除商家「' + row.name + '」？删除后会同步删除该商家的商家用户账号，商家本身将被逻辑删除。').then(async () => {
        await deleteMerchant(row.id)
        this.$modal.msgSuccess('删除成功')
        this.fetchData()
      }).catch(() => {})
    },
    async loadDistributors() {
      try {
        const res = await listDistributor({ pageSize: 500 })
        this.distributorOptions = res.rows || []
      } catch (e) {
        this.distributorOptions = []
      }
    },
    handleAssign(row) {
      this.assignRow = { ...row }
      this.assignDistributorId = row.distributorId || null
      this.loadDistributors()
      this.assignDialogVisible = true
    },
    async submitAssign() {
      this.assignLoading = true
      try {
        await updateMerchant({ id: this.assignRow.id, distributorId: this.assignDistributorId })
        this.$modal.msgSuccess('分配成功')
        this.assignDialogVisible = false
        this.fetchData()
      } catch (e) {
        this.$modal.msgError('分配失败')
      } finally {
        this.assignLoading = false
      }
    },
    handleStop(row) {
      this.$modal.confirm('确认停止与商家「' + row.name + '」的合作？商户资料、订单、结算记录将保留，但商家端和C端将不可继续运营。').then(async () => {
        await stopMerchant(row.id)
        this.$modal.msgSuccess('已停止合作')
        this.fetchData()
      }).catch(() => {})
    },
    handleResume(row) {
      this.$modal.confirm('确认恢复与商家「' + row.name + '」的合作？').then(async () => {
        await resumeMerchant(row.id)
        this.$modal.msgSuccess('已恢复合作')
        this.fetchData()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}
.pagination {
  margin-top: 15px;
  text-align: right;
}
.audit-info p {
  margin: 8px 0;
  color: #606266;
}
.audit-actions {
  text-align: center;
  padding: 10px 0;
}
.audit-btn {
  color: #e6a23c;
}
.reject-reason {
  margin-top: 10px;
}
.text-danger {
  color: #F56C6C;
}
.text-warning {
  color: #E6A23C;
}
.text-success {
  color: #67C23A;
}
</style>
