<template>
  <div class="app-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4" v-for="item in statCards" :key="item.label">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-value" :style="{ color: item.color }">
            <count-to :start-val="0" :end-val="item.value" :duration="2000"
              :prefix="item.prefix" :decimals="item.decimals || 0" :separator="item.separator || ','" />
          </div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>近7日订单趋势</span></div>
      <div ref="trendChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import * as echarts from 'echarts'
import { getDashboardStats, getTrendData } from '@/api/data'

export default {
  name: 'Overview',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        todayAmount: 0,
        totalFlow: 0,
        todayOrders: 0,
        merchantCount: 0,
        userTotal: 0,
        userTodayNew: 0
      },
      trendData: {
        dates: [],
        orderCounts: [],
        amounts: [],
        completedCounts: []
      },
      chart: null
    }
  },
  computed: {
    statCards() {
      return [
        { label: '今日交易额', value: this.stats.todayAmount, color: '#409EFF', prefix: '¥', decimals: 2 },
        { label: '平台总流水', value: this.stats.totalFlow, color: '#67C23A', prefix: '¥', decimals: 2 },
        { label: '今日订单数', value: this.stats.todayOrders, color: '#E6A23C' },
        { label: '入驻商家数', value: this.stats.merchantCount, color: '#F56C6C' },
        { label: '用户总数', value: this.stats.userTotal, color: '#909399' },
        { label: '今日新增用户', value: this.stats.userTodayNew, color: '#5CBB7A' }
      ]
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
      Promise.all([
        getDashboardStats(),
        getTrendData()
      ]).then(([statsRes, trendRes]) => {
        this.stats = statsRes.data || this.stats
        this.trendData = trendRes.data || this.trendData
        this.$nextTick(() => {
          this.initChart()
        })
      }).catch(() => {
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.trendChart, 'macarons')
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['订单量', '完成量', '交易额']
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
          data: this.trendData.dates
        },
        yAxis: [
          {
            type: 'value',
            name: '订单量',
            position: 'left'
          },
          {
            type: 'value',
            name: '金额(元)',
            position: 'right'
          }
        ],
        series: [
          {
            name: '订单量',
            type: 'line',
            smooth: true,
            data: this.trendData.orderCounts,
            areaStyle: { opacity: 0.15 }
          },
          {
            name: '完成量',
            type: 'line',
            smooth: true,
            data: this.trendData.completedCounts,
            areaStyle: { opacity: 0.15 }
          },
          {
            name: '交易额',
            type: 'line',
            smooth: true,
            yAxisIndex: 1,
            data: this.trendData.amounts
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
