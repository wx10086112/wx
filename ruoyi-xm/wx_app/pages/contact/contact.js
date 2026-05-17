const templateService = require('../../services/template')
const util = require('../../utils/util')

Page({
  data: {
    brandInfo: {}
  },

  onLoad() {
    this.setData({
      brandInfo: templateService.getTemplateSection('brandInfo')
    })
  },

  callService() {
    const phone = this.data.brandInfo.servicePhone
    if (!phone) {
      util.showToast('暂无客服电话')
      return
    }
    wx.makePhoneCall({
      phoneNumber: phone,
      fail: () => {}
    })
  }
})
