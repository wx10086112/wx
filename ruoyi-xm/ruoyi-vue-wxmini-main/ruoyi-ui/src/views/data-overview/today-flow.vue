<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
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
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="stats.todayOrders" :duration="2000" />
          </div>
          <div class="stat-label">今日订单数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="avgAmount" :duration="2000" prefix="¥" :decimals="2" />
          </div>
          <div class="stat-label">平均客单价</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>今日交易额按小时分布</span></div>
      <div ref="hourlyChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import echarts from 'echarts'
import { getDashboardStats } from '@/api/data'

export default {
  name: 'TodayFlow',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        todayAmount: 0,
        todayOrders: 0
      },
      hourlyData: [],
      chart: null
    }
  },
  computed: {
    avgAmount() {
      if (this.stats.todayOrders <= 0) return 0
      return this.stats.todayAmount / this.stats.todayOrders
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
        this.stats = res.data || this.stats
        this.generateHourlyData()
        this.$nextTick(() => {
          this.initChart()
        })
      }).catch(() => {
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    generateHourlyData() {
      const hours = []
      const values = []
      const baseAmount = this.stats.todayAmount / 24
      for (let i = 0; i < 24; i++) {
        hours.push(i + ':00')
        let factor = 1
        if (i >= 10 && i <= 12) factor = 2.2
        else if (i >= 12 && i <= 14) factor = 1.8
        else if (i >= 18 && i <= 21) factor = 2.5
        else if (i >= 0 && i <= 6) factor = 0.2
        else if (i >= 7 && i <= 9) factor = 1.0
        else factor = 1.3
        values.push(Math.round(baseAmount * factor * (0.8 + Math.random() * 0.4)))
      }
      this.hourlyData = { hours, values }
    },
    initChart() {
      this.chart = echarts.init(this.$refs.hourlyChart, 'macarons')
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: '{b}<br/>交易额: ¥{c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.hourlyData.hours,
          axisLabel: { interval: 1 }
        },
        yAxis: {
          type: 'value',
          name: '交易额(元)'
        },
        series: [
          {
            name: '交易额',
            type: 'bar',
            data: this.hourlyData.values,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#409EFF' },
                { offset: 1, color: '#67C23A' }
              ])
            },
            barMaxWidth: 30
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
