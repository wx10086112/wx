const app = getApp()
const util = require('../../utils/util')
const templateService = require('../../services/template')
const merchantApi = require('../../api/merchant')

const PANEL_CONTENT = {
  collection: {
    title: '个人信息收集清单',
    groups: [
      {
        title: '微信授权信息',
        items: [
          '微信登录标识：用于登录、识别账号、查询订单和保障账号安全。',
          '昵称、头像：通过微信头像昵称能力获取，用于个人资料展示和订单服务识别。',
          '手机号：通过微信手机号能力获取，用于账号绑定、订单联系、售后和退款处理。',
          '位置信息：通过微信定位能力获取，用于计算门店距离和辅助导航；拒绝授权不影响浏览商品。'
        ]
      },
      {
        title: '交易服务信息',
        items: [
          '订单信息：包括商品、数量、订单号、金额、优惠、状态、核销码和售后记录。',
          '支付信息：包括支付金额、支付状态、微信支付返回的交易处理结果。',
          '客服与售后信息：包括退款申请、售后进度、联系记录和必要的问题说明。'
        ]
      }
    ]
  },
  sharing: {
    title: '第三方信息共享清单',
    groups: [
      {
        title: '微信支付',
        items: [
          '共享目的：完成订单支付、退款、交易查询和财务对账。',
          '共享信息：订单号、支付金额、支付状态、用户微信标识等必要交易信息。'
        ]
      },
      {
        title: '当前订单对应商户',
        items: [
          '共享目的：完成到店履约、核销、售后、退款审核和订单争议处理。',
          '共享信息：订单号、商品信息、订单状态、联系人或脱敏手机号、核销和售后所需信息。'
        ]
      },
      {
        title: '微信小程序基础能力',
        items: [
          '使用目的：完成手机号授权、定位授权、头像昵称选择、订阅消息授权、隐私授权弹窗和隐私指引展示。',
          '涉及信息：手机号、位置信息、头像昵称、订阅消息授权状态等你主动授权或填写的信息。',
          '处理规则：以微信小程序平台和本小程序隐私保护指引共同约定为准。'
        ]
      }
    ]
  },
  summary: {
    title: '隐私政策摘要',
    groups: [
      {
        title: '最小必要',
        items: [
          '我们仅在登录、下单、支付、退款、核销、客服、门店距离展示等必要场景处理信息。',
          '你可以拒绝非必要授权；拒绝定位后，距离展示和导航相关能力可能不可用。'
        ]
      },
      {
        title: '你的权利',
        items: [
          '你可以在个人资料中管理昵称和头像。',
          '你可以在微信小程序设置里管理定位、手机号等授权。',
          '你可以通过联系客服或订单详情中的商家联系方式提出查阅、更正、删除等请求。'
        ]
      }
    ]
  }
}

const normalizeLicenseList = (merchant = {}) => {
  const licenseImage = merchant.licenseImage || merchant.businessLicenseUrl || ''
  const foodLicenseImage = merchant.foodLicenseImage || merchant.foodPermitUrl || merchant.foodLicenseUrl || ''
  const permitImage = merchant.serviceLicenseImage || merchant.industryLicenseUrl || ''
  const licenseNo = merchant.businessLicenseNo || merchant.licenseNo || ''
  const permitNo = merchant.foodLicenseNo || merchant.permitNo || ''

  return [
    {
      label: '营业执照',
      status: licenseImage || licenseNo ? '已公示' : '暂未公示',
      isPublic: !!(licenseImage || licenseNo),
      image: licenseImage,
      number: licenseNo
    },
    {
      label: '行业许可证',
      status: foodLicenseImage || permitImage || permitNo ? '已公示' : '按类目公示',
      isPublic: !!(foodLicenseImage || permitImage || permitNo),
      image: foodLicenseImage || permitImage,
      number: permitNo
    }
  ]
}

Page({
  data: {
    versionText: 'v1.1.0',
    isLoggedIn: false,
    merchantName: '当前商户',
    privacyInfo: {},
    licenseList: [],
    activePanel: '',
    panelData: {},
    showAgreementPanel: false
  },

  onLoad() {
    this.initStaticInfo()
    this.loadMerchantQualification()
  },

  onShow() {
    this.setData({
      isLoggedIn: !!app.globalData.isLoggedIn
    })
  },

  initStaticInfo() {
    const templateMeta = templateService.getTemplateSection('templateMeta') || {}
    const brandInfo = templateService.getTemplateSection('brandInfo') || {}
    const privacyInfo = templateService.getTemplateSection('privacyInfo') || {}
    this.setData({
      versionText: `v${templateMeta.version || '1.1.0'}`,
      merchantName: brandInfo.name || '当前商户',
      privacyInfo
    })
    templateService.fetchTemplateConfig({ useRemote: true, force: true }).then(() => {
      const remoteTemplateMeta = templateService.getTemplateSection('templateMeta') || {}
      const remoteBrandInfo = templateService.getTemplateSection('brandInfo') || {}
      const remotePrivacyInfo = templateService.getTemplateSection('privacyInfo') || {}
      this.setData({
        versionText: `v${remoteTemplateMeta.version || '1.1.0'}`,
        merchantName: remoteBrandInfo.name || this.data.merchantName,
        privacyInfo: remotePrivacyInfo
      })
    }).catch(() => {})
  },

  buildPanelData(type) {
    const basePanel = PANEL_CONTENT[type]
    if (!basePanel) return null
    const panelData = JSON.parse(JSON.stringify(basePanel))
    if (type !== 'summary') {
      return panelData
    }

    const privacyInfo = this.data.privacyInfo || {}
    const contactItems = [
      privacyInfo.operatorName ? `个人信息处理者：${privacyInfo.operatorName}` : '',
      privacyInfo.servicePhone ? `客服电话：${privacyInfo.servicePhone}` : '',
      privacyInfo.contactEmail ? `联系邮箱：${privacyInfo.contactEmail}` : '',
      privacyInfo.contactAddress ? `联系地址：${privacyInfo.contactAddress}` : '',
      privacyInfo.rightsRequestTips || ''
    ].filter(Boolean)

    if (privacyInfo.configured === false && privacyInfo.missingFields && privacyInfo.missingFields.length) {
      contactItems.push(`后台待补充：${privacyInfo.missingFields.join('、')}`)
    }

    if (contactItems.length) {
      panelData.groups.push({
        title: '联系方式与个人信息权利请求',
        items: contactItems
      })
    }
    return panelData
  },

  loadMerchantQualification() {
    merchantApi
      .getMerchantList()
      .then((res) => {
        const merchant = (res.data || res || [])[0] || {}
        this.setData({
          merchantName: merchant.name || merchant.storeName || this.data.merchantName,
          licenseList: normalizeLicenseList(merchant)
        })
      })
      .catch(() => {
        this.setData({
          licenseList: normalizeLicenseList({})
        })
      })
  },

  goProfile() {
    util.navigateTo('/pages/profile-edit/profile-edit')
  },

  openAgreementSelector() {
    this.setData({ showAgreementPanel: true })
  },

  closeAgreementSelector() {
    this.setData({ showAgreementPanel: false })
  },

  openWechatPrivacy() {
    this.closeAgreementSelector()
    if (!wx.openPrivacyContract) {
      util.showToast('当前微信版本暂不支持打开')
      return
    }
    wx.openPrivacyContract({
      fail: () => {
        util.showToast('微信隐私指引打开失败')
      }
    })
  },

  openPanel(e) {
    const type = e.currentTarget.dataset.type
    const panelData = this.buildPanelData(type)
    if (!panelData) return
    this.setData({
      activePanel: type,
      panelData
    })
  },

  openQualificationPanel() {
    this.setData({
      activePanel: 'qualification',
      panelData: {
        title: '商家资质信息'
      }
    })
  },

  closePanel() {
    this.setData({
      activePanel: '',
      panelData: {}
    })
  },

  previewLicense(e) {
    const image = e.currentTarget.dataset.image
    if (!image) {
      util.showToast('暂无公示图片')
      return
    }
    wx.previewImage({
      urls: [image],
      current: image
    })
  },

  showVersionInfo() {
    util.showToast(`当前版本 ${this.data.versionText}`)
  },

  handleLogout() {
    util.showModal('退出登录', '确定退出当前账号吗？').then((confirm) => {
      if (!confirm) return
      app.clearLoginInfo()
      util.showToast('已退出登录', 'success')
      wx.redirectTo({ url: '/pages/login/login' })
    })
  },

  preventMove() {}
})
