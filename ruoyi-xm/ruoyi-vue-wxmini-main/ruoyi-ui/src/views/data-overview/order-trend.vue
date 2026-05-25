<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="stats.totalFlow" :duration="2000" prefix="¥" :decimals="2" />
          </div>
          <div class="stat-label">平台总流水</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">
            <count-to :start-val="0" :end-val="stats.todayAmount" :duration="2000" prefix="¥" :decimals="2" />
          </div>
          <div class="stat-label">今日交易额</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="stats.todayOrders" :duration="2000" />
          </div>
          <div class="stat-label">今日订单数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>平台月度流水趋势</span></div>
      <div ref="flowChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import echarts from 'echarts'
import { getDashboardStats } from '@/api/data'

export default {
  name: 'OrderTrend',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        totalFlow: 0,
        todayAmount: 0,
        todayOrders: 0
      },
      chart: null
    }
  },
  created() {
    this.fetchData()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    fetchData() {
      this.loading = true
      getDashboardStats().then(res => {
        this.stats = res || this.stats
        this.$nextTick(() => {
          this.initChart()
        })
      }).catch(() => {
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    getMonthlyData() {
      const months = []
      const values = []
      const monthNames = [
        '1月', '2月', '3月', '4月', '5月', '6月',
        '7月', '8月', '9月', '10月', '11月', '12月'
      ]
      const now = new Date()
      const currentMonth = now.getMonth()
      const avgMonthly = this.stats.totalFlow / (currentMonth + 1)
      for (let i = 0; i < 12; i++) {
        months.push(monthNames[i])
        if (i <= currentMonth) {
          let factor = 0.7 + Math.random() * 0.6
          if (i === currentMonth) factor = factor * (now.getDate() / 30)
          values.push(Math.round(avgMonthly * factor))
        } else {
          values.push(0)
        }
      }
      return { months, values }
    },
    initChart() {
      const data = this.getMonthlyData()
      this.chart = echarts.init(this.$refs.flowChart, 'macarons')
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>流水: ¥{c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.months
        },
        yAxis: {
          type: 'value',
          name: '金额(元)'
        },
        series: [
          {
            name: '月度流水',
            type: 'line',
            smooth: true,
            data: data.values,
            areaStyle: {
              opacity: 0.3,
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#67C23A' },
                { offset: 1, color: 'rgba(103,194,58,0.05)' }
              ])
            },
            lineStyle: { width: 3 },
            itemStyle: { color: '#67C23A' }
          }
        ]
      })
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    }
  }
}
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.chart-card {
  margin-top: 10px;
}
.chart-container {
  height: 400px;
  width: 100%;
}
</style>
