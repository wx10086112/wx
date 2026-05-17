<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>提现记录</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantId" placeholder="请输入商家ID" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="商家" prop="merchantId" width="140" />
        <el-table-column label="提现金额" width="120">
          <template slot-scope="scope">
            <span class="text-primary">¥{{ Number(scope.row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="银行" prop="bankName" width="120" />
        <el-table-column label="账号" prop="bankAccount" width="120" />
        <el-table-column label="申请时间" prop="createTime" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="withdrawStatusMap[scope.row.status].type" size="small">
              {{ withdrawStatusMap[scope.row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" min-width="160">
          <template slot-scope="scope">
            <span>{{ scope.row.status >= 2 ? scope.row.updateTime : '-' }}</span>
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
  </div>
</template>

<script>
import { getWithdrawList } from '@/api/finance'

export default {
  name: 'WithdrawRecord',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        merchantId: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      withdrawStatusMap: {
        0: { text: '待审核', type: 'warning' },
        1: { text: '审核通过', type: '' },
        2: { text: '已完成', type: 'success' },
        3: { text: '已驳回', type: 'danger' }
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getWithdrawList(this.queryParams)
        this.tableList = res.rows
        this.total = res.total
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    resetQuery() {
      this.queryParams = {
        merchantId: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.fetchData()
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
.text-primary {
  color: #409EFF;
  font-weight: 500;
}
</style>
