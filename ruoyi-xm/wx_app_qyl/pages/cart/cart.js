const util = require('../../utils/util')
const cartStore = require('../../utils/cart')
const cartSync = require('../../utils/cart-sync')

Page({
  data: {
    cart: {},
    summary: {},
    hasItems: false
  },

  onLoad() {
    this.syncCart()
  },

  onShow() {
    this.refreshCart()
  },

  refreshCart() {
    return cartSync.refreshCart()
      .catch(() => cartStore.getCart())
      .then(() => this.syncCart())
  },

  syncCart() {
    const cart = cartStore.getCart()
    this.setData({
      cart,
      summary: cartStore.buildSummary(cart),
      hasItems: cart.items.length > 0
    })
  },

  changeQuantity(e) {
    const productId = Number(e.currentTarget.dataset.id)
    const delta = Number(e.currentTarget.dataset.delta)
    const item = this.data.cart.items.find((cartItem) => cartItem.productId === productId)
    if (!item) return
    const nextQuantity = item.quantity + delta
    if (nextQuantity < 1) {
      this.removeItem(e)
      return
    }
    if (item.stock > 0 && nextQuantity > item.stock) {
      util.showToast('已达到库存上限')
      return
    }
    cartStore.updateQuantity(productId, nextQuantity)
    this.syncCart()
  },

  removeItem(e) {
    const productId = Number(e.currentTarget.dataset.id)
    cartStore.removeItem(productId)
    this.syncCart()
  },

  clearCart() {
    util.showModal('清空购物车', '确认清空当前门店已选商品？').then((confirm) => {
      if (!confirm) return
      cartStore.clearCart()
      this.syncCart()
    })
  },

  goHome() {
    util.switchTab('/pages/home/home')
  },

  goCheckout() {
    if (!this.data.hasItems) {
      util.showToast('请先添加商品')
      return
    }
    util.navigateTo('/pages/checkout/checkout?cart=1')
  }
})
