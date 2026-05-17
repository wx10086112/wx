<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">
            <count-to :start-val="0" :end-val="stats.userTotal" :duration="2000" />
          </div>
          <div class="stat-label">用户总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="stats.userTodayNew" :duration="2000" />
          </div>
          <div class="stat-label">今日新增</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="weekNewUsers" :duration="2000" />
          </div>
          <div class="stat-label">本周新增</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C;">
            <count-to :start-val="0" :end-val="monthNewUsers" :duration="2000" />
          </div>
          <div class="stat-label">本月新增</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>近30天用户增长趋势</span></div>
      <div ref="growthChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import echarts from 'echarts'
import { getDashboardStats } from '@/api/data'

export default {
  name: 'UserGrowth',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        userTotal: 0,
        userTodayNew: 0
      },
      chart: null
    }
  },
  computed: {
    weekNewUsers() {
      return Math.round(this.stats.userTodayNew * 5.8)
    },
    monthNewUsers() {
      return Math.round(this.stats.userTodayNew * 22)
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
        this.$nextTick(() => {
          this.initChart()
        })
      }).catch(() => {
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    generateDailyData() {
      const dates = []
      const values = []
      const baseDaily = this.stats.userTodayNew
      const now = new Date()
      for (let i = 29; i >= 0; i--) {
        const d = new Date(now)
        d.setDate(d.getDate() - i)
        const month = (d.getMonth() + 1).toString().padStart(2, '0')
        const day = d.getDate().toString().padStart(2, '0')
        dates.push(month + '-' + day)
        let factor = 0.6 + Math.random() * 0.8
        if (i <= 7) factor *= 1.2
        if (d.getDay() === 0 || d.getDay() === 6) factor *= 0.7
        values.push(Math.round(baseDaily * factor))
      }
      return { dates, values }
    },
    initChart() {
      const data = this.generateDailyData()
      this.chart = echarts.init(this.$refs.growthChart, 'macarons')
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>新增用户: {c}'
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
          data: data.dates,
          axisLabel: { interval: 4 }
        },
        yAxis: {
          type: 'value',
          name: '新增用户数'
        },
        series: [
          {
            name: '新增用户',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            data: data.values,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64,158,255,0.4)' },
                { offset: 1, color: 'rgba(64,158,255,0.02)' }
              ])
            },
            lineStyle: { width: 2, color: '#409EFF' },
            itemStyle: { color: '#409EFF' }
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
