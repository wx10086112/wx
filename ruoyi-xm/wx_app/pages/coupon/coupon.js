const mock = require('../../data/mock')

const couponStatusMap = {
  AVAILABLE: '可使用',
  USED: '已使用',
  EXPIRED: '已过期'
}

Page({
  data: {
    couponList: mock.couponList.map((item) => ({
      ...item,
      statusText: couponStatusMap[item.status] || '不可用',
      thresholdText: (item.thresholdAmount / 100).toFixed(0),
      amountText: (item.amount / 100).toFixed(0)
    }))
  }
})
