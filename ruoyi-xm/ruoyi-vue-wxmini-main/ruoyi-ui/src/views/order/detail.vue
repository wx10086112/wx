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
              <el-button v-if="order.status === 1" type="success" size="small">标记已核销</el-button>
              <el-button v-if="order.status === 2" type="success" size="small">标记已完成</el-button>
            </div>
          </div>
        </el-card>

        <!-- 商品信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>商品信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商品名称">{{ orderItemName }}</el-descriptions-item>
            <el-descriptions-item label="商家名称">{{ order.merchantName || order.merchantId }}</el-descriptions-item>
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 支付信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>支付信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="实付金额">
              <span class="amount text-primary">¥{{ (order.payAmount || 0).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="优惠金额">
              <span class="text-danger">-¥{{ ((order.totalAmount || 0) - (order.payAmount || 0)).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ order.payTime || '未支付' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 用户信息 -->
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>用户信息</span></div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ order.userName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card shadow="never" class="info-card">
          <div slot="header"><span>订单历史</span></div>
          <el-timeline v-if="history.length">
            <el-timeline-item
              v-for="item in history"
              :key="item.id || item.changeTime || item.action"
              :timestamp="item.changeTime"
              :type="historyTimelineType(item.action)"
              placement="top"
            >
              <div class="history-title">{{ historyActionText(item.action) }}</div>
              <div class="history-meta">
                <span>状态：{{ statusText(item.fromStatus) }} → {{ statusText(item.toStatus) }}</span>
                <span v-if="item.operatorName">操作人：{{ item.operatorName }}</span>
                <span v-if="item.source">来源：{{ item.source }}</span>
              </div>
              <div v-if="item.remark" class="history-remark">说明：{{ item.remark }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无订单历史" />
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
      items: [],
      history: [],
      historyActionMap: {
        CREATE: '创建订单',
        PAY_SUCCESS: '支付成功',
        CANCEL: '取消订单',
        WRITE_OFF_COMPLETE: '核销完成',
        REFUND_APPLY: '申请退款',
        REFUND_APPROVE: '同意退款',
        REFUND_REJECT: '拒绝退款',
        REFUND_SUCCESS: '退款成功',
        MERCHANT_ACCEPT: '商家接单',
        MERCHANT_REJECT: '商家拒单',
        MERCHANT_CANCEL: '商家取消',
        ADMIN_UPDATE_STATUS: '后台改状态'
      },
      statusMap: {
        0: { text: '待付款', type: 'warning' },
        1: { text: '已付款', type: '' },
        2: { text: '已核销', type: 'success' },
        3: { text: '已完成', type: 'success' },
        4: { text: '已退款', type: 'info' },
        5: { text: '已取消', type: 'danger' }
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
        3: 'el-icon-circle-check',
        4: 'el-icon-refresh-left',
        5: 'el-icon-circle-close'
      }
      return icons[this.order.status] || 'el-icon-info'
    },
    statusDesc() {
      if (!this.order) return ''
      const descs = {
        0: '用户已下单，等待支付',
        1: '用户已支付，等待核销',
        2: '商家已核销，等待完成',
        3: '订单已完成',
        4: '订单已退款',
        5: '订单已取消'
      }
      return descs[this.order.status] || ''
    },
    orderItemName() {
      if (!this.items || !this.items.length) return '-'
      const names = this.items.map(i => i.productName || i.skuName || '').filter(Boolean)
      return names.join('、') || '-'
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
        this.order = res.data.order
        this.items = res.data.items || []
        this.history = res.data.history || []
      } finally {
        this.loading = false
      }
    },
    statusText(status) {
      return this.statusMap[status] ? this.statusMap[status].text : '未知'
    },
    historyActionText(action) {
      return this.historyActionMap[action] || action || '订单更新'
    },
    historyTimelineType(action) {
      const dangerActions = ['CANCEL', 'MERCHANT_REJECT', 'MERCHANT_CANCEL', 'REFUND_REJECT']
      return dangerActions.includes(action) ? 'danger' : 'success'
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
.history-title {
  font-weight: 600;
  color: #303133;
}
.history-meta {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
}
.history-meta span {
  margin-right: 16px;
}
.history-remark {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
</style>
