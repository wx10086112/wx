<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>财务报表</span>
        <el-button type="primary" size="small" icon="el-icon-download" @click="handleExport">导出报表</el-button>
      </div>

      <!-- 统计卡片 -->
      <div v-loading="loading" class="stat-cards">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value text-success">¥{{ formatMoney(report.totalRevenue) }}</div>
          <div class="stat-label">总营收</div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value text-primary">¥{{ formatMoney(report.totalCommission) }}</div>
          <div class="stat-label">总佣金</div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value text-warning">¥{{ formatMoney(report.totalWithdraw) }}</div>
          <div class="stat-label">总提现</div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value text-danger">¥{{ formatMoney(report.totalRefund) }}</div>
          <div class="stat-label">总退款</div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value text-purple">¥{{ formatMoney(report.netProfit) }}</div>
          <div class="stat-label">净利润</div>
        </el-card>
      </div>

      <!-- 月度明细表 -->
      <el-table v-loading="loading" :data="report.monthlyData || []" border style="margin-top: 20px;">
        <el-table-column label="月份" prop="month" width="140" />
        <el-table-column label="营收" width="160">
          <template slot-scope="scope">
            <span class="text-success">¥{{ formatMoney(scope.row.revenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="佣金" width="160">
          <template slot-scope="scope">
            <span class="text-primary">¥{{ formatMoney(scope.row.commission) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单数" prop="orders" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getFinanceReport } from '@/api/finance'

export default {
  name: 'FinanceReport',
  data() {
    return {
      loading: false,
      report: {
        totalRevenue: 0,
        totalCommission: 0,
        totalWithdraw: 0,
        totalRefund: 0,
        netProfit: 0,
        monthlyData: []
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
        const res = await getFinanceReport()
        this.report = res.data
      } finally {
        this.loading = false
      }
    },
    formatMoney(val) {
      if (val === undefined || val === null) return '0.00'
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    handleExport() {
      this.$message.info('导出功能开发中')
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.stat-card {
  flex: 1;
  min-width: 160px;
  text-align: center;
}
.stat-card >>> .el-card__body {
  padding: 20px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.text-success { color: #67C23A; }
.text-primary { color: #409EFF; }
.text-warning { color: #E6A23C; }
.text-danger { color: #F56C6C; }
.text-purple { color: #9B59B6; }
</style>
