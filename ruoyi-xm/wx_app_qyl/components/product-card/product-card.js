const util = require('../../utils/util')

Component({
  properties: {
    product: {
      type: Object,
      value: {}
    }
  },

  observers: {
    product(nextProduct = {}) {
      this.setData({
        imageCropStyle: util.buildImageCropStyle(nextProduct.imageCrop)
      })
    }
  },

  data: {
    imageCropStyle: util.buildImageCropStyle()
  },

  methods: {
    onTap() {
      this.triggerEvent('click', { product: this.properties.product })
    },

    onBuy() {
      const product = this.properties.product || {}
      if (product.soldOut || Number(product.stock || 0) <= 0) {
        util.showToast('当前商品已售罄')
        return
      }
      this.triggerEvent('buy', { product: this.properties.product })
    }
  }
})
