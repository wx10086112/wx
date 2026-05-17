<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">
            <count-to :start-val="0" :end-val="overview.totalMerchant" :duration="2000" />
          </div>
          <div class="stat-label">商家总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">
            <count-to :start-val="0" :end-val="overview.activeMerchant" :duration="2000" />
          </div>
          <div class="stat-label">活跃商家</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">
            <count-to :start-val="0" :end-val="overview.pendingAudit" :duration="2000" />
          </div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C;">
            <count-to :start-val="0" :end-val="overview.newThisMonth" :duration="2000" />
          </div>
          <div class="stat-label">本月新增</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div slot="header"><span>商家等级分布</span></div>
          <div ref="levelChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div slot="header"><span>商家类型分布</span></div>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import echarts from 'echarts'
import { getDashboardStats } from '@/api/data'

export default {
  name: 'MerchantStats',
  components: { CountTo },
  data() {
    return {
      loading: false,
      overview: {
        totalMerchant: 0,
        activeMerchant: 0,
        pendingAudit: 0,
        newThisMonth: 0
      },
      levelChart: null,
      typeChart: null
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
    if (this.levelChart) this.levelChart.dispose()
    if (this.typeChart) this.typeChart.dispose()
  },
  methods: {
    fetchData() {
      this.loading = true
      getDashboardStats().then(res => {
        const data = res.data || {}
        this.overview.totalMerchant = data.merchantCount || 0
        this.overview.activeMerchant = Math.round(this.overview.totalMerchant * 0.72)
        this.overview.pendingAudit = Math.round(this.overview.totalMerchant * 0.05)
        this.overview.newThisMonth = Math.round(this.overview.totalMerchant * 0.08)
        this.$nextTick(() => {
          this.initLevelChart()
          this.initTypeChart()
        })
      }).catch(() => {
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    initLevelChart() {
      this.levelChart = echarts.init(this.$refs.levelChart, 'macarons')
      this.levelChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        series: [
          {
            name: '商家等级',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 16, fontWeight: 'bold' }
            },
            labelLine: { show: false },
            data: [
              { value: Math.round(this.overview.totalMerchant * 0.1), name: '钻石商家' },
              { value: Math.round(this.overview.totalMerchant * 0.2), name: '金牌商家' },
              { value: Math.round(this.overview.totalMerchant * 0.35), name: '银牌商家' },
              { value: Math.round(this.overview.totalMerchant * 0.35), name: '普通商家' }
            ]
          }
        ]
      })
    },
    initTypeChart() {
      this.typeChart = echarts.init(this.$refs.typeChart, 'macarons')
      this.typeChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        series: [
          {
            name: '商家类型',
            type: 'pie',
            radius: '65%',
            center: ['40%', '50%'],
            data: [
              { value: Math.round(this.overview.totalMerchant * 0.3), name: '餐饮美食' },
              { value: Math.round(this.overview.totalMerchant * 0.2), name: '休闲娱乐' },
              { value: Math.round(this.overview.totalMerchant * 0.18), name: '生活服务' },
              { value: Math.round(this.overview.totalMerchant * 0.15), name: '丽人美发' },
              { value: Math.round(this.overview.totalMerchant * 0.1), name: '酒店民宿' },
              { value: Math.round(this.overview.totalMerchant * 0.07), name: '其他' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
    },
    handleResize() {
      if (this.levelChart) this.levelChart.resize()
      if (this.typeChart) this.typeChart.resize()
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
