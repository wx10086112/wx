const app = getApp()
const util = require('../../utils/util')
const userApi = require('../../api/user')
const merchantApi = require('../../api/merchant')

const SERVICE_MENUS = [
  { label: '个人资料', desc: '头像、手机号', url: '/pages/profile-edit/profile-edit' },
  { label: '联系客服', desc: '电话与营业时间', url: '/pages/contact/contact' }
]

const EMPTY_MERCHANT_INFO = {
  id: null,
  name: '商家信息加载中',
  phone: '',
  address: '正在读取门店公开资料',
  distance: '',
  latitude: null,
  longitude: null,
  businessHours: '',
  businessHoursText: '请稍候',
  businessStatus: false,
  businessStatusText: '',
  bookingText: '',
  displayTags: []
}

const buildBusinessHoursText = (merchant = {}) => {
  if (merchant.businessHoursText) return merchant.businessHoursText
  if (merchant.businessHours) return `周一至周日 ${merchant.businessHours}`
  return '暂无营业时间'
}

const normalizeMerchantInfo = (merchant = {}) => {
  const id = Number(merchant.id || 0)
  const phone = merchant.phone || ''
  const name = merchant.name || merchant.storeName || merchant.brandName || '暂无商家名称'
  const rawTags = Array.isArray(merchant.tags) ? merchant.tags : []

  return {
    id,
    name,
    phone,
    address: merchant.address || '暂无门店地址',
    distance: merchant.distance || '',
    latitude: merchant.latitude || null,
    longitude: merchant.longitude || null,
    businessHours: merchant.businessHours || '',
    businessHoursText: buildBusinessHoursText(merchant),
    businessStatus: merchant.businessStatus !== false && !!id,
    businessStatusText: merchant.businessStatus === false ? '休息中' : (id ? '营业中' : ''),
    bookingText: merchant.supportBooking === false ? '到店即用' : (id ? '可预约' : ''),
    displayTags: rawTags.filter((tag) => !['营业中', '休息中'].includes(tag))
  }
}

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    serviceMenuList: SERVICE_MENUS,
    loginStep: 'idle',
    merchantInfo: EMPTY_MERCHANT_INFO,
    merchantLoading: false
  },

  onLoad() {
    this.checkLoginStatus()
    this.loadMerchantInfo()
  },

  onShow() {
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    this.setData({
      isLoggedIn: app.globalData.isLoggedIn,
      userInfo: app.globalData.userInfo || {}
    })
  },

  loadMerchantInfo() {
    this.setData({ merchantLoading: true })
    merchantApi
      .getMerchantList()
      .then((res) => {
        const merchant = (res.data || res || [])[0] || {}
        this.setData({
          merchantInfo: normalizeMerchantInfo(merchant),
          merchantLoading: false
        })
      })
      .catch(() => {
        this.setData({
          merchantInfo: normalizeMerchantInfo({}),
          merchantLoading: false
        })
      })
  },

  fetchMerchantInfo() {
    this.setData({ merchantLoading: true })
    return merchantApi
      .getMerchantList()
      .then((res) => {
        const merchant = normalizeMerchantInfo((res.data || res || [])[0] || {})
        this.setData({
          merchantInfo: merchant,
          merchantLoading: false
        })
        return merchant
      })
      .catch(() => {
        const merchant = normalizeMerchantInfo({})
        this.setData({
          merchantInfo: merchant,
          merchantLoading: false
        })
        return merchant
      })
  },

  handleLogin() {
    this.setData({ loginStep: 'loading' })
    util.showLoading('登录中...')

    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          util.hideLoading()
          util.showToast('登录失败，请重试')
          this.setData({ loginStep: 'idle' })
          return
        }

        userApi
          .login(app.globalData.appId, loginRes.code)
          .then((res) => {
            const info = res.data || {}
            const userInfo = {
              userId: info.userId || '',
              openId: info.openId || '',
              nickName: info.userName || '微信用户',
              avatarUrl: info.avatarUrl || '/assets/images/avatar.png',
              phone: info.phone || ''
            }
            const token = info.apiToken || ''

            if (!token) {
              util.hideLoading()
              util.showToast('登录失败，请重试')
              this.setData({ loginStep: 'idle' })
              return
            }

            app.setLoginInfo(token, userInfo)

            if (!userInfo.phone) {
              util.hideLoading()
              this.setData({
                isLoggedIn: true,
                userInfo,
                loginStep: 'phone'
              })
              return
            }

            this.setData({
              isLoggedIn: true,
              userInfo,
              loginStep: 'idle'
            })
            util.hideLoading()
            util.showToast('登录成功', 'success')
          })
          .catch(() => {
            util.hideLoading()
            util.showToast('登录失败，请重试')
            this.setData({ loginStep: 'idle' })
          })
      },
      fail: () => {
        util.hideLoading()
        util.showToast('登录失败，请重试')
        this.setData({ loginStep: 'idle' })
      }
    })
  },

  handlePhoneAuth(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      util.showToast('需要授权手机号才能使用完整功能')
      return
    }

    const phoneCode = e.detail.code
    if (!phoneCode) {
      util.showToast('获取手机号失败，请重试')
      return
    }

    util.showLoading('绑定中...')
    userApi
      .bindPhoneByCode(phoneCode)
      .then((res) => {
        const info = res.data || {}
        if (info.phone) {
          const userInfo = { ...this.data.userInfo, phone: info.phone }
          app.setLoginInfo(app.globalData.token, userInfo)
          this.setData({ userInfo, loginStep: 'idle' })
        }
        util.hideLoading()
        util.showToast('登录成功', 'success')
      })
      .catch(() => {
        util.hideLoading()
        this.setData({ loginStep: 'idle' })
        util.showToast('手机号绑定失败，可稍后在个人中心补充')
      })
  },

  callMerchant() {
    const phone = this.data.merchantInfo.phone
    if (!phone) {
      util.showToast('暂无联系电话')
      return
    }
    wx.makePhoneCall({
      phoneNumber: phone,
      fail: () => {}
    })
  },

  openMerchantMap() {
    const merchant = this.data.merchantInfo
    if (!merchant.latitude || !merchant.longitude) {
      util.showToast('暂无门店位置')
      return
    }
    wx.openLocation({
      latitude: Number(merchant.latitude),
      longitude: Number(merchant.longitude),
      name: merchant.name,
      address: merchant.address
    })
  },

  goMerchantDetail() {
    const merchantId = this.data.merchantInfo.id
    if (merchantId) {
      util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${merchantId}`)
      return
    }

    if (this.data.merchantLoading) {
      util.showToast('正在加载商家信息')
      return
    }

    util.showLoading('加载商家信息...')
    this.fetchMerchantInfo().then((merchant) => {
      util.hideLoading()
      if (merchant.id) {
        util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${merchant.id}`)
        return
      }
      util.showToast('暂无商家信息')
    })
  },

  handleLogout() {
    util.showModal('退出登录', '确定退出当前账号吗？').then((confirm) => {
      if (!confirm) return
      app.clearLoginInfo()
      this.setData({
        isLoggedIn: false,
        userInfo: {}
      })
      util.showToast('已退出登录', 'success')
    })
  },

  goPage(e) {
    util.navigateTo(e.currentTarget.dataset.url)
  },

  goProfileEdit() {
    util.navigateTo('/pages/profile-edit/profile-edit')
  }
})
