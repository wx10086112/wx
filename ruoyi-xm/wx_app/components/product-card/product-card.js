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
      this.triggerEvent('buy', { product: this.properties.product })
    }
  }
})
