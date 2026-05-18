const util = require('../../utils/util')
const cartService = require('../../services/cart')
const app = getApp()

Page({
  data: {
    cartList: [],
    allSelected: true,
    selectedCount: 0,
    totalPrice: 0,
    totalOriginalPrice: 0,
    isEmpty: true
  },

  onShow() {
    this.loadCart()
  },

  onPullDownRefresh() {
    this.loadCart()
    wx.stopPullDownRefresh()
  },

  loadCart() {
    const cartList = cartService.getCartList().map((item) => ({
      ...item,
      priceText: (item.price / 100).toFixed(2),
      originalPriceText: (item.originalPrice / 100).toFixed(2)
    }))
    const allSelected = cartList.length > 0 && cartList.every((item) => item.selected)
    this.setData({ cartList, allSelected, isEmpty: cartList.length === 0 })
    this.calcTotal()
  },

  calcTotal() {
    const selected = this.data.cartList.filter((item) => item.selected)
    const totalPrice = selected.reduce((sum, item) => sum + item.price * item.quantity, 0)
    const totalOriginalPrice = selected.reduce((sum, item) => sum + (item.originalPrice || item.price) * item.quantity, 0)
    this.setData({
      selectedCount: selected.reduce((sum, item) => sum + item.quantity, 0),
      totalPrice,
      totalOriginalPrice,
      totalPriceText: (totalPrice / 100).toFixed(2)
    })
  },

  onItemSelect(e) {
    const productId = e.currentTarget.dataset.id
    cartService.toggleSelect(productId)
    this.loadCart()
  },

  onSelectAll() {
    const next = !this.data.allSelected
    cartService.selectAll(next)
    this.loadCart()
  },

  onQuantityMinus(e) {
    const { id, quantity } = e.currentTarget.dataset
    if (quantity <= 1) {
      util.showModal('删除商品', '确定将该商品从购物车移除？').then((confirm) => {
        if (!confirm) return
        cartService.removeFromCart(id)
        this.loadCart()
      })
      return
    }
    cartService.updateQuantity(id, quantity - 1)
    this.loadCart()
  },

  onQuantityPlus(e) {
    const { id, quantity, stock } = e.currentTarget.dataset
    if (quantity >= stock) {
      util.showToast('已达到库存上限')
      return
    }
    cartService.updateQuantity(id, quantity + 1)
    this.loadCart()
  },

  onDeleteItem(e) {
    const productId = e.currentTarget.dataset.id
    util.showModal('删除商品', '确定将该商品从购物车移除？').then((confirm) => {
      if (!confirm) return
      cartService.removeFromCart(productId)
      this.loadCart()
      util.showToast('已移除', 'success')
    })
  },

  onClearCart() {
    util.showModal('清空购物车', '确定清空购物车中的所有商品？').then((confirm) => {
      if (!confirm) return
      cartService.clearCart()
      this.loadCart()
    })
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  },

  goProductDetail(e) {
    const productId = e.currentTarget.dataset.id
    util.navigateTo(`/pages/product-detail/product-detail?id=${productId}`)
  },

  onCheckout() {
    if (!app.needLogin()) return
    const selected = this.data.cartList.filter((item) => item.selected)
    if (!selected.length) {
      util.showToast('请选择要结算的商品')
      return
    }
    const cartIds = selected.map((item) => item.productId).join(',')
    util.navigateTo(`/pages/checkout/checkout?cartIds=${cartIds}`)
  }
})
