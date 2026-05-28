<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>平台流水</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable>
            <el-option label="支付收入" value="payment" />
            <el-option label="退款支出" value="refund" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="类型" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="typeMap[scope.row.type] ? typeMap[scope.row.type].type : 'info'" size="small">
              {{ typeMap[scope.row.type] ? typeMap[scope.row.type].text : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="130">
          <template slot-scope="scope">
            <span :class="Number(scope.row.amount || 0) >= 0 ? 'text-success' : 'text-danger'">
              {{ Number(scope.row.amount || 0) >= 0 ? '+' : '' }}¥{{ Math.abs(Number(scope.row.amount || 0)).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="商家" prop="merchantName" width="140" />
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="平台佣金" width="120">
          <template slot-scope="scope">
            <span class="text-primary">-</span>
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
import { getPlatformFlowList } from '@/api/finance'

export default {
  name: 'PlatformFlow',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        type: '',
        pageNum: 1,
        pageSize: 10
      },
      dateRange: [],
      typeMap: {
        payment: { text: '支付收入', type: 'success' },
        income: { text: '收入', type: 'success' },
        withdraw: { text: '提现', type: 'warning' },
        refund: { text: '退款', type: 'danger' },
        commission: { text: '佣金', type: 'primary' }
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
        const res = await getPlatformFlowList(this.queryParams)
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
        type: '',
        pageNum: 1,
        pageSize: 10
      }
      this.dateRange = []
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
.text-success {
  color: #67C23A;
  font-weight: 500;
}
.text-danger {
  color: #F56C6C;
  font-weight: 500;
}
.text-primary {
  color: #409EFF;
  font-weight: 500;
}
</style>
