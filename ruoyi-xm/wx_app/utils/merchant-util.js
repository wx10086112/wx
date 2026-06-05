const MERCHANT_STORAGE_VERSION_KEY = 'merchant_mock_storage_version'
const MERCHANT_STORAGE_VERSION = 'merchant_real_login_20260602'
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

const showToast = (title, icon = 'none') => {
  wx.showToast({
    title,
    icon
  })
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

const MERCHANT_TABBAR_PAGES = ['/pages/home/home', '/pages/order/order', '/pages/mine/mine']
const MERCHANT_NAV_LIST = [
  { key: 'workbench', label: '工作台', url: '/pages/merchant/index/index?tab=workbench' },
  { key: 'order', label: '订单', url: '/pages/merchant/index/index?tab=order' },
  { key: 'verify', label: '核销', url: '/pages/merchant/index/index?tab=verify' },
  { key: 'goods', label: '商品', url: '/pages/merchant/index/index?tab=goods' },
  { key: 'mine', label: '我的', url: '/pages/merchant/index/index?tab=mine' }
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

const buildVerifyRecords = (orderList = []) => {
  return normalizeGrouponOrders(orderList)
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
}

const resetMerchantMockStorage = (mock) => {
  wx.setStorageSync(MERCHANT_ORDER_KEY, clone(normalizeGrouponOrders(mock.orderList)))
  wx.setStorageSync(MERCHANT_GOODS_KEY, clone(mock.goodsList))
  wx.setStorageSync(MERCHANT_STORE_KEY, clone(mock.merchantInfo))
  wx.setStorageSync(MERCHANT_STAFF_KEY, clone(mock.staffList))
  wx.setStorageSync(MERCHANT_VERIFY_RECORD_KEY, clone(buildVerifyRecords(mock.orderList)))
  wx.setStorageSync(MERCHANT_WITHDRAW_KEY, [])
  wx.setStorageSync(MERCHANT_STORAGE_VERSION_KEY, MERCHANT_STORAGE_VERSION)
}

const initMerchantMockStorage = (mock) => {
  if (wx.getStorageSync(MERCHANT_STORAGE_VERSION_KEY) !== MERCHANT_STORAGE_VERSION) {
    resetMerchantMockStorage(mock)
    return
  }

  if (!wx.getStorageSync(MERCHANT_ORDER_KEY)) {
    wx.setStorageSync(MERCHANT_ORDER_KEY, clone(normalizeGrouponOrders(mock.orderList)))
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
    wx.setStorageSync(MERCHANT_VERIFY_RECORD_KEY, clone(buildVerifyRecords(mock.orderList)))
  }
  if (!wx.getStorageSync(MERCHANT_WITHDRAW_KEY)) {
    wx.setStorageSync(MERCHANT_WITHDRAW_KEY, [])
  }
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

const buildWorkbenchStats = (orderList = [], goodsList = []) => {
  const grouponOrders = normalizeGrouponOrders(orderList)
  return {
    pendingVerifyCount: grouponOrders.filter((item) => item.status === 'PENDING_VERIFY').length,
    completedCount: grouponOrders.filter((item) => item.status === 'COMPLETED').length,
    refundingCount: grouponOrders.filter((item) => item.status === 'REFUNDING').length,
    refundedCount: grouponOrders.filter((item) => item.status === 'REFUNDED').length,
    cancelledCount: grouponOrders.filter((item) => item.status === 'CANCELLED').length,
    onShelfCount: goodsList.filter((item) => item.status === 'ON_SHELF').length,
    todaySalesAmount: grouponOrders
      .filter((item) => ['PENDING_VERIFY', 'COMPLETED'].includes(item.status))
      .reduce((sum, item) => sum + Number(item.payAmount || 0), 0),
    abnormalCount: grouponOrders.filter((item) => item.status === 'REFUNDING').length
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
      const finishTime = item.verifyTime || item.completeTime || item.payTime
      const settleTime = finishTime + DAY_MILLISECONDS
      const transferStatus = settleTime <= Date.now() ? 'ARRIVED' : 'WAITING_T1'
      return {
        ledgerId: index + 1,
        settlementId: index + 1,
        orderNo: item.orderNo,
        title: item.title,
        orderAmount: item.payAmount,
        merchantAmount: Math.floor(Number(item.payAmount || 0) * MERCHANT_RATE / 100),
        platformFeeAmount: Math.floor(Number(item.payAmount || 0) * PLATFORM_RATE / 100),
        status: settleTime <= Date.now() ? 'SETTLED' : 'PENDING',
        transferStatus,
        finishTime,
        settleTime,
        arriveTime: transferStatus === 'ARRIVED' ? settleTime : null,
        transferRemark: transferStatus === 'ARRIVED' ? '微信已自动打款至结算卡' : '订单完成后进入 T+1 自动打款队列'
      }
    })
    .sort((a, b) => (b.finishTime || 0) - (a.finishTime || 0))
}

const isSameDay = (time) => time && formatDate(time, 'YYYYMMDD') === formatDate(Date.now(), 'YYYYMMDD')
const isSameMonth = (time) => time && formatDate(time, 'YYYYMM') === formatDate(Date.now(), 'YYYYMM')

const buildSettlementRecordList = (ledgerList = []) => {
  const withdrawList = getWithdrawRecordList().sort((a, b) => (b.applyTime || 0) - (a.applyTime || 0))
  const apiStyleRecords = withdrawList.map((item) => ({
    settlementId: `W${item.withdrawId}`,
    orderNo: item.orderNo || '',
    title: item.title || '自动结算批次',
    amount: Number(item.amount || 0),
    status:
      item.status === 'FAILED'
        ? 'FAILED'
        : item.status === 'SUCCESS'
          ? 'ARRIVED'
          : 'TRANSFERRING',
    applyTime: item.applyTime,
    expectedTransferTime: item.expectedTransferTime || item.applyTime + DAY_MILLISECONDS,
    arriveTime: item.arriveTime || (item.status === 'SUCCESS' ? item.applyTime + DAY_MILLISECONDS : null),
    remark: item.remark || '微信支付自动打款处理中'
  }))

  const ledgerRecords = ledgerList.map((item) => ({
    settlementId: `L${item.ledgerId}`,
    orderNo: item.orderNo,
    title: item.title,
    amount: item.merchantAmount,
    status: item.transferStatus,
    applyTime: item.finishTime,
    expectedTransferTime: item.settleTime,
    arriveTime: item.arriveTime,
    remark: item.transferRemark
  }))

  return [...apiStyleRecords, ...ledgerRecords].sort(
    (a, b) => (b.expectedTransferTime || b.applyTime || 0) - (a.expectedTransferTime || a.applyTime || 0)
  )
}

const buildFinanceOverview = () => {
  const ledgerList = buildFinanceLedgerList()
  const settlementRecordList = buildSettlementRecordList(ledgerList)
  const storeInfo = getStoreInfo()
  const settledAmount = ledgerList
    .filter((item) => item.status === 'SETTLED')
    .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0)
  const pendingSettleAmount = ledgerList
    .filter((item) => item.status === 'PENDING')
    .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0)
  const processingAmount = settlementRecordList
    .filter((item) => item.status === 'TRANSFERRING')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)

  return {
    todayIncomeAmount: ledgerList
      .filter((item) => isSameDay(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    monthIncomeAmount: ledgerList
      .filter((item) => isSameMonth(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    pendingSettleAmount,
    withdrawableAmount: settledAmount,
    settledAmount,
    processingAmount,
    pendingAutoTransferAmount: pendingSettleAmount + processingAmount,
    platformFeeAmount: ledgerList.reduce((sum, item) => sum + Number(item.platformFeeAmount || 0), 0),
    completedOrderCount: ledgerList.length,
    autoTransferMode: 'T+1',
    nextAutoTransferTime: Date.now() + DAY_MILLISECONDS,
    settlementAccount: {
      accountName: storeInfo.storeName || storeInfo.brandName || '当前门店',
      bankName: '微信支付结算银行卡',
      accountNoTail: String(storeInfo.phone || '6601').slice(-4),
      status: 'VERIFIED'
    },
    ledgerList,
    settlementRecordList,
    withdrawList: settlementRecordList
  }
}

const cancelOrder = (orderNo, reason = '') => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (['COMPLETED', 'CANCELLED', 'REFUNDED'].includes(target.status)) {
    return { success: false, message: '当前状态不可取消' }
  }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'CANCELLED', cancelTime: Date.now(), cancelReason: reason }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '订单已取消' }
}

const approveRefundOrder = (orderNo) => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'REFUNDING') return { success: false, message: '当前状态不可退款' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'REFUNDED', refundTime: Date.now() }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '已同意退款' }
}

const rejectRefundOrder = (orderNo, reason = '') => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'REFUNDING') return { success: false, message: '当前状态不可操作' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'PENDING_VERIFY', refundRejectReason: reason, refundRejectTime: Date.now() }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '已拒绝退款' }
}

const getLowStockGoods = (threshold = 20) => {
  return getGoodsList().filter((item) => item.status === 'ON_SHELF' && Number(item.stock || 0) <= threshold)
}

const batchUpdateGoodsStatus = (goodsIds = [], status = 'OFF_SHELF') => {
  const goodsList = getGoodsList()
  const nextList = goodsList.map((item) =>
    goodsIds.includes(item.goodsId) ? { ...item, status } : item
  )
  setGoodsList(nextList)
  return { success: true, message: `已批量${status === 'ON_SHELF' ? '上架' : '下架'} ${goodsIds.length} 个商品` }
}

module.exports = {
  clone,
  formatDate,
  formatPrice,
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
  cancelOrder,
  approveRefundOrder,
  rejectRefundOrder,
  getLowStockGoods,
  batchUpdateGoodsStatus,
  buildFinanceOverview
}
