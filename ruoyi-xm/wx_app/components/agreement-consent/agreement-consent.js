const agreement = require('../../utils/agreement')

const AGREEMENT_TABS = [
  { key: 'service', title: '用户服务协议' },
  { key: 'privacy', title: '隐私保护指引' },
  { key: 'wechat', title: '微信隐私指引' }
]

Component({
  properties: {
    theme: {
      type: String,
      value: 'surface'
    }
  },

  data: {
    checked: false,
    showPanel: false,
    activeTab: 'service',
    tabs: AGREEMENT_TABS
  },

  lifetimes: {
    attached() {
      this.syncCheckedState()
    }
  },

  pageLifetimes: {
    show() {
      this.syncCheckedState()
    }
  },

  methods: {
    syncCheckedState() {
      const checked = agreement.isAgreementAccepted()
      this.setData({ checked })
      this.triggerEvent('change', { checked })
    },

    openAgreementPanel() {
      this.setData({
        showPanel: true,
        activeTab: 'service'
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
      agreement.acceptAgreement()
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
