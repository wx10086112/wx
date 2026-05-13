<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header" class="card-header">
        <span>订单详情</span>
        <el-button size="small" @click="goBack">返回</el-button>
      </div>

      <template v-if="order">
        <!-- 订单状态 -->
        <el-card shadow="never" class="status-card">
          <div class="status-row">
            <i :class="statusIcon" class="status-icon" />
            <div>
              <div class="status-text">{{ statusMap[order.status] ? statusMap[order.status].text : '未知' }}</div>
              <div class="status-desc">{{ statusDesc }}</div>
            </div>
            <div class="action-buttons">
              <el-button v-if="order.status === 0" type="primary" size="small">标记已付款</el-button>
              <el-button v-if="order.status === 1" type="success" size="small">标记已完成</el-button>
              <el-button v-if="order.status === 4" type="warning" size="small">处理售后</el-button>
              <el-button v-if="order.status === 5" type="danger" size="small">处理异常</el-button>
            </div>
          </div>
        </el-card>

        <!-- 商品信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>商品信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商品名称">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="商家名称">{{ order.merchantId }}</el-descriptions-item>
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 支付信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>支付信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ order.totalAmount.toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="实付金额">
              <span class="amount text-primary">¥{{ order.payAmount.toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="优惠金额">
              <span class="text-danger">-¥{{ (order.totalAmount - order.payAmount).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ order.payTime || '未支付' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 用户信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>用户信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ order.userId }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ order.id }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </template>
    </el-card>
  </div>
</template>

<script>
import { getOrderDetail } from '@/api/order'

export default {
  name: 'OrderDetail',
  data() {
    return {
      loading: false,
      order: null,
      statusMap: {
        0: { text: '待付款', type: 'warning' },
        1: { text: '已付款', type: '' },
        2: { text: '已完成', type: 'success' },
        3: { text: '已退款', type: 'info' },
        4: { text: '售后中', type: 'danger' },
        5: { text: '异常', type: 'danger' }
      }
    }
  },
  computed: {
    statusIcon() {
      if (!this.order) return ''
      const icons = {
        0: 'el-icon-time',
        1: 'el-icon-success',
        2: 'el-icon-circle-check',
        3: 'el-icon-refresh-left',
        4: 'el-icon-warning',
        5: 'el-icon-error'
      }
      return icons[this.order.status] || 'el-icon-info'
    },
    statusDesc() {
      if (!this.order) return ''
      const descs = {
        0: '用户已下单，等待支付',
        1: '用户已支付，等待商家服务',
        2: '订单已完成',
        3: '订单已退款',
        4: '订单正在售后处理中',
        5: '订单存在异常，需要处理'
      }
      return descs[this.order.status] || ''
    }
  },
  created() {
    this.fetchDetail()
  },
  methods: {
    async fetchDetail() {
      this.loading = true
      try {
        const id = Number(this.$route.params.id)
        const res = await getOrderDetail(id)
        this.order = res.data
      } finally {
        this.loading = false
      }
    },
    goBack() {
      this.$router.go(-1)
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
.status-card {
  margin-bottom: 16px;
  background: #f5f7fa;
}
.status-card >>> .el-card__body {
  padding: 20px 24px;
}
.status-row {
  display: flex;
  align-items: center;
}
.status-icon {
  font-size: 48px;
  margin-right: 20px;
  color: #409EFF;
}
.status-text {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}
.status-desc {
  font-size: 13px;
  color: #909399;
}
.action-buttons {
  margin-left: auto;
}
.info-card {
  margin-bottom: 16px;
}
.totalAmount {
  font-weight: 600;
  font-size: 15px;
}
.text-primary {
  color: #409EFF;
}
.text-danger {
  color: #F56C6C;
}
</style>
