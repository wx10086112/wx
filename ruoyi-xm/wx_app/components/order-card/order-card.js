const util = require('../../utils/util')
const { toListThumbnailUrl } = require('../../utils/image-url')

Component({
  properties: {
    order: {
      type: Object,
      value: {},
      observer(order) {
        this.syncViewOrder(order)
      }
    },
    showActions: {
      type: Boolean,
      value: true
    }
  },

  data: {
    viewOrder: {}
  },

  methods: {
    syncViewOrder(order = {}) {
      const meta = util.getOrderStatusMeta(order.status)
      this.setData({
        viewOrder: {
          ...order,
          image: toListThumbnailUrl(order.image || order.coverImage || order.mainImage || ''),
          statusText: meta.text,
          statusClass: meta.class,
          payAmountText: ((order.payAmount || order.price || 0) / 100).toFixed(2)
        }
      })
    },

    onTap() {
      this.triggerEvent('click', { order: this.properties.order })
    },

    onCancel(e) {
      e.stopPropagation()
      this.triggerEvent('cancel', { order: this.properties.order })
    },

    onPay(e) {
      e.stopPropagation()
      this.triggerEvent('pay', { order: this.properties.order })
    },

    onRefund(e) {
      e.stopPropagation()
      this.triggerEvent('refund', { order: this.properties.order })
    },

    onRebuy(e) {
      e.stopPropagation()
      this.triggerEvent('rebuy', { order: this.properties.order })
    }
  }
})
