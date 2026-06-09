const templateService = require('../../services/template')
const util = require('../../utils/util')
const merchantApi = require('../../api/merchant')

const normalizeContactInfo = (contactInfo = {}) => {
  const businessHoursText = contactInfo.businessHoursText || ''
  const rightsRequestTips = contactInfo.rightsRequestTips || ''
  return {
    ...contactInfo,
    businessHoursText,
    rightsRequestTips,
    businessHoursDisplay: businessHoursText || '以商家门店设置为准',
    rightsRequestTipsDisplay: rightsRequestTips || '个人信息查阅、更正、删除等请求，可通过本页联系方式或订单详情商家联系方式提交'
  }
}

Page({
  data: {
    contactInfo: {
      operatorName: '',
      phone: '',
      email: '',
      address: '',
      businessHoursText: '',
      businessHoursDisplay: '以商家门店设置为准',
      rightsRequestTips: '',
      rightsRequestTipsDisplay: '个人信息查阅、更正、删除等请求，可通过本页联系方式或订单详情商家联系方式提交',
      configured: true,
      missingFields: []
    }
  },

  onLoad() {
    this.loadContactInfo()
  },

  loadContactInfo() {
    templateService.fetchTemplateConfig({ useRemote: true, force: true }).then(() => {
      const brandInfo = templateService.getTemplateSection('brandInfo')
      const templateContact = templateService.getTemplateSection('contactInfo')
      const fallbackInfo = {
        operatorName: templateContact.operatorName || brandInfo.name || '',
        phone: templateContact.servicePhone || brandInfo.servicePhone || '',
        email: templateContact.contactEmail || '',
        address: templateContact.contactAddress || '',
        businessHoursText: templateContact.businessHoursText || '',
        rightsRequestTips: templateContact.rightsRequestTips || '',
        configured: templateContact.configured !== false,
        missingFields: templateContact.missingFields || []
      }

      merchantApi.getMerchantList()
        .then((res) => {
          const merchant = (res.data || res || [])[0] || {}
          this.setData({
            contactInfo: normalizeContactInfo({
              ...fallbackInfo,
              operatorName: merchant.name || merchant.storeName || fallbackInfo.operatorName,
              phone: merchant.phone || fallbackInfo.phone,
              address: merchant.address || fallbackInfo.address,
              businessHoursText: merchant.businessHoursText || (merchant.businessHours ? `周一至周日 ${merchant.businessHours}` : fallbackInfo.businessHoursText)
            })
          })
        })
        .catch(() => {
          this.setData({ contactInfo: normalizeContactInfo(fallbackInfo) })
        })
    }).catch(() => {
      const brandInfo = templateService.getTemplateSection('brandInfo')
      const templateContact = templateService.getTemplateSection('contactInfo')
      this.setData({
        contactInfo: normalizeContactInfo({
          operatorName: templateContact.operatorName || brandInfo.name || '',
          phone: templateContact.servicePhone || brandInfo.servicePhone || '',
          email: templateContact.contactEmail || '',
          address: templateContact.contactAddress || '',
          businessHoursText: templateContact.businessHoursText || '',
          rightsRequestTips: templateContact.rightsRequestTips || '',
          configured: templateContact.configured !== false,
          missingFields: templateContact.missingFields || []
        })
      })
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
