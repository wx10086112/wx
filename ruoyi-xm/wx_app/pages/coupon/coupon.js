const util = require('../../utils/util')
<<<<<<< HEAD

const couponStatusMap = {
  AVAILABLE: '可使用',
  USED: '已使用',
  EXPIRED: '已过期'
}
=======
>>>>>>> 苏

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
<<<<<<< HEAD
        ...item,
        statusText: couponStatusMap[item.status] || '不可用',
        thresholdText: (item.thresholdAmount / 100).toFixed(0),
        amountText: (item.amount / 100).toFixed(0),
        isAvailable: item.status === 'AVAILABLE'
      }))
    this.filterCoupons()
  },

  filterCoupons() {
    const tab = this.data.currentTab
    this.setData({
      couponList: this.data.allCoupons.filter((item) => item.status === tab)
    })
=======
    this.setData({ allCoupons: [], couponList: [] })
>>>>>>> 苏
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
