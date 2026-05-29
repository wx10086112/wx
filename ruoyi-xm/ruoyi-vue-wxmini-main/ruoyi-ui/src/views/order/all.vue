<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>全部订单</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已核销" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已退款" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
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
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="商家" prop="merchantName" width="140" />
        <el-table-column label="用户" prop="userName" width="100" />
        <el-table-column label="订单金额" width="110">
          <template slot-scope="scope">
            <span>¥{{ (scope.row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实付金额" width="110">
          <template slot-scope="scope">
            <span class="text-primary">¥{{ (scope.row.payAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="statusMap[scope.row.status]" :type="statusMap[scope.row.status].type" size="small">
              {{ statusMap[scope.row.status].text }}
            </el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="createTime" width="160" />
        <el-table-column label="支付时间" prop="payTime" width="160">
          <template slot-scope="scope">
            <span>{{ scope.row.payTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
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
import { getOrderList } from '@/api/order'

export default {
  name: 'OrderAll',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        orderNo: '',
        merchantName: '',
        userName: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      dateRange: [],
      statusMap: {
        0: { text: '待付款', type: 'warning' },
        1: { text: '已付款', type: '' },
        2: { text: '已核销', type: 'success' },
        3: { text: '已完成', type: 'success' },
        4: { text: '已退款', type: 'info' },
        5: { text: '已取消', type: 'danger' }
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
        const params = { ...this.queryParams }
        if (this.dateRange && this.dateRange.length === 2) {
          params.beginTime = this.dateRange[0]
          params.endTime = this.dateRange[1]
        }
        const res = await getOrderList(params)
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
        orderNo: '',
        merchantName: '',
        userName: '',
        status: '',
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
    },
    handleDetail(row) {
      this.$router.push({ name: 'OrderDetail', params: { id: row.id } })
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
