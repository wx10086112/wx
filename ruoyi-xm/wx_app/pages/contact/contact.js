const templateService = require('../../services/template')
const util = require('../../utils/util')
const merchantApi = require('../../api/merchant')

Page({
  data: {
    contactInfo: {
      phone: '',
      businessHoursText: ''
    }
  },

  onLoad() {
    this.loadContactInfo()
  },

  loadContactInfo() {
    const brandInfo = templateService.getTemplateSection('brandInfo')
    const fallbackInfo = {
      phone: brandInfo.servicePhone || '',
      businessHoursText: ''
    }

    merchantApi.getMerchantList()
      .then((res) => {
        const merchant = (res.data || res || [])[0] || {}
        this.setData({
          contactInfo: {
            phone: merchant.phone || fallbackInfo.phone,
            businessHoursText: merchant.businessHoursText || (merchant.businessHours ? `周一至周日 ${merchant.businessHours}` : fallbackInfo.businessHoursText)
          }
        })
      })
      .catch(() => {
        this.setData({ contactInfo: fallbackInfo })
      })
  },

  callService() {
    const phone = this.data.contactInfo.phone
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
