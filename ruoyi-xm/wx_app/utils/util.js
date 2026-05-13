const ORDER_STORAGE_KEY = 'o2o_order_list'
const ORDER_FILTER_KEY = 'o2o_order_filter'

const legacyImageMap = {
  'https://img.zcool.cn/community/01e07155431210000019ae9dd17df.jpg': '/assets/images/merchant-spa.png',
  'https://img.zcool.cn/community/01786555431210000019ae9d4c90b.jpg': '/assets/images/merchant-neck.png',
  'https://img.zcool.cn/community/01d8a155431210000019ae9d9c2d9.jpg': '/assets/images/merchant-fitness.png',
  'https://img.zcool.cn/community/01686555431210000019ae9d8c7f0.jpg': '/assets/images/merchant-meal.png',
  'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9V62zibQ/132': '/assets/images/avatar.png'
}

const orderStatusMap = {
  PENDING_PAY: { text: '待支付', class: 'status-pending', icon: '⏳' },
  PAID_UNUSED: { text: '待使用', class: 'status-paid', icon: '🎟' },
  USED_COMPLETED: { text: '已完成', class: 'status-completed', icon: '✅' },
  REFUNDING: { text: '退款中', class: 'status-refunding', icon: '↩' },
  REFUNDED: { text: '已退款', class: 'status-cancelled', icon: '↩' },
  CANCELLED: { text: '已取消', class: 'status-cancelled', icon: '✖' },
  CLOSED: { text: '已关闭', class: 'status-cancelled', icon: '✖' }
}

const formatDate = (date, fmt = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''

  let d = date
  if (typeof date === 'number') {
    d = new Date(date)
  }

  const o = {
    'M+': d.getMonth() + 1,
    'D+': d.getDate(),
    'H+': d.getHours(),
    'm+': d.getMinutes(),
    's+': d.getSeconds(),
    'q+': Math.floor((d.getMonth() + 3) / 3),
    S: d.getMilliseconds()
  }

  if (/(Y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (d.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }

  return fmt
}

const formatTime = (date) => {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

const clone = (data) => JSON.parse(JSON.stringify(data))

const normalizeImageUrl = (url = '') => {
  return legacyImageMap[url] || url
}

const normalizeImageFields = (data) => {
  if (Array.isArray(data)) {
    return data.map((item) => normalizeImageFields(item))
  }

  if (!data || typeof data !== 'object') {
    return data
  }

  const normalized = { ...data }
  ;['image', 'avatar', 'coverImage', 'avatarUrl'].forEach((key) => {
    if (typeof normalized[key] === 'string') {
      normalized[key] = normalizeImageUrl(normalized[key])
    }
  })

  return normalized
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return (Number(price) / 100).toFixed(2)
}

const getOrderStatusMeta = (status) => {
  return orderStatusMap[status] || { text: '未知', class: 'status-default', icon: '？' }
}

const formatOrderStatus = (status) => {
  return getOrderStatusMeta(status)
}

const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

const throttle = (func, limit) => {
  let inThrottle
  return function throttled(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
  }
}

const showLoading = (title = '加载中...') => {
  wx.showLoading({
    title,
    mask: true
  })
}

const hideLoading = () => {
  wx.hideLoading()
}

const showToast = (title, icon = 'none', duration = 2000) => {
  wx.showToast({
    title,
    icon,
    duration
  })
}

const showModal = (title, content, showCancel = true) => {
  return new Promise((resolve) => {
    wx.showModal({
      title,
      content,
      showCancel,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

const navigateTo = (url) => {
  wx.navigateTo({
    url
  })
}

const redirectTo = (url) => {
  wx.redirectTo({
    url
  })
}

const switchTab = (url) => {
  wx.switchTab({
    url
  })
}

const navigateBack = (delta = 1) => {
  wx.navigateBack({
    delta
  })
}

const getStoredOrderList = (fallback = []) => {
  const stored = wx.getStorageSync(ORDER_STORAGE_KEY)
  if (Array.isArray(stored) && stored.length) {
    const normalized = normalizeImageFields(clone(stored))
    if (JSON.stringify(stored) !== JSON.stringify(normalized)) {
      wx.setStorageSync(ORDER_STORAGE_KEY, normalized)
    }
    return normalized
  }
  return normalizeImageFields(clone(fallback))
}

const setStoredOrderList = (list = []) => {
  wx.setStorageSync(ORDER_STORAGE_KEY, normalizeImageFields(clone(list)))
}

const setPendingOrderFilter = (status = '') => {
  wx.setStorageSync(ORDER_FILTER_KEY, status)
}

const consumePendingOrderFilter = () => {
  const status = wx.getStorageSync(ORDER_FILTER_KEY)
  wx.removeStorageSync(ORDER_FILTER_KEY)
  return status || ''
}

const generateOrderNo = () => {
  return 'ORD' + formatDate(new Date(), 'YYYYMMDDHHmmss') + Math.floor(Math.random() * 900 + 100)
}

const createWriteOffCode = () => {
  return 'LY' + Math.floor(1000 + Math.random() * 9000)
}

const transitionOrderToCancelled = (order = {}, reason = '用户主动取消') => {
  return {
    ...order,
    status: 'CANCELLED',
    cancelTime: Date.now(),
    cancelReason: reason
  }
}

const transitionOrderToPaidUnused = (order = {}) => {
  return {
    ...order,
    status: 'PAID_UNUSED',
    payTime: order.payTime || Date.now(),
    writeOffCode: order.writeOffCode || createWriteOffCode(),
    writeOffDeadline: order.writeOffDeadline || Date.now() + 1000 * 60 * 60 * 24 * 30
  }
}

const transitionOrderToRefunding = (order = {}, reason = '用户申请退款') => {
  return {
    ...order,
    status: 'REFUNDING',
    refundTime: Date.now(),
    refundReason: reason
  }
}

const transitionOrderToCompleted = (order = {}) => {
  return {
    ...order,
    status: 'USED_COMPLETED',
    writeOffTime: order.writeOffTime || Date.now()
  }
}

module.exports = {
  formatDate,
  formatTime,
  formatPrice,
  formatOrderStatus,
  getOrderStatusMeta,
  clone,
  normalizeImageUrl,
  normalizeImageFields,
  debounce,
  throttle,
  showLoading,
  hideLoading,
  showToast,
  showModal,
  navigateTo,
  redirectTo,
  switchTab,
  navigateBack,
  getStoredOrderList,
  setStoredOrderList,
  setPendingOrderFilter,
  consumePendingOrderFilter,
  generateOrderNo,
  createWriteOffCode,
  transitionOrderToCancelled,
  transitionOrderToPaidUnused,
  transitionOrderToRefunding,
  transitionOrderToCompleted
}
