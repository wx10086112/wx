<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="商家名称">
        <el-input v-model="queryParams.keyword" placeholder="请输入商家名称" clearable
          @keyup.enter.native="handleSearch" style="width: 220px;" />
      </el-form-item>
      <el-form-item label="排序方式">
        <el-select v-model="queryParams.sortBy" placeholder="请选择" @change="handleSearch" style="width: 150px;">
          <el-option label="销售额" value="sales" />
          <el-option label="订单数" value="orders" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="rankList" stripe border style="width: 100%">
        <el-table-column label="排名" width="80" align="center">
          <template slot-scope="scope">
            <span v-if="scope.$index < 3" class="rank-icon">
              <i v-if="scope.$index === 0" class="el-icon-trophy" style="color: #FFD700;" />
              <i v-else-if="scope.$index === 1" class="el-icon-trophy" style="color: #C0C0C0;" />
              <i v-else class="el-icon-trophy" style="color: #CD7F32;" />
            </span>
            <span v-else>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商家名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sales" label="销售额(元)" width="160" align="right" sortable>
          <template slot-scope="scope">
            <span class="sales-value">¥{{ formatAmount(scope.row.sales) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orders" label="订单数" width="120" align="right" />
        <el-table-column prop="completedOrders" label="完成订单" width="120" align="right" />
        <el-table-column prop="completionRate" label="完成率" width="120" align="center">
          <template slot-scope="scope">
            {{ (scope.row.completionRate * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="140" align="right">
          <template slot-scope="scope">
            <span style="color: #F56C6C;">¥{{ formatAmount(scope.row.refundAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </el-card>
  </div>
</template>

<script>
import { getMerchantRankList } from '@/api/analysis'

export default {
  name: 'SalesRank',
  data() {
    return {
      loading: false,
      total: 0,
      queryParams: {
        keyword: '',
        sortBy: 'sales',
        pageNum: 1,
        pageSize: 10
      },
      rankList: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
      getMerchantRankList(this.queryParams).then(res => {
        const data = res.data || {}
        this.rankList = data.rows || []
        this.total = data.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = {
        keyword: '',
        sortBy: 'sales',
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.fetchData()
    },
    formatAmount(val) {
      if (val == null) return '0.00'
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 15px;
}
.rank-icon {
  font-size: 20px;
  line-height: 1;
}
.sales-value {
  color: #F56C6C;
  font-weight: bold;
}
.pagination {
  margin-top: 15px;
  text-align: right;
}
</style>
