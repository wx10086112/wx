<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">
            <count-to :start-val="0" :end-val="stats.totalSales" :duration="2000" prefix="¥" :decimals="2" />
          </div>
          <div class="stat-label">总销售额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="stats.totalOrders" :duration="2000" />
          </div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="stats.avgOrderAmount" :duration="2000" prefix="¥" :decimals="2" />
          </div>
          <div class="stat-label">平均客单价</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C;">
            <count-to :start-val="0" :end-val="conversionRateDisplay" :duration="2000" suffix="%" :decimals="1" />
          </div>
          <div class="stat-label">转化率</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>近7日销售趋势</span></div>
      <div ref="barChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import echarts from 'echarts'
import { getSalesStats } from '@/api/analysis'

export default {
  name: 'SalesStats',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        totalSales: 0,
        totalOrders: 0,
        avgOrderAmount: 0,
        conversionRate: 0
      },
      trendData: null,
      barChart: null
    }
  },
  computed: {
    conversionRateDisplay() {
      return (this.stats.conversionRate || 0) * 100
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
    if (this.barChart) this.barChart.dispose()
  },
  methods: {
    fetchData() {
      this.loading = true
      getSalesStats().then(res => {
        const data = res.data || {}
        this.stats = {
          totalSales: Number(data.totalSales) || 0,
          totalOrders: Number(data.totalOrders) || 0,
          avgOrderAmount: Number(data.avgOrderAmount) || 0,
          conversionRate: Number(data.conversionRate) || 0
        }
        this.trendData = data.trendData || null
        this.$nextTick(() => {
          this.initBarChart()
        })
      }).finally(() => {
        this.loading = false
      })
    },
    initBarChart() {
      if (!this.trendData) return
      const dates = this.trendData.dates || []
      const amounts = (this.trendData.amounts || []).map(v => Number(v) || 0)
      const counts = (this.trendData.orderCounts || []).map(v => Number(v) || 0)

      this.barChart = echarts.init(this.$refs.barChart, 'macarons')
      this.barChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['销售额', '订单数']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: [
          {
            type: 'value',
            name: '金额(元)',
            position: 'left'
          },
          {
            type: 'value',
            name: '订单数',
            position: 'right'
          }
        ],
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: amounts,
            yAxisIndex: 0,
            barMaxWidth: 35,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '订单数',
            type: 'line',
            smooth: true,
            data: counts,
            yAxisIndex: 1,
            lineStyle: { color: '#E6A23C' },
            itemStyle: { color: '#E6A23C' }
          }
        ]
      })
    },
    handleResize() {
      if (this.barChart) this.barChart.resize()
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
  margin-bottom: 10px;
}
.chart-container {
  height: 350px;
  width: 100%;
}
</style>
