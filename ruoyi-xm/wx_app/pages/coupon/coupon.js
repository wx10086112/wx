const util = require('../../utils/util')
const api = require('../../api/index')

const couponStatusMap = {
  AVAILABLE: '可使用',
  USED: '已使用',
  EXPIRED: '已过期'
}

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
    this.loadCoupons()
  },

  loadCoupons() {
    api.getCouponList ? api.getCouponList().then((list) => {
      this.processCoupons(list || [])
    }).catch(() => {
      util.showToast('加载失败，请重试')
    }) : this.processCoupons([])
  },

  processCoupons(couponList) {
    const allCoupons = couponList.map((item) => ({
      ...item,
      statusText: couponStatusMap[item.status] || '不可用',
      thresholdText: (item.thresholdAmount / 100).toFixed(0),
      amountText: (item.amount / 100).toFixed(0),
      isAvailable: item.status === 'AVAILABLE'
    }))
    this.setData({ allCoupons })
    this.filterCoupons()
  },

  filterCoupons() {
    const tab = this.data.currentTab
    this.setData({
      couponList: this.data.allCoupons.filter((item) => item.status === tab)
    })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (this.data.currentTab === tab) return
    this.setData({ currentTab: tab })
    this.filterCoupons()
  },

  onUseCoupon(e) {
    util.switchTab('/pages/home/home')
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
