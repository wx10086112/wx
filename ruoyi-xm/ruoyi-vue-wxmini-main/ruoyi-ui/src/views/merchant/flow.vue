<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>商家流水</span></div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="订单收入" value="订单收入" />
            <el-option label="提现扣款" value="提现扣款" />
            <el-option label="退款扣款" value="退款扣款" />
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
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableList" border style="width: 100%">
        <el-table-column prop="merchantName" label="商家名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="flowTypeTag(scope.row.type)" size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="center">
          <template slot-scope="scope">
            <span :class="scope.row.amount >= 0 ? 'amount-positive' : 'amount-negative'">
              {{ scope.row.amount >= 0 ? '+' : '' }}¥{{ Math.abs(scope.row.amount).toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="关联订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="time" label="时间" width="160" align="center" />
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
  </div>
</template>

<script>
import { getMerchantFlowList } from '@/api/merchant'

export default {
  name: 'MerchantFlow',
  data() {
    return {
      loading: false,
      tableList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dateRange: null,
      queryParams: {
        merchantName: '',
        type: ''
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
        const res = await getMerchantFlowList({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        let list = res.data.list
        // 客户端筛选
        if (this.queryParams.merchantName) {
          const keyword = this.queryParams.merchantName.toLowerCase()
          list = list.filter(item => item.merchantName.toLowerCase().includes(keyword))
        }
        if (this.queryParams.type) {
          list = list.filter(item => item.type === this.queryParams.type)
        }
        if (this.dateRange && this.dateRange.length === 2) {
          const [start, end] = this.dateRange
          list = list.filter(item => {
            const date = item.time.split(' ')[0]
            return date >= start && date <= end
          })
        }
        this.tableList = list
        this.total = res.data.total
      } catch (e) {
        this.$message.error('获取流水列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = { merchantName: '', type: '' }
      this.dateRange = null
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
    flowTypeTag(type) {
      const map = { '订单收入': 'success', '提现扣款': 'warning', '退款扣款': 'danger' }
      return map[type] || 'info'
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
.amount-positive {
  color: #67c23a;
  font-weight: 500;
}
.amount-negative {
  color: #f56c6c;
  font-weight: 500;
}
</style>
