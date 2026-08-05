const app = getApp()
const util = require('../../utils/util')
const userApi = require('../../api/user')

Page({
  data: {
    submitting: false
  },

  submitCancelAccount() {
    if (this.data.submitting) return

    util
      .showModal('确认注销账号？', '注销后账号资料将删除或匿名化，当前登录状态会立即失效，7 天内不能使用同一微信身份重新注册。')
      .then((confirm) => {
        if (!confirm) return
        this.doCancelAccount()
      })
  },

  doCancelAccount() {
    this.setData({ submitting: true })
    util.showLoading('处理中...')

    userApi
      .cancelAccount()
      .then(() => {
        util.hideLoading()
        app.clearLoginInfo()
        util.showToast('账号已注销', 'success')
        setTimeout(() => {
          wx.switchTab({ url: '/pages/mine/mine' })
        }, 800)
      })
      .catch(() => {
        util.hideLoading()
        this.setData({ submitting: false })
      })
  }
})
