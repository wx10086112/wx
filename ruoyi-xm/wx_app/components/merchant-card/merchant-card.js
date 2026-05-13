Component({
  properties: {
    merchant: {
      type: Object,
      value: {}
    }
  },

  methods: {
    onTap() {
      this.triggerEvent('click', { merchant: this.properties.merchant })
    }
  }
})
