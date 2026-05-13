Component({
  properties: {
    product: {
      type: Object,
      value: {}
    }
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
