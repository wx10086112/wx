const util = require('../../utils/util')

Page({
  data: {
    favoriteList: [],
    isEmpty: true
  },

  onLoad() {
    this.loadFavorites()
  },

  onShow() {
    this.loadFavorites()
  },

  loadFavorites() {
    this.setData({
      favoriteList: [],
      isEmpty: true
    })
  },

  onItemTap(e) {
    const item = e.currentTarget.dataset.item
    if (item.type === 'product') {
      util.navigateTo(`/pages/product-detail/product-detail?id=${item.id}`)
    } else if (item.type === 'merchant') {
      util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${item.id}`)
    }
  },

  onRemove(e) {
    util.showToast('收藏功能需要后端支持')
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
