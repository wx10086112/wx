const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')
const requestUtil = require('../../utils/request')
const { toStorageImageUrl } = require('../../utils/image-url')
const DEFAULT_USER_NAME = '微信用户'
const DEFAULT_AVATAR_URL = '/assets/images/avatar.svg'

const getWxErrorMessage = (detail = {}) => {
  const message = detail.errMsg || detail.err_msg || ''
  if (/privacy agreement/i.test(message)) {
    return '请先在小程序隐私指引中声明头像、昵称、手机号用途'
  }
  return message
}

const isTempAvatarPath = (url = '') => {
  return /^wxfile:\/\//i.test(url)
    || /^https?:\/\/tmp\//i.test(url)
    || /^http:\/\/usr\//i.test(url)
    || url.includes('/tmp/')
}

const normalizeReturnUrl = (value = '') => {
  if (!value) return ''
  const decoded = decodeURIComponent(value)
  if (!/^\/pages\//.test(decoded) || /:\/\//.test(decoded)) {
    return ''
  }
  return decoded
}

Page({
  data: {
    userInfo: {},
    initialNickName: '',
    initialAvatarUrl: '',
    nickName: '',
    avatarUrl: '',
    phone: '',
    onboardingMode: false,
    returnUrl: '',
    saving: false,
    phoneBinding: false,
    nickNameFocus: false,
    hasProfileChanged: false
  },

  onLoad(options = {}) {
    const userInfo = app.globalData.userInfo || {}
    const nickName = userInfo.nickName || ''
    const avatarUrl = userInfo.avatarUrl || DEFAULT_AVATAR_URL
    this.setData({
      userInfo,
      initialNickName: nickName,
      initialAvatarUrl: avatarUrl,
      nickName,
      avatarUrl,
      phone: userInfo.phone || '',
      onboardingMode: options.from === 'login',
      returnUrl: normalizeReturnUrl(options.returnUrl)
    })
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value, nickNameFocus: false }, () => this.syncProfileChanged())
  },

  onNickNameBlur() {
    if (this.data.nickNameFocus) {
      this.setData({ nickNameFocus: false })
    }
  },

  onChooseAvatar(e) {
    const avatarUrl = e && e.detail && e.detail.avatarUrl
    if (!avatarUrl) {
      console.warn('[profile-edit] chooseAvatar empty detail:', e && e.detail)
      util.showToast('头像授权失败')
      return
    }
    const shouldFocusNickName = !this.data.nickName || this.data.nickName === DEFAULT_USER_NAME
    this.setData({
      avatarUrl,
      nickNameFocus: shouldFocusNickName
    }, () => this.syncProfileChanged())
  },

  onOpenCapabilityError(e) {
    const detail = (e && e.detail) || {}
    console.warn('[profile-edit] open capability failed:', detail)
    util.showToast(getWxErrorMessage(detail) || '微信授权能力调用失败')
  },

  onGetPhoneNumber(e) {
    if (this.data.phoneBinding) return
    const detail = (e && e.detail) || {}
    if (!detail.code) {
      console.warn('[profile-edit] getPhoneNumber failed:', detail)
      util.showToast(getWxErrorMessage(detail) || '请授权手机号后继续')
      return
    }

    this.setData({ phoneBinding: true })
    util.showLoading('绑定中...')
    userApi.bindPhoneByCode(detail.code)
      .then((res = {}) => {
        const payload = res.data || res || {}
        const phone = payload.phone || ''
        if (!phone) {
          throw new Error('手机号绑定返回异常')
        }
        this.applyPhoneUpdate(phone)
      })
      .catch((err = {}) => {
        util.showToast(err.message || err.msg || '手机号绑定失败')
      })
      .finally(() => {
        util.hideLoading()
        this.setData({ phoneBinding: false })
      })
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

  saveProfile(options = {}) {
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

    this.uploadAvatarIfNeeded(avatarUrl)
      .then((finalAvatarUrl) => userApi.updateUserInfo({
        userName: trimmedName,
        avatarUrl: toStorageImageUrl(finalAvatarUrl)
      }).then((res = {}) => ({ res, finalAvatarUrl })))
      .then(({ res, finalAvatarUrl }) => {
        const payload = res.data || res || {}
        this.applyProfileUpdate(payload.userName || trimmedName, payload.avatarUrl || finalAvatarUrl, options.goMine === true)
      })
      .catch((err = {}) => {
        this.setData({ saving: false })
        util.hideLoading()
        util.showToast(err.message || err.msg || '保存失败，请重试')
      })
  },

  uploadAvatarIfNeeded(avatarUrl) {
    if (!avatarUrl || avatarUrl === this.data.initialAvatarUrl || !isTempAvatarPath(avatarUrl)) {
      return Promise.resolve(avatarUrl)
    }
    return requestUtil.uploadFile(avatarUrl).then((res = {}) => {
      const payload = res.data || res || {}
      if (!payload.url) {
        throw new Error('头像上传返回异常')
      }
      return toStorageImageUrl(payload.url)
    })
  },

  goReturnTarget(goMine = false) {
    const returnUrl = this.data.returnUrl
    if (returnUrl) {
      const pages = getCurrentPages()
      const previousPage = pages.length > 1 ? pages[pages.length - 2] : null
      const previousRoute = previousPage ? `/${previousPage.route}` : ''
      const returnRoute = returnUrl.split('?')[0]
      if (previousRoute === returnRoute) {
        util.navigateBack()
      } else {
        wx.redirectTo({ url: returnUrl })
      }
      return
    }
    if (goMine) {
      wx.switchTab({ url: '/pages/mine/mine' })
    } else {
      util.navigateBack()
    }
  },

  applyProfileUpdate(nickName, avatarUrl, goMine = false) {
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
      this.goReturnTarget(goMine)
    }, 800)
  },

  applyPhoneUpdate(phone) {
    const updatedInfo = {
      ...this.data.userInfo,
      phone
    }
    app.setLoginInfo(app.globalData.token, updatedInfo)
    this.setData({
      userInfo: updatedInfo,
      phone
    })
    util.showToast('绑定成功', 'success')
    if (this.data.returnUrl) {
      setTimeout(() => {
        this.goReturnTarget()
      }, 700)
    }
  },

  isCurrentProfileComplete() {
    const nickName = String(this.data.nickName || '').trim()
    const avatarUrl = String(this.data.avatarUrl || '').trim()
    const phone = String(this.data.phone || '').trim()
    return /^1\d{10}$/.test(phone)
      && nickName
      && nickName !== DEFAULT_USER_NAME
      && avatarUrl
      && avatarUrl !== DEFAULT_AVATAR_URL
  },

  finishOnboarding() {
    if (this.data.saving || this.data.phoneBinding) return
    if (!this.isCurrentProfileComplete()) {
      util.showToast('请先授权手机号并完善头像昵称')
      return
    }
    if (this.data.hasProfileChanged) {
      this.saveProfile({ goMine: true })
      return
    }
    this.goReturnTarget(true)
  }
})
