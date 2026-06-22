<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>分销商佣金结算</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="分销商ID">
          <el-input v-model="queryParams.distributorId" placeholder="请输入分销商ID" clearable />
        </el-form-item>
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待结算" value="WAITING_SETTLEMENT" />
            <el-option label="转账中" value="TRANSFERRING" />
            <el-option label="已到账" value="ARRIVED" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已冲正" value="REVERSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="结算单号" prop="settlementNo" width="200" align="center" />
        <el-table-column label="分销商ID" prop="distributorId" width="110" align="center" />
        <el-table-column label="商家名称" prop="merchantName" width="110" align="center" />
        <el-table-column label="订单号" prop="orderNo" width="180" align="center" />
        <el-table-column label="金额" width="120" align="center">
          <template slot-scope="scope">
            <span class="text-primary">¥{{ Number(scope.row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="费率" width="90" align="center">
          <template slot-scope="scope">
            <span>{{ Number(scope.row.rate || 0) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusMap[scope.row.status] || 'info'" size="small">
              {{ statusTextMap[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结算周期开始" prop="settlementPeriodStart" width="160" align="center" />
        <el-table-column label="结算周期结束" prop="settlementPeriodEnd" width="160" align="center" />
        <el-table-column label="预计到账时间" prop="expectedTransferTime" width="160" align="center" />
        <el-table-column label="实际到账时间" prop="arriveTime" width="160" align="center" />
        <el-table-column label="失败原因" prop="failReason" min-width="160" show-overflow-tooltip />
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
import { getDistributorSettlementList } from '@/api/finance/settlement'

export default {
  name: 'DistributorSettlement',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        distributorId: '',
        merchantName: '',
        orderNo: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      statusMap: {
        WAITING_SETTLEMENT: 'warning',
        TRANSFERRING: 'primary',
        ARRIVED: 'success',
        FAILED: 'danger',
        CANCELLED: 'info',
        REVERSED: 'info'
      },
      statusTextMap: {
        WAITING_SETTLEMENT: '待结算',
        TRANSFERRING: '转账中',
        ARRIVED: '已到账',
        FAILED: '失败',
        CANCELLED: '已取消',
        REVERSED: '已冲正'
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const res = await getDistributorSettlementList(this.queryParams)
        this.tableList = res.rows
        this.total = res.total
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        distributorId: '',
        merchantName: '',
        orderNo: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      }
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
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
