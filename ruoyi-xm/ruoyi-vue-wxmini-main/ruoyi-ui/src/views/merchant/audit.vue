<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>入驻审核</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.name" placeholder="请输入商家名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="商家名称" prop="name" min-width="140" />
        <el-table-column label="联系人" prop="contact" width="100" />
        <el-table-column label="联系电话" prop="phone" width="140" />
        <el-table-column label="申请等级" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.level }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="applyTime" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拒绝原因" prop="reason" min-width="160" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.reason || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.status === 0">
              <el-button type="text" size="small" class="text-success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="text" size="small" class="text-danger" @click="handleReject(scope.row)">拒绝</el-button>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 拒绝原因弹窗 -->
    <el-dialog title="拒绝原因" :visible.sync="rejectDialogVisible" width="400px" append-to-body>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <div slot="footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReject">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAuditList, auditMerchant } from '@/api/merchant'

export default {
  name: 'MerchantAudit',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        name: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      rejectDialogVisible: false,
      rejectReason: '',
      rejectRow: null
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getAuditList(this.queryParams)
        this.tableList = res.data.list
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    resetQuery() {
      this.queryParams = { name: '', status: '', pageNum: 1, pageSize: 10 }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.fetchData()
    },
    handleApprove(row) {
      this.$confirm(`确认通过「${row.name}」的入驻申请？`, '审核确认', { type: 'success' }).then(async () => {
        await auditMerchant(row.merchantId, 1)
        row.status = 1
        this.$message.success('审核通过')
      }).catch(() => {})
    },
    handleReject(row) {
      this.rejectRow = row
      this.rejectReason = ''
      this.rejectDialogVisible = true
    },
    async submitReject() {
      if (!this.rejectReason.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      await auditMerchant(this.rejectRow.merchantId, 2)
      this.rejectRow.status = 2
      this.rejectRow.reason = this.rejectReason
      this.rejectDialogVisible = false
      this.$message.success('已拒绝')
    },
    statusText(status) {
      const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'danger' }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
.text-success {
  color: #67C23A;
}
.text-danger {
  color: #F56C6C;
}
</style>
