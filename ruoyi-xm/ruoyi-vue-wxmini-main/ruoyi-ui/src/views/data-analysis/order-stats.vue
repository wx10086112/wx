<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">
            <count-to :start-val="0" :end-val="stats.totalOrders" :duration="2000" />
          </div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="stats.completedOrders" :duration="2000" />
          </div>
          <div class="stat-label">已完成订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="stats.refundOrders" :duration="2000" />
          </div>
          <div class="stat-label">退款订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C;">
            <count-to :start-val="0" :end-val="stats.abnormalOrders" :duration="2000" />
          </div>
          <div class="stat-label">异常订单</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <div slot="header"><span>每日订单统计</span></div>
      <div ref="stackedChart" class="chart-container"></div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <div slot="header"><span>每日订单明细</span></div>
      <el-table :data="dailyTable" stripe border style="width: 100%">
        <el-table-column prop="date" label="日期" width="140" align="center" />
        <el-table-column prop="newOrders" label="新增订单" width="140" align="right">
          <template slot-scope="scope">
            <el-tag type="primary" size="small">{{ scope.row.newOrders }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completed" label="已完成" width="140" align="right">
          <template slot-scope="scope">
            <el-tag type="success" size="small">{{ scope.row.completed }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refund" label="退款" width="140" align="right">
          <template slot-scope="scope">
            <el-tag type="warning" size="small">{{ scope.row.refund }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成率" align="center">
          <template slot-scope="scope">
            <el-progress :percentage="getCompletionRate(scope.row)"
              :stroke-width="14" :color="getRateColor(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import * as echarts from 'echarts'
import { getOrderStats } from '@/api/analysis'

export default {
  name: 'OrderStats',
  components: { CountTo },
  data() {
    return {
      loading: false,
      stats: {
        totalOrders: 0,
        completedOrders: 0,
        refundOrders: 0,
        abnormalOrders: 0,
        dailyData: []
      },
      stackedChart: null
    }
  },
  computed: {
    dailyTable() {
      return (this.stats.dailyData || []).filter(d => d.newOrders > 0).slice().reverse()
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
    if (this.stackedChart) {
      this.stackedChart.dispose()
    }
  },
  methods: {
    fetchData() {
      this.loading = true
      getOrderStats().then(res => {
        const data = res.data || {}
        this.stats = {
          totalOrders: Number(data.totalOrders) || 0,
          completedOrders: Number(data.completedOrders) || 0,
          refundOrders: Number(data.refundOrders) || 0,
          abnormalOrders: Number(data.abnormalOrders) || 0,
          dailyData: data.dailyData || []
        }
        this.$nextTick(() => {
          this.initChart()
        })
      }).finally(() => {
        this.loading = false
      })
    },
    initChart() {
      const data = this.stats.dailyData || []
      if (data.length === 0) return

      const dates = data.map(d => d.date ? d.date.substring(5) : '')
      const newOrders = data.map(d => d.newOrders || 0)
      const completed = data.map(d => d.completed || 0)
      const refunds = data.map(d => d.refund || 0)

      this.stackedChart = echarts.init(this.$refs.stackedChart, 'macarons')
      this.stackedChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['新增订单', '已完成', '退款']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { interval: 2 }
        },
        yAxis: {
          type: 'value',
          name: '订单数量'
        },
        series: [
          {
            name: '新增订单',
            type: 'bar',
            stack: 'total',
            emphasis: { focus: 'series' },
            data: newOrders,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '已完成',
            type: 'bar',
            stack: 'total',
            emphasis: { focus: 'series' },
            data: completed,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '退款',
            type: 'bar',
            stack: 'total',
            emphasis: { focus: 'series' },
            data: refunds,
            itemStyle: { color: '#E6A23C' }
          }
        ]
      })
    },
    getCompletionRate(row) {
      if (!row.newOrders || row.newOrders === 0) return 0
      return Math.round((row.completed / row.newOrders) * 100)
    },
    getRateColor(row) {
      const rate = this.getCompletionRate(row)
      if (rate >= 90) return '#67C23A'
      if (rate >= 80) return '#E6A23C'
      return '#F56C6C'
    },
    handleResize() {
      if (this.stackedChart) {
        this.stackedChart.resize()
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
  margin-bottom: 20px;
}
.chart-container {
  height: 400px;
  width: 100%;
}
.table-card {
  margin-bottom: 10px;
}
</style>
