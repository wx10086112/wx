<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>商家列表</span>
        <el-button style="float: right; padding: 3px 0;" type="text" icon="el-icon-plus" @click="handleAdd">添加商户</el-button>
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
            <el-option label="待审核" :value="2" />
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
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-view" @click="handleDetail(scope.row)">查看详情</el-button>
            <el-button v-if="scope.row.status === 2" type="text" size="small" icon="el-icon-s-check" class="audit-btn" @click="handleAudit(scope.row)">审核</el-button>
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
  </div>
</template>

<script>
import { getMerchantList, auditMerchant } from '@/api/merchant'

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
      auditLoading: false
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
          pageSize: this.pageSize
        })
        let list = res.rows
        // 客户端筛选
        if (this.queryParams.name) {
          const keyword = this.queryParams.name.toLowerCase()
          list = list.filter(item => item.name.toLowerCase().includes(keyword))
        }
        if (this.queryParams.status !== undefined && this.queryParams.status !== '') {
          list = list.filter(item => item.status === this.queryParams.status)
        }
        this.tableList = list
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
      const map = { 0: '禁用', 1: '正常', 2: '待审核' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning' }
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
</style>
