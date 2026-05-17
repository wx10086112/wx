<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>商家分账</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantId" placeholder="请输入商家ID" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="结算状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="未结算" :value="0" />
            <el-option label="已结算" :value="1" />
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
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="订单金额" width="120">
          <template slot-scope="scope">
            <span>¥{{ Number(scope.row.orderAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="佣金比例" width="100" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.commissionRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="佣金" width="120">
          <template slot-scope="scope">
            <span class="text-warning">¥{{ Number(scope.row.commission || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商家收入" width="120">
          <template slot-scope="scope">
            <span class="text-success">¥{{ (Number(scope.row.orderAmount || 0) - Number(scope.row.commission || 0)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag type="success" size="small">已结算</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="createTime" min-width="160" />
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
import { getProfitShareList } from '@/api/finance'

export default {
  name: 'ProfitSharing',
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
        const res = await getProfitShareList(this.queryParams)
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
.text-warning {
  color: #E6A23C;
  font-weight: 500;
}
.text-success {
  color: #67C23A;
  font-weight: 500;
}
</style>
