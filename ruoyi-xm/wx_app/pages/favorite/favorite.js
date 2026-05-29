const mock = require('../../data/mock')
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
<<<<<<< HEAD
    const favoriteList = mock.favoriteList.map((item) => ({
      ...item,
      priceText: item.price ? (item.price / 100).toFixed(2) : ''
    }))
    this.setData({
      favoriteList,
      isEmpty: favoriteList.length === 0
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
    const item = e.currentTarget.dataset.item
    util.showModal('取消收藏', `确定取消收藏「${item.title}」吗？`).then((confirm) => {
      if (!confirm) return
      const idx = mock.favoriteList.findIndex((f) => f.id === item.id)
      if (idx > -1) {
        mock.favoriteList.splice(idx, 1)
      }
      this.loadFavorites()
      util.showToast('已取消收藏', 'success')
    })
=======
    this.setData({ favoriteList: [], isEmpty: true })
>>>>>>> 苏
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
