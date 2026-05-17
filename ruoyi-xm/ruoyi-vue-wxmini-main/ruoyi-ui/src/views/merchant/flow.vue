<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>商家流水</span></div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantId" placeholder="请输入商家ID" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="订单收入" value="income" />
            <el-option label="提现扣款" value="withdraw" />
            <el-option label="退款扣款" value="refund" />
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
        <el-table-column prop="merchantId" label="商家名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="flowTypeTag(scope.row.type)" size="small">{{ flowTypeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="center">
          <template slot-scope="scope">
            <span :class="Number(scope.row.amount || 0) >= 0 ? 'amount-positive' : 'amount-negative'">
              {{ scope.row.amount >= 0 ? '+' : '' }}¥{{ Math.abs(Number(scope.row.amount || 0)).toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="160" align="center" />
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
        merchantId: '',
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
        let list = res.rows
        // 客户端筛选
        if (this.queryParams.merchantId) {
          const keyword = this.queryParams.merchantId.toLowerCase()
          list = list.filter(item => item.merchantId.toLowerCase().includes(keyword))
        }
        if (this.queryParams.type) {
          list = list.filter(item => item.type === this.queryParams.type)
        }
        if (this.dateRange && this.dateRange.length === 2) {
          const [start, end] = this.dateRange
          list = list.filter(item => {
            const date = item.createTime.split(' ')[0]
            return date >= start && date <= end
          })
        }
        this.tableList = list
        this.total = res.total
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
      this.queryParams = { merchantId: '', type: '' }
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
      const map = { income: 'success', withdraw: 'warning', refund: 'danger', payment: 'success', commission: 'primary' }
      return map[type] || 'info'
    },
    flowTypeText(type) {
      const map = { income: '订单收入', withdraw: '提现扣款', refund: '退款扣款', payment: '支付收入', commission: '佣金' }
      return map[type] || type
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
