const MERCHANT_ORDER_KEY = 'merchant_order_list'
const MERCHANT_GOODS_KEY = 'merchant_goods_list'
const MERCHANT_STORE_KEY = 'merchant_store_info'
const MERCHANT_STAFF_KEY = 'merchant_staff_list'
const MERCHANT_ORDER_FILTER_KEY = 'merchant_order_filter'
const MERCHANT_VERIFY_RECORD_KEY = 'merchant_verify_record_list'
const MERCHANT_WITHDRAW_KEY = 'merchant_withdraw_record_list'

const DAY_MILLISECONDS = 24 * 60 * 60 * 1000
const MERCHANT_RATE = 90
const PLATFORM_RATE = 10

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

const navigateTo = (url) => wx.navigateTo({ url })
const redirectTo = (url) => wx.redirectTo({ url })
const switchTab = (url) => wx.switchTab({ url })

const getOrderStatusMeta = (status) => orderStatusMap[status] || { text: '未知', className: 'gray' }

const initMerchantMockStorage = (mock) => {
  if (!wx.getStorageSync(MERCHANT_ORDER_KEY)) {
    wx.setStorageSync(MERCHANT_ORDER_KEY, clone(mock.orderList))
  }
  if (!wx.getStorageSync(MERCHANT_GOODS_KEY)) {
    wx.setStorageSync(MERCHANT_GOODS_KEY, clone(mock.goodsList))
  }
  if (!wx.getStorageSync(MERCHANT_STORE_KEY)) {
    wx.setStorageSync(MERCHANT_STORE_KEY, clone(mock.merchantInfo))
  }
  if (!wx.getStorageSync(MERCHANT_STAFF_KEY)) {
    wx.setStorageSync(MERCHANT_STAFF_KEY, clone(mock.staffList))
  }
  if (!wx.getStorageSync(MERCHANT_VERIFY_RECORD_KEY)) {
    const verifyRecords = (mock.orderList || [])
      .filter((item) => item.status === 'COMPLETED')
      .map((item, index) => ({
        recordId: index + 1,
        orderNo: item.orderNo,
        goodsId: item.goodsId,
        title: item.title,
        inputCode: item.writeOffCode,
        writeOffCode: item.writeOffCode,
        customerName: item.customerName,
        customerPhone: item.customerPhone,
        payAmount: item.payAmount,
        status: 'SUCCESS',
        verifyTime: item.verifyTime,
        verifyStaffName: item.verifyStaffName
      }))
    wx.setStorageSync(MERCHANT_VERIFY_RECORD_KEY, clone(verifyRecords))
  }
  if (!wx.getStorageSync(MERCHANT_WITHDRAW_KEY)) {
    wx.setStorageSync(MERCHANT_WITHDRAW_KEY, [])
  }
}

const getOrderList = () => clone(wx.getStorageSync(MERCHANT_ORDER_KEY) || [])
const setOrderList = (list = []) => wx.setStorageSync(MERCHANT_ORDER_KEY, clone(list))

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

const buildWorkbenchStats = (orderList = [], goodsList = []) => {
  return {
    pendingVerifyCount: orderList.filter((item) => item.status === 'PENDING_VERIFY').length,
    completedCount: orderList.filter((item) => item.status === 'COMPLETED').length,
    refundingCount: orderList.filter((item) => item.status === 'REFUNDING').length,
    onShelfCount: goodsList.filter((item) => item.status === 'ON_SHELF').length,
    todaySalesAmount: orderList
      .filter((item) => ['PENDING_VERIFY', 'COMPLETED'].includes(item.status))
      .reduce((sum, item) => sum + Number(item.payAmount || 0), 0)
  }
}

const getValidPeriodEndTime = (validPeriod = '') => {
  const parts = String(validPeriod || '').split('至')
  const endDateText = (parts[parts.length - 1] || '').trim()
  if (!endDateText) return null
  const endTime = new Date(`${endDateText.replace(/\./g, '-')} 23:59:59`).getTime()
  return Number.isNaN(endTime) ? null : endTime
}

const isGoodsExpired = (order) => {
  const goods = getGoodsList().find((item) => item.goodsId === order.goodsId)
  if (!goods || !goods.validPeriod) return false
  const endTime = getValidPeriodEndTime(goods.validPeriod)
  return !!endTime && Date.now() > endTime
}

const appendVerifyRecord = (record) => {
  const recordList = getVerifyRecordList()
  const maxId = recordList.reduce((max, item) => Math.max(max, Number(item.recordId || 0)), 0)
  const nextRecord = {
    recordId: maxId + 1,
    verifyTime: Date.now(),
    ...record
  }
  setVerifyRecordList([nextRecord, ...recordList])
  return nextRecord
}

const verifyOrderByCode = (code, staffUser) => {
  const orderList = getOrderList()
  const targetOrder = orderList.find((item) => item.writeOffCode === code || item.orderNo === code)

  if (!targetOrder) {
    appendVerifyRecord({
      inputCode: code,
      title: '未知核销码',
      payAmount: 0,
      status: 'FAILED',
      failureReason: '未找到对应订单',
      verifyStaffId: staffUser.staffId,
      verifyStaffName: staffUser.name
    })
    return {
      success: false,
      message: '未找到对应订单'
    }
  }

  if (targetOrder.status === 'COMPLETED') {
    appendVerifyRecord({
      ...targetOrder,
      inputCode: code,
      status: 'FAILED',
      failureReason: '该订单已核销完成',
      verifyStaffId: staffUser.staffId,
      verifyStaffName: staffUser.name
    })
    return {
      success: false,
      message: '该订单已核销完成',
      order: targetOrder
    }
  }

  if (targetOrder.status !== 'PENDING_VERIFY') {
    appendVerifyRecord({
      ...targetOrder,
      inputCode: code,
      status: 'FAILED',
      failureReason: '当前订单状态不可核销',
      verifyStaffId: staffUser.staffId,
      verifyStaffName: staffUser.name
    })
    return {
      success: false,
      message: '当前订单状态不可核销',
      order: targetOrder
    }
  }

  if (isGoodsExpired(targetOrder)) {
    appendVerifyRecord({
      ...targetOrder,
      inputCode: code,
      status: 'FAILED',
      failureReason: '团购券已过有效期',
      verifyStaffId: staffUser.staffId,
      verifyStaffName: staffUser.name
    })
    return {
      success: false,
      message: '团购券已过有效期',
      order: targetOrder
    }
  }

  const nextOrderList = orderList.map((item) =>
    item.orderNo === targetOrder.orderNo
      ? {
          ...item,
          status: 'COMPLETED',
          verifyTime: Date.now(),
          verifyStaffName: staffUser.name
        }
      : item
  )

  setOrderList(nextOrderList)
  const verifiedOrder = nextOrderList.find((item) => item.orderNo === targetOrder.orderNo)
  appendVerifyRecord({
    ...verifiedOrder,
    inputCode: code,
    status: 'SUCCESS',
    verifyStaffId: staffUser.staffId,
    verifyStaffName: staffUser.name
  })

  return {
    success: true,
    message: '核销成功',
    order: verifiedOrder
  }
}

const buildFinanceLedgerList = () => {
  return getOrderList()
    .filter((item) => item.status === 'COMPLETED')
    .map((item, index) => {
      const finishTime = item.verifyTime || item.payTime
      const settleTime = finishTime + DAY_MILLISECONDS
      return {
        ledgerId: index + 1,
        orderNo: item.orderNo,
        title: item.title,
        orderAmount: item.payAmount,
        merchantAmount: Math.floor(Number(item.payAmount || 0) * MERCHANT_RATE / 100),
        platformFeeAmount: Math.floor(Number(item.payAmount || 0) * PLATFORM_RATE / 100),
        status: settleTime <= Date.now() ? 'SETTLED' : 'PENDING',
        finishTime,
        settleTime
      }
    })
    .sort((a, b) => (b.finishTime || 0) - (a.finishTime || 0))
}

const isSameDay = (time) => time && formatDate(time, 'YYYYMMDD') === formatDate(Date.now(), 'YYYYMMDD')
const isSameMonth = (time) => time && formatDate(time, 'YYYYMM') === formatDate(Date.now(), 'YYYYMM')

const buildFinanceOverview = () => {
  const ledgerList = buildFinanceLedgerList()
  const withdrawList = getWithdrawRecordList().sort((a, b) => (b.applyTime || 0) - (a.applyTime || 0))
  const settledAmount = ledgerList
    .filter((item) => item.status === 'SETTLED')
    .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0)
  const frozenWithdrawAmount = withdrawList.reduce((sum, item) => sum + Number(item.amount || 0), 0)

  return {
    todayIncomeAmount: ledgerList
      .filter((item) => isSameDay(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    monthIncomeAmount: ledgerList
      .filter((item) => isSameMonth(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    pendingSettleAmount: ledgerList
      .filter((item) => item.status === 'PENDING')
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    withdrawableAmount: Math.max(0, settledAmount - frozenWithdrawAmount),
    platformFeeAmount: ledgerList.reduce((sum, item) => sum + Number(item.platformFeeAmount || 0), 0),
    completedOrderCount: ledgerList.length,
    ledgerList,
    withdrawList
  }
}

const applyWithdraw = (amount) => {
  const overview = buildFinanceOverview()
  if (!amount || amount <= 0) {
    return {
      success: false,
      message: '请输入提现金额'
    }
  }
  if (amount > overview.withdrawableAmount) {
    return {
      success: false,
      message: '提现金额超过可提现余额'
    }
  }
  const withdrawList = getWithdrawRecordList()
  const maxId = withdrawList.reduce((max, item) => Math.max(max, Number(item.withdrawId || 0)), 0)
  const record = {
    withdrawId: maxId + 1,
    amount,
    status: 'PROCESSING',
    applyTime: Date.now(),
    remark: '商家端在线提现申请，等待平台/微信支付出款处理'
  }
  setWithdrawRecordList([record, ...withdrawList])
  return {
    success: true,
    message: '提现申请已提交',
    record
  }
}

module.exports = {
  clone,
  formatDate,
  formatPrice,
  showToast,
  showModal,
  navigateTo,
  redirectTo,
  switchTab,
  getOrderStatusMeta,
  initMerchantMockStorage,
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
  consumePendingOrderFilter,
  buildWorkbenchStats,
  verifyOrderByCode,
  buildFinanceOverview,
  applyWithdraw
}
