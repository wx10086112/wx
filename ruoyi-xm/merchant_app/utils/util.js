const MERCHANT_ORDER_FILTER_KEY = 'merchant_order_filter'

const orderStatusMap = {
  PENDING_ACCEPT: { text: '待接单', className: 'orange' },
  ACCEPTED: { text: '已接单', className: 'blue' },
  PENDING_VERIFY: { text: '待核销', className: 'blue' },
  COMPLETED: { text: '已完成', className: 'green' },
  REJECTED: { text: '已拒单', className: 'gray' },
  REFUNDING: { text: '退款中', className: 'orange' },
  REFUNDED: { text: '已退款', className: 'gray' },
  CANCELLED: { text: '已取消', className: 'gray' }
}

const formatDate = (date, fmt = 'YYYY-MM-DD HH:mm') => {
  if (!date) return ''
  const target = typeof date === 'number' ? new Date(date) : date
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

const showToast = (title, icon = 'none') => {
  wx.showToast({
    title,
    icon
  })
}

const showModal = (title, content) => {
  return new Promise((resolve) => {
    wx.showModal({
      title,
      content,
      success: (res) => resolve(res.confirm)
    })
  })
}

const showModalWithInput = (title, placeholder = '') => {
  return new Promise((resolve) => {
    wx.showModal({
      title,
      editable: true,
      placeholderText: placeholder,
      success: (res) => {
        if (res.confirm) {
          resolve(res.content || '')
        } else {
          resolve(null)
        }
      }
    })
  })
}

const navigateTo = (url) => wx.navigateTo({ url })
const redirectTo = (url) => wx.redirectTo({ url })
const switchTab = (url) => wx.switchTab({ url })

const getOrderStatusMeta = (status) => orderStatusMap[status] || { text: '未知', className: 'gray' }

const setPendingOrderFilter = (status = '') => {
  wx.setStorageSync(MERCHANT_ORDER_FILTER_KEY, status)
}

const consumePendingOrderFilter = () => {
  const status = wx.getStorageSync(MERCHANT_ORDER_FILTER_KEY)
  wx.removeStorageSync(MERCHANT_ORDER_FILTER_KEY)
  return status || ''
}

module.exports = {
  formatDate,
  formatPrice,
  showToast,
  showModal,
  showModalWithInput,
  navigateTo,
  redirectTo,
  switchTab,
  getOrderStatusMeta,
  orderStatusMap,
  setPendingOrderFilter,
  consumePendingOrderFilter
}
