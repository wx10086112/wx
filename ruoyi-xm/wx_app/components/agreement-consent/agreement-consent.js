const agreement = require('../../utils/agreement')
const templateService = require('../../services/template')

const AGREEMENT_CONFIGS = {
  user: {
    sheetTitle: '服务与协议',
    serviceTitle: '用户服务协议',
    privacyTitle: '隐私保护指引',
    tabs: [
      { key: 'service', title: '用户服务协议' },
      { key: 'privacy', title: '隐私保护指引' },
      { key: 'wechat', title: '微信隐私指引' }
    ]
  },
  merchant: {
    sheetTitle: '商家服务与协议',
    serviceTitle: '商家服务协议',
    privacyTitle: '商家隐私与数据保护指引',
    tabs: [
      { key: 'service', title: '商家服务协议' },
      { key: 'privacy', title: '商家隐私与数据保护指引' }
    ]
  }
}

const normalizeAudience = (audience = 'user') => {
  return AGREEMENT_CONFIGS[audience] ? audience : 'user'
}

const getAgreementConfig = (audience = 'user') => {
  return AGREEMENT_CONFIGS[normalizeAudience(audience)]
}

const buildPrivacyContactLine = (privacyInfo = {}) => {
  const parts = []
  if (privacyInfo.servicePhone) parts.push(`电话 ${privacyInfo.servicePhone}`)
  if (privacyInfo.contactEmail) parts.push(`邮箱 ${privacyInfo.contactEmail}`)
  if (privacyInfo.contactAddress) parts.push(`地址 ${privacyInfo.contactAddress}`)
  return parts.join('；')
}

const buildPrivacyRightsText = (privacyInfo = {}) => {
  return privacyInfo.rightsRequestTips || '如你对个人信息处理有查阅、复制、更正、删除、撤回授权、注销或投诉建议等需求，可优先通过小程序“联系客服”、订单详情页商家联系方式或微信小程序主体公示联系方式处理。我们将在合理期限内核验身份并处理你的请求。'
}

Component({
  properties: {
    audience: {
      type: String,
      value: 'user'
    },
    theme: {
      type: String,
      value: 'surface'
    },
    triggerVisible: {
      type: Boolean,
      value: true
    }
  },

  data: {
    audienceType: 'user',
    checked: false,
    showPanel: false,
    activeTab: 'service',
    tabs: AGREEMENT_CONFIGS.user.tabs,
    sheetTitle: AGREEMENT_CONFIGS.user.sheetTitle,
    serviceTitle: AGREEMENT_CONFIGS.user.serviceTitle,
    privacyTitle: AGREEMENT_CONFIGS.user.privacyTitle,
    privacyInfo: {},
    privacyContactLine: '',
    privacyRightsText: buildPrivacyRightsText()
  },

  observers: {
    audience(audience) {
      this.applyAudienceConfig(audience, true)
    }
  },

  lifetimes: {
    attached() {
      this.applyAudienceConfig(this.data.audience, true)
      this.refreshPrivacyInfo()
    }
  },

  pageLifetimes: {
    show() {
      this.syncCheckedState()
    }
  },

  methods: {
    applyAudienceConfig(audience, shouldSyncChecked = false) {
      const audienceType = normalizeAudience(audience)
      const config = getAgreementConfig(audienceType)
      const activeTab = config.tabs.some((item) => item.key === this.data.activeTab) ? this.data.activeTab : 'service'
      const nextData = {
        audienceType,
        activeTab,
        tabs: config.tabs,
        sheetTitle: config.sheetTitle,
        serviceTitle: config.serviceTitle,
        privacyTitle: config.privacyTitle
      }
      if (shouldSyncChecked) {
        nextData.checked = agreement.isAgreementAccepted(audienceType)
      }
      this.setData(nextData)
      if (shouldSyncChecked) {
        this.triggerEvent('change', { checked: nextData.checked })
      }
    },

    refreshPrivacyInfo() {
      const applyPrivacyInfo = () => {
        const privacyInfo = templateService.getTemplateSection('privacyInfo') || {}
        this.setData({
          privacyInfo,
          privacyContactLine: buildPrivacyContactLine(privacyInfo),
          privacyRightsText: buildPrivacyRightsText(privacyInfo)
        })
      }
      applyPrivacyInfo()
      templateService.fetchTemplateConfig({ useRemote: true, force: true }).then(() => {
        applyPrivacyInfo()
      }).catch(() => {})
    },

    syncCheckedState() {
      const audienceType = normalizeAudience(this.data.audience)
      const checked = agreement.isAgreementAccepted(audienceType)
      this.setData({
        audienceType,
        checked
      })
      this.triggerEvent('change', { checked })
    },

    openAgreementPanel(tab = 'service') {
      const targetTab = typeof tab === 'string' ? tab : (tab.currentTarget && tab.currentTarget.dataset.key) || 'service'
      const activeTab = this.data.tabs.some((item) => item.key === targetTab) ? targetTab : 'service'
      this.setData({
        showPanel: true,
        activeTab
      })
    },

    closeAgreementPanel() {
      this.setData({ showPanel: false })
    },

    switchTab(e) {
      const key = e.currentTarget.dataset.key
      if (!key || key === this.data.activeTab) return
      this.setData({ activeTab: key })
    },

    confirmAgreement() {
      agreement.acceptAgreement(this.data.audienceType)
      this.setData({
        checked: true,
        showPanel: false
      })
      this.triggerEvent('change', { checked: true })
      wx.showToast({
        title: '已同意相关协议',
        icon: 'success',
        duration: 1200
      })
    },

    openWechatPrivacy() {
      if (!wx.openPrivacyContract) {
        wx.showToast({
          title: '当前微信版本暂不支持打开',
          icon: 'none'
        })
        return
      }
      wx.openPrivacyContract({
        fail: () => {
          wx.showToast({
            title: '微信隐私指引打开失败',
            icon: 'none'
          })
        }
      })
    },

    preventMove() {}
  }
})
