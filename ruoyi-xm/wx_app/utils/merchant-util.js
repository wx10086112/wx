const MERCHANT_ORDER_KEY = 'merchant_order_list'
const MERCHANT_GOODS_KEY = 'merchant_goods_list'
const MERCHANT_STORE_KEY = 'merchant_store_info'
const MERCHANT_STAFF_KEY = 'merchant_staff_list'
const MERCHANT_ORDER_FILTER_KEY = 'merchant_order_filter'
const MERCHANT_VERIFY_RECORD_KEY = 'merchant_verify_record_list'
const MERCHANT_WITHDRAW_KEY = 'merchant_withdraw_record_list'

const GROUPON_ORDER_STATUSES = ['PENDING_VERIFY', 'COMPLETED', 'REFUNDING', 'REFUNDED', 'CANCELLED']

const orderStatusMap = {
  PENDING_VERIFY: { text: '待核销', className: 'blue' },
  COMPLETED: { text: '已完成', className: 'green' },
  REFUNDING: { text: '退款中', className: 'orange' },
  REFUNDED: { text: '已退款', className: 'gray' },
  CANCELLED: { text: '已取消', className: 'gray' }
}

const clone = (data) => JSON.parse(JSON.stringify(data))

const formatDate = (date, fmt = 'YYYY-MM-DD HH:mm') => {
  if (!date) return ''
  let target
  if (typeof date === 'number') {
    target = new Date(date)
  } else if (typeof date === 'string') {
    target = new Date(date.replace(/-/g, '/'))
  } else {
    target = date
  }
  if (!(target instanceof Date) || isNaN(target.getTime())) return ''
  const map = {
    'M+': target.getMonth() + 1,
    'D+': target.getDate(),
    'H+': target.getHours(),
    'm+': target.getMinutes(),
    's+': target.getSeconds()
  }

  if (/(Y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (target.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  Object.keys(map).forEach((key) => {
    if (new RegExp(`(${key})`).test(fmt)) {
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? map[key] : `00${map[key]}`.slice(`${map[key]}`.length))
    }
  })

  return fmt
}

const formatPrice = (price) => (Number(price || 0) / 100).toFixed(2)

const maskWriteOffCode = (code) => {
  const value = String(code || '').trim()
  if (!value) return ''
  if (value.length <= 4) return `${value.slice(0, 1)}**${value.slice(-1)}`
  if (value.length <= 8) return `${value.slice(0, 2)}****${value.slice(-2)}`
  return `${value.slice(0, 4)}****${value.slice(-4)}`
}

const getCallablePhone = (phone) => {
  const value = String(phone || '').trim()
  return /^1\d{10}$/.test(value) ? value : ''
}

let loadingVisible = false

const showLoading = (title = '加载中...') => {
  loadingVisible = true
  wx.showLoading({
    title,
    mask: true
  })
}

const hideLoading = () => {
  if (!loadingVisible) return
  loadingVisible = false
  wx.hideLoading()
}

const showToast = (title, icon = 'none') => {
  hideLoading()
  wx.showToast({
    title,
    icon
  })
}

const MERCHANT_TABBAR_PAGES = ['/pages/home/home', '/pages/order/order', '/pages/mine/mine']
const MERCHANT_NAV_LIST = [
  { key: 'workbench', label: '工作台', url: '/pages/merchant/workbench/workbench' },
  { key: 'order', label: '订单', url: '/pages/merchant/order/order' },
  { key: 'verify', label: '核销', url: '/pages/merchant/verify/verify' },
  { key: 'goods', label: '商品', url: '/pages/merchant/goods/goods' },
  { key: 'mine', label: '我的', url: '/pages/merchant/mine/mine' }
]
const MERCHANT_MAIN_PAGE_PATHS = [
  '/pages/merchant/index/index',
  '/pages/merchant/workbench/workbench',
  '/pages/merchant/order/order',
  '/pages/merchant/verify/verify',
  '/pages/merchant/goods/goods',
  '/pages/merchant/mine/mine'
]

const normalizeRouteUrl = (url = '') => String(url || '').split('?')[0]
const getCurrentRoutePath = () => {
  const pageStack = getCurrentPages()
  const currentPage = pageStack[pageStack.length - 1]
  return currentPage && currentPage.route ? `/${currentPage.route}` : ''
}
const isMerchantMainPage = (url = '') => MERCHANT_MAIN_PAGE_PATHS.includes(normalizeRouteUrl(url))

const navigateTo = (url) => wx.navigateTo({ url })
const redirectTo = (url) => wx.redirectTo({ url })
const openMerchantMainPage = (url) => {
  const targetPath = normalizeRouteUrl(url)
  if (!targetPath) return
  if (getCurrentRoutePath() === targetPath) return
  wx.reLaunch({ url })
}

const switchTab = (url) => {
  const targetPath = normalizeRouteUrl(url)
  if (!targetPath) return

  if (getCurrentRoutePath() === targetPath) return

  if (MERCHANT_TABBAR_PAGES.includes(targetPath)) {
    wx.switchTab({ url: targetPath })
    return
  }

  if (isMerchantMainPage(targetPath)) {
    openMerchantMainPage(url)
    return
  }

  const pageStack = getCurrentPages()

  if (pageStack.length >= 9) {
    wx.redirectTo({ url })
    return
  }

  wx.navigateTo({ url })
}

const getMerchantNavList = (currentKey = '') => {
  return MERCHANT_NAV_LIST.map((item) => ({
    ...item,
    active: item.key === currentKey
  }))
}

const getOrderStatusMeta = (status) => orderStatusMap[status] || { text: '未知', className: 'gray' }

const isGrouponOrder = (order = {}) => {
  return GROUPON_ORDER_STATUSES.includes(order.status) && (!order.orderType || order.orderType === 'GROUPON')
}

const normalizeGrouponOrders = (orderList = []) => {
  return orderList
    .filter(isGrouponOrder)
    .map((item) => ({
      orderId: item.orderId,
      orderNo: item.orderNo,
      goodsId: item.goodsId,
      title: item.title,
      customerName: item.customerName,
      customerPhone: item.customerPhone,
      quantity: item.quantity,
      payAmount: item.payAmount,
      status: item.status,
      orderType: 'GROUPON',
      createTime: item.createTime,
      payTime: item.payTime,
      writeOffCode: item.writeOffCode,
      verifyTime: item.verifyTime,
      verifyStaffName: item.verifyStaffName,
      refundReason: item.refundReason,
      refundTime: item.refundTime,
      refundRejectReason: item.refundRejectReason,
      refundRejectTime: item.refundRejectTime,
      cancelReason: item.cancelReason,
      cancelTime: item.cancelTime,
      remark: item.remark || ''
    }))
}

const getOrderList = () => normalizeGrouponOrders(wx.getStorageSync(MERCHANT_ORDER_KEY) || [])
const setOrderList = (list = []) => wx.setStorageSync(MERCHANT_ORDER_KEY, clone(normalizeGrouponOrders(list)))

const getGoodsList = () => clone(wx.getStorageSync(MERCHANT_GOODS_KEY) || [])
const setGoodsList = (list = []) => wx.setStorageSync(MERCHANT_GOODS_KEY, clone(list))

const getStoreInfo = () => clone(wx.getStorageSync(MERCHANT_STORE_KEY) || {})
const setStoreInfo = (data = {}) => wx.setStorageSync(MERCHANT_STORE_KEY, clone(data))

const getStaffList = () => clone(wx.getStorageSync(MERCHANT_STAFF_KEY) || [])
const setStaffList = (list = []) => wx.setStorageSync(MERCHANT_STAFF_KEY, clone(list))

const getVerifyRecordList = () => clone(wx.getStorageSync(MERCHANT_VERIFY_RECORD_KEY) || [])
const setVerifyRecordList = (list = []) => wx.setStorageSync(MERCHANT_VERIFY_RECORD_KEY, clone(list))

const getWithdrawRecordList = () => clone(wx.getStorageSync(MERCHANT_WITHDRAW_KEY) || [])
const setWithdrawRecordList = (list = []) => wx.setStorageSync(MERCHANT_WITHDRAW_KEY, clone(list))

const setPendingOrderFilter = (status = '') => {
  wx.setStorageSync(MERCHANT_ORDER_FILTER_KEY, status)
}

const consumePendingOrderFilter = () => {
  const status = wx.getStorageSync(MERCHANT_ORDER_FILTER_KEY)
  wx.removeStorageSync(MERCHANT_ORDER_FILTER_KEY)
  return status || ''
}

module.exports = {
  clone,
  formatDate,
  formatPrice,
  maskWriteOffCode,
  getCallablePhone,
  showToast,
  showLoading,
  hideLoading,
  navigateTo,
  redirectTo,
  openMerchantMainPage,
  switchTab,
  getMerchantNavList,
  getOrderStatusMeta,
  isGrouponOrder,
  normalizeGrouponOrders,
  getOrderList,
  setOrderList,
  getGoodsList,
  setGoodsList,
  getStoreInfo,
  setStoreInfo,
  getStaffList,
  setStaffList,
  getVerifyRecordList,
  setVerifyRecordList,
  getWithdrawRecordList,
  setWithdrawRecordList,
  setPendingOrderFilter,
  consumePendingOrderFilter
}
