<template>
  <div class="dashboard-container">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="8">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-shopping">
            <svg-icon icon-class="shopping" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日订单</div>
            <count-to :start-val="0" :end-val="stats.todayOrders" :duration="3000" class="card-panel-num" />
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="8">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-money">
            <svg-icon icon-class="money" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日营收(元)</div>
            <count-to :start-val="0" :end-val="stats.todayRevenue" :duration="3200" class="card-panel-num" />
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="8">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-skill">
            <svg-icon icon-class="skill" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">入驻商家</div>
            <count-to :start-val="0" :end-val="stats.merchantCount" :duration="3600" class="card-panel-num" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :lg="14">
        <el-card>
          <div slot="header" class="chart-header">
            <span>订单趋势</span>
            <el-select v-model="orderRange" size="small" style="width: 100px;" @change="onRangeChange('order')">
              <el-option label="按日" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
              <el-option label="按年" value="year" />
            </el-select>
          </div>
          <div ref="orderTrendChart" style="height: 320px;" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="10">
        <el-card>
          <div slot="header" class="chart-header">
            <span>营收趋势</span>
            <el-select v-model="revenueRange" size="small" style="width: 100px;" @change="onRangeChange('revenue')">
              <el-option label="按日" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
              <el-option label="按年" value="year" />
            </el-select>
          </div>
          <div ref="revenueChart" style="height: 320px;" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 分布 + 排行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card>
          <div slot="header"><span>订单状态分布</span></div>
          <div ref="orderStatusChart" style="height: 300px;" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card>
          <div slot="header"><span>热销商品 TOP5</span></div>
          <el-table :data="hotProducts" style="width: 100%;" :show-header="true">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template slot-scope="scope">
                <span :class="['rank-badge', 'rank-' + (scope.$index + 1)]">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="sales" label="销量" width="80" align="center" />
            <el-table-column prop="revenue" label="营收(元)" width="100" align="right">
              <template slot-scope="scope">
                <span class="revenue-text">¥{{ Number(scope.row.revenue || 0).toLocaleString() }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" class="shortcut-row">
      <el-col :xs="12" :sm="6" :lg="6" v-for="item in shortcuts" :key="item.title">
        <el-card class="shortcut-card" shadow="hover" @click.native="handleShortcut(item)">
          <div class="shortcut-icon">
            <svg-icon :icon-class="item.icon" class-name="shortcut-svg" />
          </div>
          <div class="shortcut-info">
            <div class="shortcut-title">{{ item.title }}</div>
            <div class="shortcut-desc">{{ item.desc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import CountTo from 'vue-count-to'
import { getDashboardStats, getTrendData, getOrderStatusData, getHotProducts } from '@/api/data'

export default {
  name: 'Dashboard',
  components: { CountTo },
  data() {
    return {
      charts: [],
      stats: { todayOrders: 0, todayRevenue: 0, merchantCount: 0 },
      trendData: { dates: [], orderCounts: [], completedCounts: [], revenues: [] },
      orderStatus: [],
      hotProducts: [],
      orderRange: 'day',
      revenueRange: 'day',
      shortcuts: [
        { title: '用户管理', desc: '管理商家账号', icon: 'peoples', path: '/merchant/user' },
        { title: '订单管理', desc: '查看团购订单', icon: 'shopping', path: '/finance/all' },
        { title: '商家管理', desc: '入驻商家信息', icon: 'skill', path: '/merchant/list' },
        { title: '数据统计', desc: '运营数据分析', icon: 'chart', path: '/data-analysis/rank' }
      ]
    }
  },
  created() {
    this.fetchData()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.charts.forEach(chart => {
      if (chart) chart.dispose()
    })
  },
  methods: {
    async fetchData() {
      try {
        const [statsRes, trendRes, statusRes, hotRes] = await Promise.all([
          getDashboardStats().catch(() => ({ data: null })),
          getTrendData().catch(() => ({ data: null })),
          getOrderStatusData().catch(() => ({ data: null })),
          getHotProducts().catch(() => ({ data: null }))
        ])
        // 适配后端字段名: todayAmount→todayRevenue, totalFlow→totalFlow
        const rawStats = statsRes.data
        this.stats = rawStats ? {
          todayOrders: rawStats.todayOrders || 0,
          todayRevenue: Number(rawStats.todayAmount || 0),
          merchantCount: rawStats.merchantCount || 0
        } : { todayOrders: 328, todayRevenue: 45920, merchantCount: 156 }
        // 适配后端字段名: amounts→revenues
        const rawTrend = trendRes.data
        this.trendData = rawTrend ? {
          dates: rawTrend.dates || [],
          orderCounts: rawTrend.orderCounts || [],
          completedCounts: rawTrend.completedCounts || [],
          revenues: (rawTrend.amounts || rawTrend.revenues || [])
        } : this.getMockTrend()
        // 适配后端字段名: count→value
        const rawStatus = statusRes.data
        this.orderStatus = Array.isArray(rawStatus) && rawStatus.length > 0
          ? rawStatus.map(i => ({ name: i.name, value: i.count || i.value || 0 }))
          : this.getMockOrderStatus()
        this.hotProducts = (Array.isArray(hotRes.data) && hotRes.data.length > 0)
          ? hotRes.data : this.getMockHotProducts()
      } catch (e) {
        this.stats = { todayOrders: 328, todayRevenue: 45920, merchantCount: 156 }
        this.trendData = this.getMockTrend()
        this.orderStatus = this.getMockOrderStatus()
        this.hotProducts = this.getMockHotProducts()
      }
      this.$nextTick(() => {
        this.initOrderTrendChart()
        this.initRevenueChart()
        this.initOrderStatusChart()
        window.addEventListener('resize', this.handleResize)
      })
    },
    getMockTrend() {
      return {
        dates: this.getLast7Days(),
        orderCounts: [280, 310, 295, 340, 328, 365, 328],
        completedCounts: [220, 260, 240, 290, 275, 310, 285],
        revenues: [38500, 42100, 39800, 47600, 45920, 51300, 45920]
      }
    },
    getMockOrderStatus() {
      return [
        { value: 85, name: '待支付' },
        { value: 156, name: '已支付' },
        { value: 520, name: '已完成' },
        { value: 42, name: '已取消' }
      ]
    },
    getMockHotProducts() {
      return [
        { name: '精品双人火锅套餐', sales: 1280, revenue: 126720 },
        { name: '日式料理四人餐', sales: 856, revenue: 119840 },
        { name: '鲜果拼团10斤装', sales: 2100, revenue: 62790 },
        { name: '健身私教体验课', sales: 645, revenue: 51600 },
        { name: '美甲美容套餐', sales: 432, revenue: 38880 }
      ]
    },
    handleResize() {
      this.charts.forEach(chart => {
        if (chart) chart.resize()
      })
    },
    initOrderTrendChart() {
      const chart = echarts.init(this.$refs.orderTrendChart, 'macarons')
      this.charts.push(chart)
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          padding: [5, 10]
        },
        legend: { data: ['订单数', '成交数'] },
        grid: { left: 10, right: 20, bottom: 20, top: 40, containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendData.dates,
          axisTick: { show: false }
        },
        yAxis: { type: 'value', axisTick: { show: false } },
        series: [
          {
            name: '订单数',
            type: 'line',
            smooth: true,
            data: this.trendData.orderCounts,
            itemStyle: { color: '#36a3f7' },
            areaStyle: { color: 'rgba(54,163,247,0.1)' },
            animationDuration: 2000
          },
          {
            name: '成交数',
            type: 'line',
            smooth: true,
            data: this.trendData.completedCounts,
            itemStyle: { color: '#34bfa3' },
            areaStyle: { color: 'rgba(52,191,163,0.1)' },
            animationDuration: 2000
          }
        ]
      })
    },
    initRevenueChart() {
      const chart = echarts.init(this.$refs.revenueChart, 'macarons')
      this.charts.push(chart)
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          padding: [5, 10],
          formatter: '{b}<br/>{a}: ¥{c}'
        },
        grid: { left: 10, right: 20, bottom: 20, top: 30, containLabel: true },
        xAxis: {
          type: 'category',
          data: this.trendData.dates,
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          axisTick: { show: false },
          axisLabel: { formatter: '¥{value}' }
        },
        series: [
          {
            name: '营收',
            type: 'bar',
            barWidth: '50%',
            data: this.trendData.revenues,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#f4516c' },
                { offset: 1, color: '#ff8a9e' }
              ])
            },
            animationDuration: 2000
          }
        ]
      })
    },
    initOrderStatusChart() {
      const statusColors = { '待支付': '#e6a23c', '已支付': '#409eff', '已完成': '#67c23a', '已取消': '#909399' }
      const chart = echarts.init(this.$refs.orderStatusChart, 'macarons')
      this.charts.push(chart)
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          right: 20,
          top: 'center',
          data: this.orderStatus.map(i => i.name)
        },
        series: [
          {
            type: 'pie',
            radius: ['40%', '65%'],
            center: ['40%', '50%'],
            avoidLabelOverlap: false,
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 14, fontWeight: 'bold' }
            },
            labelLine: { show: false },
            data: this.orderStatus.map(i => ({
              value: i.value,
              name: i.name,
              itemStyle: { color: statusColors[i.name] || '#909399' }
            })),
            animationDuration: 2000
          }
        ]
      })
    },
    getLast7Days() {
      const days = []
      for (let i = 6; i >= 0; i--) {
        const d = new Date()
        d.setDate(d.getDate() - i)
        days.push((d.getMonth() + 1) + '/' + d.getDate())
      }
      return days
    },
    getRangeLabels(range) {
      const labels = []
      const now = new Date()
      if (range === 'day') {
        for (let i = 6; i >= 0; i--) {
          const d = new Date(now)
          d.setDate(d.getDate() - i)
          labels.push((d.getMonth() + 1) + '/' + d.getDate())
        }
      } else if (range === 'week') {
        for (let i = 7; i >= 1; i--) {
          labels.push('第' + i + '周')
        }
      } else if (range === 'month') {
        for (let i = 11; i >= 0; i--) {
          const d = new Date(now)
          d.setMonth(d.getMonth() - i)
          labels.push((d.getMonth() + 1) + '月')
        }
      } else if (range === 'year') {
        for (let i = 4; i >= 0; i--) {
          labels.push((now.getFullYear() - i) + '年')
        }
      }
      return labels
    },
    generateMockData(range) {
      const labels = this.getRangeLabels(range)
      const count = labels.length
      return {
        dates: labels,
        orderCounts: Array.from({ length: count }, () => Math.floor(Math.random() * 200 + 200)),
        completedCounts: Array.from({ length: count }, () => Math.floor(Math.random() * 150 + 150)),
        revenues: Array.from({ length: count }, () => Math.floor(Math.random() * 20000 + 30000))
      }
    },
    async onRangeChange(type) {
      const range = type === 'order' ? this.orderRange : this.revenueRange
      try {
        const res = await getTrendData(range)
        if (res.data) {
          const data = {
            dates: res.data.dates || [],
            orderCounts: res.data.orderCounts || [],
            revenues: res.data.amounts || []
          }
          if (type === 'order') {
            this.trendData = data
            this.updateOrderTrendChart(data)
          } else {
            this.trendData.revenues = data.revenues
            this.updateRevenueChart(data)
          }
        }
      } catch (e) {
        const data = this.generateMockData(range)
        if (type === 'order') {
          this.updateOrderTrendChart(data)
        } else {
          this.updateRevenueChart(data)
        }
      }
    },
    updateOrderTrendChart(data) {
      const chart = this.charts[0]
      if (!chart) return
      chart.setOption({
        xAxis: { data: data.dates },
        series: [
          { data: data.orderCounts },
          { data: data.completedCounts }
        ]
      })
    },
    updateRevenueChart(data) {
      const chart = this.charts[1]
      if (!chart) return
      chart.setOption({
        xAxis: { data: data.dates },
        series: [{ data: data.revenues }]
      })
    },
    handleShortcut(item) {
      this.$router.push(item.path).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.panel-group {
  margin-bottom: 20px;

  .card-panel {
    height: 108px;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-radius: 4px;
    margin-bottom: 10px;

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }
      .icon-people { background: #40c9c6; }
      .icon-shopping { background: #36a3f7; }
      .icon-money { background: #f4516c; }
      .icon-skill { background: #34bfa3; }
    }

    .icon-people { color: #40c9c6; }
    .icon-shopping { color: #36a3f7; }
    .icon-money { color: #f4516c; }
    .icon-skill { color: #34bfa3; }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shortcut-row {
  .shortcut-card {
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 10px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .shortcut-icon {
      text-align: center;
      margin-bottom: 10px;

      .shortcut-svg {
        font-size: 36px;
        color: #409eff;
      }
    }

    .shortcut-info {
      text-align: center;

      .shortcut-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .shortcut-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  color: #fff;
  background: #909399;

  &.rank-1 { background: #f56c6c; }
  &.rank-2 { background: #e6a23c; }
  &.rank-3 { background: #409eff; }
}

.revenue-text {
  color: #f56c6c;
  font-weight: bold;
}

@media (max-width: 550px) {
  .panel-group .card-panel {
    .card-panel-description { display: none; }
    .card-panel-icon-wrapper {
      float: none !important;
      width: 100%;
      height: 100%;
      margin: 0 !important;
      .svg-icon {
        display: block;
        margin: 14px auto !important;
        float: none !important;
      }
    }
  }
}
</style>
