const util = require('../../../utils/merchant-util')

const app = getApp()

Page({
  data: {
    staffUser: {},
    storeInfo: {},
    operationList: [],
    managementList: [],
    merchantNavList: util.getMerchantNavList('mine')
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    const staffUser = app.globalData.staffUser || {}
    const permissionCodes = app.globalData.permissionCodes || []
    const storeInfo = util.getStoreInfo()

    this.setData({
      staffUser,
      storeInfo,
      operationList: this.buildOperationList(permissionCodes),
      managementList: this.buildManagementList(permissionCodes)
    })
  },

  hasAnyPermission(permissionCodes, requiredCodes = []) {
    if (!requiredCodes.length) return true
    return requiredCodes.some((code) => permissionCodes.includes(code))
  },

  buildOperationList(permissionCodes = []) {
    return [
      {
        title: '核销记录',
        desc: '查看到店核销结果与失败原因',
        url: '/pages/merchant/verify-records/verify-records',
        permissionCodes: ['verify.record', 'verify.scan', 'verify.manual']
      },
      {
        title: '订单管理',
        desc: '处理待核销、退款与取消订单',
        url: '/pages/merchant/order/order',
        isTab: true,
        permissionCodes: ['order.manage']
      },
      {
        title: '商品套餐',
        desc: '维护团购套餐、库存与上下架',
        url: '/pages/merchant/goods/goods',
        isTab: true,
        permissionCodes: ['goods.manage']
      }
    ].filter((item) => this.hasAnyPermission(permissionCodes, item.permissionCodes))
  },

  buildManagementList(permissionCodes = []) {
    return [
      {
        title: '门店资料',
        desc: '营业时间、地址、标签与展示信息',
        url: '/pages/merchant/store/store',
        permissionCodes: ['store.manage']
      },
      {
        title: '员工账号',
        desc: '店长/店员账号与权限分配',
        url: '/pages/merchant/staff/staff',
        permissionCodes: ['staff.manage']
      },
      {
        title: '结算中心',
        desc: '自动到账概览、到账记录与结算流水',
        url: '/pages/merchant/finance/finance',
        permissionCodes: ['finance.manage']
      }
    ].filter((item) => this.hasAnyPermission(permissionCodes, item.permissionCodes))
  },

  goMenu(e) {
    const { url, istab } = e.currentTarget.dataset
    if (istab) {
      util.switchTab(url)
      return
    }
    util.navigateTo(url)
  },

  logout() {
    app.clearMerchantLoginInfo()
    wx.redirectTo({
      url: '/pages/merchant/login/login'
    })
  },

  goMerchantTab(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
