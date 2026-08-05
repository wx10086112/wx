const util = require('../../utils/util')
const { toListThumbnailUrl } = require('../../utils/image-url')
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

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
    normalizeItems(order = {}) {
      const sourceItems = Array.isArray(order.items) && order.items.length ? order.items : [order]
      return sourceItems.map((item) => {
        const price = Number(item.price || item.unitPrice || order.price || 0)
        const quantity = Math.max(1, Number(item.quantity || 1))
        return {
          ...item,
          productId: item.productId || item.id || order.productId,
          title: item.title || item.productName || item.name || order.title || order.productName || '',
          image: toListThumbnailUrl(item.image || item.coverImage || item.mainImage || order.image || DEFAULT_PRODUCT_IMAGE),
          quantity,
          price,
          priceText: (price / 100).toFixed(2)
        }
      })
    },

    syncViewOrder(order = {}) {
      const meta = util.getOrderStatusMeta(order.status)
      const items = this.normalizeItems(order)
      const firstItem = items[0] || {}
      const totalQuantity = items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
      this.setData({
        viewOrder: {
          ...order,
          items,
          image: firstItem.image || toListThumbnailUrl(order.image || order.coverImage || order.mainImage || DEFAULT_PRODUCT_IMAGE),
          title: items.length > 1 ? `${firstItem.title}等${items.length}件商品` : (firstItem.title || order.title),
          quantityText: items.length > 1 ? `共${items.length}种 ${totalQuantity}件` : `数量 x${totalQuantity || order.quantity || 1}`,
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
