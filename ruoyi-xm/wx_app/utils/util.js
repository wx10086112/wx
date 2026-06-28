const ORDER_STORAGE_KEY = 'o2o_order_list'
const ORDER_FILTER_KEY = 'o2o_order_filter'
const { normalizeImageUrl, normalizeImageFields } = require('./image-url')

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

const buildImageCropStyle = (crop = {}) => {
  const scale = Math.min(Math.max(Number(crop.scale || 1), 1), 2.2)
  const renderedPercent = 130 * scale
  const limit = ((renderedPercent - 100) / (renderedPercent * 2)) * 100
  const x = Math.min(Math.max(Number(crop.x || 0), -limit), limit)
  const y = Math.min(Math.max(Number(crop.y || 0), -limit), limit)
  return `transform: translate(${x}%, ${y}%) scale(${scale});`
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

const orderHistoryActionMap = {
  CREATE: '创建订单',
  PAY_SUCCESS: '支付成功',
  CANCEL: '取消订单',
  WRITE_OFF_COMPLETE: '核销完成',
  REFUND_APPLY: '申请退款',
  REFUND_APPROVE: '同意退款',
  REFUND_REJECT: '拒绝退款',
  REFUND_SUCCESS: '退款成功',
  MERCHANT_ACCEPT: '商家接单',
  MERCHANT_REJECT: '商家拒单',
  MERCHANT_CANCEL: '商家取消',
  ADMIN_UPDATE_STATUS: '后台改状态'
}

const failedHistoryActions = ['CANCEL', 'MERCHANT_REJECT', 'MERCHANT_CANCEL', 'REFUND_REJECT']

const formatOrderHistory = (history = []) => {
  if (!Array.isArray(history)) return []
  return history.map((item, index) => ({
    ...item,
    key: `${item.action || 'history'}-${item.changeTime || index}`,
    label: orderHistoryActionMap[item.action] || item.action || '订单更新',
    timeText: item.changeTime ? formatDate(item.changeTime, 'YYYY-MM-DD HH:mm') : '',
    operatorText: item.operatorName ? `操作人：${item.operatorName}` : '',
    remarkText: item.remark || '',
    className: failedHistoryActions.includes(item.action) ? 'failed' : 'done'
  }))
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

const showToast = (title, icon = 'none', duration = 2000) => {
  hideLoading()
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
  try {
    const stored = wx.getStorageSync(ORDER_STORAGE_KEY)
    if (Array.isArray(stored) && stored.length) {
      return normalizeImageFields(clone(stored))
    }
  } catch (e) {
    wx.removeStorageSync(ORDER_STORAGE_KEY)
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
  const randomChars = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'
  const datePart = formatDate(new Date(), 'YYYYMMDD')
  let randomPart = ''
  for (let i = 0; i < 8; i += 1) {
    randomPart += randomChars.charAt(Math.floor(Math.random() * randomChars.length))
  }
  return `LY${datePart}${randomPart}`
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

const SUBSCRIBE_TMPL_IDS = [
  'order_status_change',
  'write_off_remind'
]

const requestSubscribeMessage = () => {
  return new Promise((resolve) => {
    if (!wx.requestSubscribeMessage) {
      resolve(true)
      return
    }
    wx.requestSubscribeMessage({
      tmplIds: SUBSCRIBE_TMPL_IDS,
      success: (res) => {
        const accepted = SUBSCRIBE_TMPL_IDS.some((id) => res[id] === 'accept')
        resolve(accepted)
      },
      fail: () => resolve(false)
    })
  })
}

const requestPayment = (payParams) => {
  return new Promise((resolve, reject) => {
    if (!payParams || !payParams.timeStamp) {
      reject(new Error('支付参数缺失，请稍后重试'))
      return
    }
    wx.requestPayment({
      timeStamp: String(payParams.timeStamp),
      nonceStr: payParams.nonceStr,
      package: payParams.package || payParams.packageValue,
      signType: payParams.signType || 'HMAC-SHA256',
      paySign: payParams.paySign,
      success: () => resolve(true),
      fail: (err) => {
        if (err.errMsg && err.errMsg.includes('cancel')) {
          reject(new Error('用户取消支付'))
        } else {
          console.warn('requestPayment failed', err)
          reject(new Error('微信支付拉起失败，请稍后重试'))
        }
      }
    })
  })
}

module.exports = {
  formatDate,
  formatTime,
  formatPrice,
  formatOrderStatus,
  getOrderStatusMeta,
  formatOrderHistory,
  clone,
  normalizeImageUrl,
  normalizeImageFields,
  buildImageCropStyle,
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
  transitionOrderToCompleted,
  requestSubscribeMessage,
  requestPayment
}
