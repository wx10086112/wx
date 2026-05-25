<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>收益统计</span></div>

      <div v-loading="loading" class="stat-cards">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon income-bg">
            <i class="el-icon-coin" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayIncome) }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon income-bg">
            <i class="el-icon-money" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.monthIncome) }}</div>
            <div class="stat-label">本月收入</div>
          </div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon income-bg">
            <i class="el-icon-wallet" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.totalIncome) }}</div>
            <div class="stat-label">累计收入</div>
          </div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon commission-bg">
            <i class="el-icon-coin" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayCommission) }}</div>
            <div class="stat-label">今日佣金</div>
          </div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon commission-bg">
            <i class="el-icon-money" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.monthCommission) }}</div>
            <div class="stat-label">本月佣金</div>
          </div>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon commission-bg">
            <i class="el-icon-wallet" />
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.totalCommission) }}</div>
            <div class="stat-label">累计佣金</div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getIncomeStats } from '@/api/finance'

export default {
  name: 'IncomeStats',
  data() {
    return {
      loading: false,
      stats: {
        todayIncome: 0,
        monthIncome: 0,
        totalIncome: 0,
        todayCommission: 0,
        monthCommission: 0,
        totalCommission: 0
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
        const res = await getIncomeStats()
        this.stats = res.data
      } finally {
        this.loading = false
      }
    },
    formatMoney(val) {
      if (val === undefined || val === null) return '0.00'
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
  }
}
</script>

<style scoped>
.stat-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.stat-card {
  flex: 1;
  min-width: 240px;
}
.stat-card >>> .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px 24px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}
.income-bg {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}
.commission-bg {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
</style>
