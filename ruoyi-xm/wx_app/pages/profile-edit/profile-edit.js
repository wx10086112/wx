const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')

Page({
  data: {
    userInfo: {},
    nickName: '',
    avatarUrl: '',
    phone: '',
    saving: false
  },

  onLoad() {
    const userInfo = app.globalData.userInfo || {}
    this.setData({
      userInfo,
      nickName: userInfo.nickName || '',
      avatarUrl: userInfo.avatarUrl || '/assets/images/avatar.png',
      phone: userInfo.phone || ''
    })
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value })
  },

  onChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl
    if (avatarUrl) {
      this.setData({ avatarUrl })
    }
  },

  saveProfile() {
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
        const updatedInfo = {
          ...userInfo,
          nickName: trimmedName,
          avatarUrl
        }
        app.setLoginInfo(app.globalData.token, updatedInfo)
        this.setData({ userInfo: updatedInfo, saving: false })
        util.hideLoading()
        util.showToast('保存成功', 'success')
        setTimeout(() => {
          util.navigateBack()
        }, 800)
      })
      .catch(() => {
        const updatedInfo = {
          ...userInfo,
          nickName: trimmedName,
          avatarUrl
        }
        app.setLoginInfo(app.globalData.token, updatedInfo)
        this.setData({ userInfo: updatedInfo, saving: false })
        util.hideLoading()
        util.showToast('保存成功', 'success')
        setTimeout(() => {
          util.navigateBack()
        }, 800)
      })
  }
})
