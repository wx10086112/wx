<<<<<<< HEAD
=======
const MERCHANT_STORAGE_VERSION_KEY = 'merchant_mock_storage_version'
const MERCHANT_STORAGE_VERSION = 'groupon_verify_only_20260525'
>>>>>>> 苏
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
<<<<<<< HEAD

const orderStatusMap = {
  PENDING_ACCEPT: { text: '待接单', className: 'orange' },
  ACCEPTED: { text: '已接单', className: 'blue' },
  SHIPPING: { text: '配送中', className: 'blue' },
=======
const GROUPON_ORDER_STATUSES = ['PENDING_VERIFY', 'COMPLETED', 'REFUNDING', 'REFUNDED', 'CANCELLED']

const orderStatusMap = {
>>>>>>> 苏
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

const navigateTo = (url) => wx.navigateTo({ url })
const redirectTo = (url) => wx.redirectTo({ url })
const switchTab = (url) => wx.switchTab({ url })

const getOrderStatusMeta = (status) => orderStatusMap[status] || { text: '未知', className: 'gray' }

<<<<<<< HEAD
const initMerchantMockStorage = (mock) => {
  if (!wx.getStorageSync(MERCHANT_ORDER_KEY)) {
    wx.setStorageSync(MERCHANT_ORDER_KEY, clone(mock.orderList))
=======
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
>>>>>>> 苏
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
<<<<<<< HEAD
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
=======
    wx.setStorageSync(MERCHANT_VERIFY_RECORD_KEY, clone(buildVerifyRecords(mock.orderList)))
>>>>>>> 苏
  }
  if (!wx.getStorageSync(MERCHANT_WITHDRAW_KEY)) {
    wx.setStorageSync(MERCHANT_WITHDRAW_KEY, [])
  }
}

<<<<<<< HEAD
const getOrderList = () => clone(wx.getStorageSync(MERCHANT_ORDER_KEY) || [])
const setOrderList = (list = []) => wx.setStorageSync(MERCHANT_ORDER_KEY, clone(list))
=======
const getOrderList = () => normalizeGrouponOrders(wx.getStorageSync(MERCHANT_ORDER_KEY) || [])
const setOrderList = (list = []) => wx.setStorageSync(MERCHANT_ORDER_KEY, clone(normalizeGrouponOrders(list)))
>>>>>>> 苏

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
<<<<<<< HEAD
  return {
    pendingAcceptCount: orderList.filter((item) => item.status === 'PENDING_ACCEPT').length,
    pendingVerifyCount: orderList.filter((item) => item.status === 'PENDING_VERIFY').length,
    completedCount: orderList.filter((item) => item.status === 'COMPLETED').length,
    refundingCount: orderList.filter((item) => item.status === 'REFUNDING').length,
    shippingCount: orderList.filter((item) => item.status === 'SHIPPING').length,
    onShelfCount: goodsList.filter((item) => item.status === 'ON_SHELF').length,
    todaySalesAmount: orderList
      .filter((item) => ['PENDING_VERIFY', 'COMPLETED', 'ACCEPTED', 'SHIPPING'].includes(item.status))
      .reduce((sum, item) => sum + Number(item.payAmount || 0), 0),
    abnormalCount: orderList.filter((item) => ['REFUNDING', 'REJECTED'].includes(item.status)).length
=======
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
>>>>>>> 苏
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

<<<<<<< HEAD
/**
 * 接单操作（本地）
 */
const acceptOrder = (orderNo) => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'PENDING_ACCEPT') return { success: false, message: '当前状态不可接单' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'ACCEPTED', acceptTime: Date.now() }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '接单成功', order: nextList.find((item) => item.orderNo === orderNo) }
}

/**
 * 拒单操作（本地）
 */
const rejectOrder = (orderNo, reason = '') => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'PENDING_ACCEPT') return { success: false, message: '当前状态不可拒单' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'REJECTED', rejectTime: Date.now(), rejectReason: reason }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '已拒单', order: nextList.find((item) => item.orderNo === orderNo) }
}

/**
 * 发货/配送操作（本地）
 */
const shipOrder = (orderNo) => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'ACCEPTED') return { success: false, message: '当前状态不可发货' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'SHIPPING', shipTime: Date.now() }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '已发货', order: nextList.find((item) => item.orderNo === orderNo) }
}

/**
 * 确认完成（配送到达 / 本地）
 */
const completeOrder = (orderNo) => {
  const orderList = getOrderList()
  const target = orderList.find((item) => item.orderNo === orderNo)
  if (!target) return { success: false, message: '订单不存在' }
  if (target.status !== 'SHIPPING') return { success: false, message: '当前状态不可确认完成' }
  const nextList = orderList.map((item) =>
    item.orderNo === orderNo
      ? { ...item, status: 'COMPLETED', completeTime: Date.now() }
      : item
  )
  setOrderList(nextList)
  return { success: true, message: '订单已完成', order: nextList.find((item) => item.orderNo === orderNo) }
}

=======
>>>>>>> 苏
const buildFinanceLedgerList = () => {
  return getOrderList()
    .filter((item) => item.status === 'COMPLETED')
    .map((item, index) => {
      const finishTime = item.verifyTime || item.completeTime || item.payTime
      const settleTime = finishTime + DAY_MILLISECONDS
<<<<<<< HEAD
      return {
        ledgerId: index + 1,
=======
      const transferStatus = settleTime <= Date.now() ? 'ARRIVED' : 'WAITING_T1'
      return {
        ledgerId: index + 1,
        settlementId: index + 1,
>>>>>>> 苏
        orderNo: item.orderNo,
        title: item.title,
        orderAmount: item.payAmount,
        merchantAmount: Math.floor(Number(item.payAmount || 0) * MERCHANT_RATE / 100),
        platformFeeAmount: Math.floor(Number(item.payAmount || 0) * PLATFORM_RATE / 100),
        status: settleTime <= Date.now() ? 'SETTLED' : 'PENDING',
<<<<<<< HEAD
        finishTime,
        settleTime
=======
        transferStatus,
        finishTime,
        settleTime,
        arriveTime: transferStatus === 'ARRIVED' ? settleTime : null,
        transferRemark: transferStatus === 'ARRIVED' ? '微信已自动打款至结算卡' : '订单完成后进入 T+1 自动打款队列'
>>>>>>> 苏
      }
    })
    .sort((a, b) => (b.finishTime || 0) - (a.finishTime || 0))
}

const isSameDay = (time) => time && formatDate(time, 'YYYYMMDD') === formatDate(Date.now(), 'YYYYMMDD')
const isSameMonth = (time) => time && formatDate(time, 'YYYYMM') === formatDate(Date.now(), 'YYYYMM')

<<<<<<< HEAD
const buildFinanceOverview = () => {
  const ledgerList = buildFinanceLedgerList()
  const withdrawList = getWithdrawRecordList().sort((a, b) => (b.applyTime || 0) - (a.applyTime || 0))
  const settledAmount = ledgerList
    .filter((item) => item.status === 'SETTLED')
    .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0)
  const frozenWithdrawAmount = withdrawList.reduce((sum, item) => sum + Number(item.amount || 0), 0)
=======
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
>>>>>>> 苏

  return {
    todayIncomeAmount: ledgerList
      .filter((item) => isSameDay(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
    monthIncomeAmount: ledgerList
      .filter((item) => isSameMonth(item.finishTime))
      .reduce((sum, item) => sum + Number(item.merchantAmount || 0), 0),
<<<<<<< HEAD
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

/**
 * 添加新员工（本地）
 */
=======
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

>>>>>>> 苏
const addStaff = (staffData) => {
  const staffList = getStaffList()
  const maxId = staffList.reduce((max, item) => Math.max(max, Number(item.staffId || 0)), 0)
  const newStaff = {
    staffId: maxId + 1,
    name: staffData.name || '',
    phone: staffData.phone || '',
    roleKey: staffData.roleKey || 'clerk',
    roleName: staffData.roleKey === 'manager' ? '店长' : '店员',
    status: 'ACTIVE',
    permissions: staffData.permissions || ['stats.view', 'order.manage', 'verify.scan', 'verify.manual', 'verify.record']
  }
  setStaffList([...staffList, newStaff])
  return { success: true, message: '员工添加成功', staff: newStaff }
}

<<<<<<< HEAD
/**
 * 编辑员工信息（本地）
 */
=======
>>>>>>> 苏
const updateStaffInfo = (staffId, updates) => {
  const staffList = getStaffList()
  const target = staffList.find((item) => item.staffId === staffId)
  if (!target) return { success: false, message: '员工不存在' }
  const nextList = staffList.map((item) =>
    item.staffId === staffId ? { ...item, ...updates } : item
  )
  setStaffList(nextList)
  return { success: true, message: '员工信息已更新', staff: nextList.find((item) => item.staffId === staffId) }
}

<<<<<<< HEAD
/**
 * 商家取消订单（本地）
 */
=======
>>>>>>> 苏
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

<<<<<<< HEAD
/**
 * 同意退款（本地）
 */
=======
>>>>>>> 苏
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

<<<<<<< HEAD
/**
 * 拒绝退款（本地）
 */
=======
>>>>>>> 苏
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

<<<<<<< HEAD
/**
 * 获取库存预警商品（库存 ≤ 阈值的上架商品）
 */
=======
>>>>>>> 苏
const getLowStockGoods = (threshold = 20) => {
  return getGoodsList().filter((item) => item.status === 'ON_SHELF' && Number(item.stock || 0) <= threshold)
}

<<<<<<< HEAD
/**
 * 批量更新商品状态（本地）
 */
=======
>>>>>>> 苏
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
  navigateTo,
  redirectTo,
  switchTab,
  getOrderStatusMeta,
<<<<<<< HEAD
=======
  isGrouponOrder,
  normalizeGrouponOrders,
>>>>>>> 苏
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
<<<<<<< HEAD
  acceptOrder,
  rejectOrder,
  shipOrder,
  completeOrder,
=======
>>>>>>> 苏
  cancelOrder,
  approveRefundOrder,
  rejectRefundOrder,
  getLowStockGoods,
  batchUpdateGoodsStatus,
  buildFinanceOverview,
<<<<<<< HEAD
  applyWithdraw,
=======
>>>>>>> 苏
  addStaff,
  updateStaffInfo
}
