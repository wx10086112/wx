const util = require('./utils/util')
const merchantUtil = require('./utils/merchant-util')
const MERCHANT_ENTRY_KEY = 'merchantEntry'
const BASE_URL_STORAGE_KEY = 'baseUrl'
const DEFAULT_BASE_URL = 'http://localhost:8080'

const normalizeBaseUrl = (value = '') => {
  if (!value || typeof value !== 'string') return DEFAULT_BASE_URL
  return value.trim().replace(/\/+$/, '') || DEFAULT_BASE_URL
}

const normalizeMerchantRoleKey = (roleKey = '') => {
  if (roleKey === 'manager') return 'owner'
  if (roleKey === 'clerk') return 'member'
  return roleKey === 'owner' ? 'owner' : 'member'
}

const buildMerchantPermissions = (roleKey = 'member') => {
  const basePermissions = ['stats.view', 'order.manage', 'verify.scan', 'verify.manual', 'verify.record']
  if (roleKey !== 'owner') {
    return basePermissions
  }
  return [
    ...basePermissions,
    'goods.manage',
    'store.manage',
    'staff.manage',
    'finance.manage',
    'marketing.manage'
  ]
}

const normalizeMerchantStaffUser = (staffUser) => {
  if (!staffUser) return null
  const roleKey = normalizeMerchantRoleKey(staffUser.roleKey)
  return {
    ...staffUser,
    roleKey,
    roleName: roleKey === 'owner' ? '店长' : '店员',
    permissions: buildMerchantPermissions(roleKey)
  }
}

const normalizeMerchantEntry = (entry = {}) => {
  const merchantId = Number(entry.merchantId || 0)
  if (!merchantId) return null
  return {
    merchantId,
    merchantName: entry.merchantName || '',
    contact: entry.contact || '',
    phone: entry.phone || '',
    entryAppId: entry.entryAppId || '',
    miniAppConfigured: entry.miniAppConfigured === true,
    loginPage: entry.loginPage || `/pages/merchant/login/login?merchantId=${merchantId}`
  }
}

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false,
    staffUser: null,
    merchantToken: null,
    isMerchantLoggedIn: false,
    permissionCodes: [],
    appId: '',
    merchantEntry: null
  },

  onLaunch(options = {}) {
    this.initEnv()
    this.checkLoginStatus()
    this.restoreMerchantEntry()
    this.applyMerchantEntryOptions(options)
    this.restoreMerchantLogin()
  },

  onShow(options = {}) {
    this.applyMerchantEntryOptions(options)
  },

  initEnv() {
    this.baseUrl = normalizeBaseUrl(wx.getStorageSync(BASE_URL_STORAGE_KEY) || DEFAULT_BASE_URL)
    wx.setStorageSync(BASE_URL_STORAGE_KEY, this.baseUrl)
    const accountInfo = wx.getAccountInfoSync()
    if (accountInfo && accountInfo.miniProgram) {
      this.globalData.appId = accountInfo.miniProgram.appId || ''
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    const userInfo = util.normalizeImageFields(wx.getStorageSync('userInfo'))
    util.getStoredOrderList()
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      this.globalData.isLoggedIn = true
      wx.setStorageSync('userInfo', userInfo)
    }
  },

  restoreMerchantLogin() {
    const merchantToken = wx.getStorageSync('merchantToken')
    const storedStaffUser = wx.getStorageSync('merchantStaffUser')
    const staffUser = normalizeMerchantStaffUser(storedStaffUser)

    if (!merchantToken || !staffUser) {
      this.clearMerchantLoginInfo()
      return
    }

    this.globalData.merchantToken = merchantToken
    this.globalData.staffUser = staffUser
    this.globalData.isMerchantLoggedIn = true
    this.globalData.permissionCodes = staffUser.permissions || []
    wx.setStorageSync('merchantStaffUser', staffUser)
  },

  restoreMerchantEntry() {
    const merchantEntry = normalizeMerchantEntry(wx.getStorageSync(MERCHANT_ENTRY_KEY))
    this.globalData.merchantEntry = merchantEntry
    if (merchantEntry) {
      wx.setStorageSync(MERCHANT_ENTRY_KEY, merchantEntry)
    } else {
      wx.removeStorageSync(MERCHANT_ENTRY_KEY)
    }
  },

  applyMerchantEntryOptions(options = {}) {
    const query = options.query || {}
    const scene = decodeURIComponent(query.scene || '')
    let merchantId = query.merchantId || ''

    if (!merchantId && scene) {
      const match = scene.match(/(?:^|&)merchantId=(\d+)(?:&|$)/)
      if (match) {
        merchantId = match[1]
      }
    }

    if (!merchantId) return
    this.setMerchantEntry({ merchantId })
  },

  setLoginInfo(token, userInfo) {
    const normalizedUserInfo = util.normalizeImageFields(userInfo)
    this.globalData.token = token
    this.globalData.userInfo = normalizedUserInfo
    this.globalData.isLoggedIn = true
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', normalizedUserInfo)
  },

  clearLoginInfo() {
    this.globalData.token = null
    this.globalData.userInfo = null
    this.globalData.isLoggedIn = false
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  setMerchantLoginInfo(token, staffUser) {
    const normalizedStaffUser = normalizeMerchantStaffUser(staffUser)
    this.globalData.merchantToken = token
    this.globalData.staffUser = normalizedStaffUser
    this.globalData.isMerchantLoggedIn = true
    this.globalData.permissionCodes = normalizedStaffUser ? normalizedStaffUser.permissions || [] : []
    wx.setStorageSync('merchantToken', token)
    wx.setStorageSync('merchantStaffUser', normalizedStaffUser)
  },

  setMerchantEntry(entry) {
    const merchantEntry = normalizeMerchantEntry({
      ...(this.globalData.merchantEntry || {}),
      ...(entry || {})
    })
    this.globalData.merchantEntry = merchantEntry
    if (merchantEntry) {
      wx.setStorageSync(MERCHANT_ENTRY_KEY, merchantEntry)
    } else {
      wx.removeStorageSync(MERCHANT_ENTRY_KEY)
    }
    return merchantEntry
  },

  getMerchantEntry() {
    return this.globalData.merchantEntry || null
  },

  clearMerchantEntry() {
    this.globalData.merchantEntry = null
    wx.removeStorageSync(MERCHANT_ENTRY_KEY)
  },

  clearMerchantLoginInfo() {
    this.globalData.merchantToken = null
    this.globalData.staffUser = null
    this.globalData.isMerchantLoggedIn = false
    this.globalData.permissionCodes = []
    wx.removeStorageSync('merchantToken')
    wx.removeStorageSync('merchantStaffUser')
  },

  needLogin() {
    if (!this.globalData.isLoggedIn) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({
              url: '/pages/mine/mine'
            })
          }
        }
      })
      return false
    }
    return true
  },

  needMerchantLogin() {
    if (this.globalData.isMerchantLoggedIn) return true
    wx.redirectTo({
      url: '/pages/merchant/login/login'
    })
    return false
  },

  hasPermission(code) {
    return (this.globalData.permissionCodes || []).includes(code)
  },

  hasAnyPermission(codeList = []) {
    if (!codeList.length) return true
    return codeList.some((code) => this.hasPermission(code))
  },

  needPermission(codeList = [], message = '当前账号暂无该操作权限') {
    if (this.hasAnyPermission(codeList)) return true
    merchantUtil.showToast(message)
    return false
  }
})
