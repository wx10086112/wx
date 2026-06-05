const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')

Page({
  data: {
    userInfo: {},
    initialNickName: '',
    initialAvatarUrl: '',
    nickName: '',
    avatarUrl: '',
    phone: '',
    saving: false,
    hasProfileChanged: false
  },

  onLoad() {
    const userInfo = app.globalData.userInfo || {}
    const nickName = userInfo.nickName || ''
    const avatarUrl = userInfo.avatarUrl || '/assets/images/avatar.svg'
    this.setData({
      userInfo,
      initialNickName: nickName,
      initialAvatarUrl: avatarUrl,
      nickName,
      avatarUrl,
      phone: userInfo.phone || ''
    })
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value }, () => this.syncProfileChanged())
  },

  onChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl
    if (avatarUrl) {
      this.setData({ avatarUrl }, () => this.syncProfileChanged())
    }
  },

  goAccountCancel() {
    util.navigateTo('/pages/account-cancel/account-cancel')
  },

  syncProfileChanged() {
    const { nickName, avatarUrl, initialNickName, initialAvatarUrl } = this.data
    const hasProfileChanged = (nickName || '').trim() !== (initialNickName || '').trim()
      || (avatarUrl || '') !== (initialAvatarUrl || '')
    if (hasProfileChanged !== this.data.hasProfileChanged) {
      this.setData({ hasProfileChanged })
    }
  },

  saveProfile() {
    if (!this.data.hasProfileChanged) return
    if (!agreement.assertAgreementAccepted()) return

    const { nickName, avatarUrl, userInfo } = this.data
    const trimmedName = (nickName || '').trim()

    if (!trimmedName) {
      util.showToast('请输入昵称')
      return
    }

    this.setData({ saving: true })
    util.showLoading('保存中...')

    userApi
      .updateUserInfo({
        userName: trimmedName,
        avatarUrl
      })
      .then(() => {
        this.applyProfileUpdate(trimmedName, avatarUrl)
      })
      .catch(() => {
        this.applyProfileUpdate(trimmedName, avatarUrl)
      })
  },

  applyProfileUpdate(nickName, avatarUrl) {
    const updatedInfo = {
      ...this.data.userInfo,
      nickName,
      avatarUrl
    }
    app.setLoginInfo(app.globalData.token, updatedInfo)
    this.setData({
      userInfo: updatedInfo,
      initialNickName: nickName,
      initialAvatarUrl: avatarUrl,
      saving: false,
      hasProfileChanged: false
    })
    util.hideLoading()
    util.showToast('保存成功', 'success')
    setTimeout(() => {
      util.navigateBack()
    }, 800)
  }
})
