const util = require('../../utils/util')

Page({
  data: {
    tabs: [
      { label: '可使用', value: 'AVAILABLE' },
      { label: '已使用', value: 'USED' },
      { label: '已过期', value: 'EXPIRED' }
    ],
    currentTab: 'AVAILABLE',
    couponList: [],
    allCoupons: []
  },

  onLoad() {
    this.setData({ allCoupons: [], couponList: [] })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (this.data.currentTab === tab) return
    this.setData({ currentTab: tab })
  },

  onUseCoupon() {
    util.switchTab('/pages/home/home')
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
