const app = getApp()

const tabPageMap = {
  workbench: '/pages/merchant/workbench/workbench',
  order: '/pages/merchant/order/order',
  verify: '/pages/merchant/verify/verify',
  goods: '/pages/merchant/goods/goods',
  mine: '/pages/merchant/mine/mine'
}

Page({
  onLoad(options = {}) {
    this.redirectToMerchantPage(options.tab)
  },

  onShow() {
    this.redirectToMerchantPage()
  },

  redirectToMerchantPage(tab = 'workbench') {
    if (!app.needMerchantLogin()) return

    const target = tabPageMap[tab] || tabPageMap.workbench
    wx.redirectTo({ url: target })
  }
})
