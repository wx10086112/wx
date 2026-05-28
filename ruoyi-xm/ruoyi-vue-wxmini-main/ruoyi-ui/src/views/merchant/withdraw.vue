<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>提现管理</span></div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableList" border style="width: 100%">
        <el-table-column prop="merchantName" label="商家名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="amount" label="提现金额" width="120" align="center">
          <template slot-scope="scope">
            <span class="withdraw-amount">¥{{ Number(scope.row.amount).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bankName" label="银行名称" width="120" align="center" />
        <el-table-column prop="bankAccount" label="银行账号" width="120" align="center" />
        <el-table-column prop="createTime" label="申请时间" width="160" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['mall:finance:edit']"
              v-if="scope.row.status === 0"
              type="text"
              size="small"
              icon="el-icon-check"
              class="pass-btn"
              @click="handlePass(scope.row)"
            >通过</el-button>
            <el-button
              v-hasPermi="['mall:finance:edit']"
              v-if="scope.row.status === 0"
              type="text"
              size="small"
              icon="el-icon-close"
              class="reject-btn"
              @click="handleReject(scope.row)"
            >拒绝</el-button>
            <el-button type="text" size="small" icon="el-icon-view" @click="handleViewDetail(scope.row)">查看详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog title="提现详情" :visible.sync="detailDialogVisible" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商家名称">{{ detailRow.merchantId }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">¥{{ Number(detailRow.amount || 0).toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="银行名称">{{ detailRow.bankName }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ detailRow.bankAccount }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detailRow.status) }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.auditTime" label="审核时间">{{ detailRow.auditTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.payTime" label="完成时间">{{ detailRow.payTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.rejectReason" label="拒绝原因" :span="2">{{ detailRow.rejectReason }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 拒绝弹窗 -->
    <el-dialog title="拒绝提现" :visible.sync="rejectDialogVisible" width="450px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="商家名称">
          <span>{{ rejectRow.merchantId }}</span>
        </el-form-item>
        <el-form-item label="提现金额">
          <span>¥{{ Number(rejectRow.amount || 0).toLocaleString() }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button v-hasPermi="['mall:finance:edit']" type="danger" :loading="rejectLoading" @click="submitReject">确认拒绝</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getWithdrawList, approveWithdraw } from '@/api/finance'

export default {
  name: 'MerchantWithdraw',
  data() {
    return {
      loading: false,
      tableList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      queryParams: {
        merchantName: '',
        status: undefined
      },
      detailDialogVisible: false,
      detailRow: {},
      rejectDialogVisible: false,
      rejectRow: {},
      rejectReason: '',
      rejectLoading: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getWithdrawList({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        let list = res.rows
        // 客户端筛选
        if (this.queryParams.merchantName) {
          const keyword = this.queryParams.merchantName.toLowerCase()
          list = list.filter(item => (item.merchantName || '').toLowerCase().includes(keyword))
        }
        if (this.queryParams.status !== undefined && this.queryParams.status !== '') {
          list = list.filter(item => item.status === this.queryParams.status)
        }
        this.tableList = list
        this.total = res.total
      } catch (e) {
        this.$message.error('获取提现列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = { merchantName: '', status: undefined }
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
    handleViewDetail(row) {
      this.detailRow = { ...row }
      this.detailDialogVisible = true
    },
    handlePass(row) {
      this.$confirm(`确认通过商家 ${row.merchantId} 的提现申请（¥${Number(row.amount).toLocaleString()}）？`, '确认通过', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        try {
          await approveWithdraw(row.id, 1)
          this.$message.success('已通过提现申请')
          this.fetchData()
        } catch (e) {
          this.$message.error('操作失败')
        }
      }).catch(() => {})
    },
    handleReject(row) {
      this.rejectRow = { ...row }
      this.rejectReason = ''
      this.rejectDialogVisible = true
    },
    submitReject() {
      if (!this.rejectReason.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      this.rejectLoading = true
      approveWithdraw(this.rejectRow.id, 0).then(() => {
        this.$message.success('已拒绝提现申请')
        this.rejectDialogVisible = false
        this.fetchData()
      }).catch(() => {
        this.$message.error('操作失败')
      }).finally(() => {
        this.rejectLoading = false
      })
    },
    statusText(status) {
      const map = { 0: '待审核', 1: '已审核', 2: '已完成', 3: '已拒绝' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
      return map[status] || 'info'
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
.withdraw-amount {
  color: #e6a23c;
  font-weight: 500;
}
.pass-btn {
  color: #67c23a;
}
.reject-btn {
  color: #f56c6c;
}
</style>
