const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

const REVENUE_STATUS = ['ARRIVED', 'WAITING_T1', 'TRANSFERRING', 'SETTLED', 'PENDING']

Page({
  data: {
    overview: {},
    dailyFlow: {},
    settlementRecordList: [],
    ledgerList: [],
    flowRecordList: [],
    filterTabs: [
      { label: '全部', value: 'ALL' },
      { label: '待到账', value: 'WAITING' },
      { label: '已到账', value: 'ARRIVED' },
      { label: '失败', value: 'FAILED' }
    ],
    dailyRangeTabs: [
      { label: '今日', value: 'today' },
      { label: '昨日', value: 'yesterday' },
      { label: '近7日', value: 'week' },
      { label: '本月', value: 'month' }
    ],
    statsCards: [],
    dailyStatsCards: [],
    currentFilter: 'ALL',
    currentDailyRange: 'today'
  },

  onShow() {
    if (!app.needMerchantLogin() || !app.needPermission(['finance.manage'])) return
    this.loadData()
  },

  loadData() {
    const settlementRequest = api.getSettlementOverview().catch(() => util.buildFinanceOverview())
    const dailyRequest = api.getDailyFlow(this.data.currentDailyRange).catch(() => null)

    Promise.all([settlementRequest, dailyRequest]).then(([overview = {}, dailyFlow]) => {
      this.renderSettlement(overview)
      this.renderDailyFlow(dailyFlow || this.buildDailyFlowFromOverview(overview))
    })
  },

  renderSettlement(overview = {}) {
    const settlementRecordList = this.filterSettlementRecords(
      overview.settlementRecordList || overview.withdrawList || [],
      this.data.currentFilter
    )
      .slice(0, 20)
      .map((item) => ({
        ...item,
        amountText: util.formatPrice(item.amount),
        applyTimeText: util.formatDate(item.applyTime),
        expectedTransferTimeText: util.formatDate(item.expectedTransferTime),
        arriveTimeText: item.arriveTime ? util.formatDate(item.arriveTime) : '',
        statusText: this.getSettlementStatusText(item.status),
        statusClass: this.getSettlementStatusClass(item.status)
      }))

    const ledgerList = (overview.ledgerList || [])
      .slice(0, 20)
      .map((item) => ({
        ...item,
        merchantAmountText: util.formatPrice(item.merchantAmount),
        platformFeeAmountText: util.formatPrice(item.platformFeeAmount),
        finishTimeText: util.formatDate(item.finishTime),
        settleTimeText: util.formatDate(item.settleTime),
        statusText: item.status === 'SETTLED' ? '已进入到账记录' : '等待 T+1 自动打款',
        statusClass: item.status === 'SETTLED' ? 'arrived' : 'waiting'
      }))

    const statsCards = [
      {
        label: '已到账',
        value: `¥${util.formatPrice(overview.settledAmount)}`,
        tone: 'arrived'
      },
      {
        label: '打款中',
        value: `¥${util.formatPrice(overview.processingAmount)}`,
        tone: 'processing'
      },
      {
        label: '待结算',
        value: `¥${util.formatPrice(overview.pendingSettleAmount)}`,
        tone: 'waiting'
      },
      {
        label: '完成订单',
        value: overview.completedOrderCount || 0,
        tone: 'neutral'
      }
    ]

    this.setData({
      overview: {
        ...overview,
        todayIncomeText: util.formatPrice(overview.todayIncomeAmount),
        monthIncomeText: util.formatPrice(overview.monthIncomeAmount),
        pendingSettleText: util.formatPrice(overview.pendingSettleAmount),
        settledAmountText: util.formatPrice(overview.settledAmount),
        processingAmountText: util.formatPrice(overview.processingAmount),
        pendingAutoTransferText: util.formatPrice(overview.pendingAutoTransferAmount),
        platformFeeText: util.formatPrice(overview.platformFeeAmount),
        nextAutoTransferTimeText: util.formatDate(overview.nextAutoTransferTime)
      },
      statsCards,
      settlementRecordList,
      ledgerList
    })
  },

  renderDailyFlow(dailyFlow = {}) {
    const flowRecordList = (dailyFlow.recordList || []).slice(0, 30).map((item) => {
      const isRefund = item.type === 'refund'
      return {
        ...item,
        amountPrefix: isRefund ? '-' : '+',
        amountTone: isRefund ? 'refund' : 'income',
        orderAmountText: util.formatPrice(item.orderAmount),
        merchantAmountText: util.formatPrice(item.merchantAmount),
        platformFeeAmountText: util.formatPrice(item.platformFeeAmount),
        flowTimeText: util.formatDate(item.flowTime),
        statusText: this.getFlowStatusText(item.status, item.type)
      }
    })

    const dailyStatsCards = [
      {
        label: '流水金额',
        value: `¥${util.formatPrice(dailyFlow.totalAmount)}`,
        tone: 'blue'
      },
      {
        label: '有效订单',
        value: dailyFlow.orderCount || 0,
        tone: 'neutral'
      },
      {
        label: '退款金额',
        value: `¥${util.formatPrice(dailyFlow.refundAmount)}`,
        tone: 'orange'
      },
      {
        label: '预计入账',
        value: `¥${util.formatPrice(dailyFlow.merchantAmount)}`,
        tone: 'green'
      }
    ]

    this.setData({
      dailyFlow: {
        ...dailyFlow,
        totalAmountText: util.formatPrice(dailyFlow.totalAmount),
        merchantAmountText: util.formatPrice(dailyFlow.merchantAmount),
        platformFeeAmountText: util.formatPrice(dailyFlow.platformFeeAmount),
        refundAmountText: util.formatPrice(dailyFlow.refundAmount),
        rangeLabel: this.getRangeLabel(dailyFlow.range || this.data.currentDailyRange)
      },
      dailyStatsCards,
      flowRecordList
    })
  },

  buildDailyFlowFromOverview(overview = {}) {
    const sourceList = overview.ledgerList || []
    const dayMap = {}
    const recordList = sourceList.slice(0, 30).map((item) => {
      const date = this.getDateKey(item.finishTime || item.settleTime || Date.now())
      if (!dayMap[date]) {
        dayMap[date] = {
          date,
          totalAmount: 0,
          merchantAmount: 0,
          platformFeeAmount: 0,
          refundAmount: 0,
          orderCount: 0
        }
      }
      if (REVENUE_STATUS.includes(item.status)) {
        dayMap[date].totalAmount += Number(item.orderAmount || 0)
        dayMap[date].merchantAmount += Number(item.merchantAmount || 0)
        dayMap[date].platformFeeAmount += Number(item.platformFeeAmount || 0)
        dayMap[date].orderCount += 1
      }
      return {
        id: item.ledgerId,
        orderNo: item.orderNo,
        title: item.title,
        type: 'income',
        orderAmount: item.orderAmount,
        merchantAmount: item.merchantAmount,
        platformFeeAmount: item.platformFeeAmount,
        status: item.status,
        flowTime: item.finishTime || item.settleTime
      }
    })

    const dailyList = Object.keys(dayMap)
      .sort()
      .reverse()
      .map((key) => dayMap[key])

    return {
      range: this.data.currentDailyRange,
      totalAmount: dailyList.reduce((sum, item) => sum + item.totalAmount, 0),
      merchantAmount: dailyList.reduce((sum, item) => sum + item.merchantAmount, 0),
      platformFeeAmount: dailyList.reduce((sum, item) => sum + item.platformFeeAmount, 0),
      refundAmount: dailyList.reduce((sum, item) => sum + item.refundAmount, 0),
      orderCount: dailyList.reduce((sum, item) => sum + item.orderCount, 0),
      recordList
    }
  },

  filterSettlementRecords(recordList = [], filter = 'ALL') {
    if (filter === 'ALL') return recordList
    if (filter === 'WAITING') {
      return recordList.filter((item) => ['WAITING_T1', 'TRANSFERRING'].includes(item.status))
    }
    if (filter === 'ARRIVED') {
      return recordList.filter((item) => item.status === 'ARRIVED')
    }
    if (filter === 'FAILED') {
      return recordList.filter((item) => item.status === 'FAILED')
    }
    return recordList
  },

  getSettlementStatusText(status) {
    if (status === 'WAITING_T1') return 'T+1 待打款'
    if (status === 'TRANSFERRING') return '微信处理中'
    if (status === 'ARRIVED') return '已到账'
    if (status === 'FAILED') return '到账失败'
    return '处理中'
  },

  getSettlementStatusClass(status) {
    if (status === 'ARRIVED') return 'arrived'
    if (status === 'FAILED') return 'failed'
    if (status === 'WAITING_T1') return 'waiting'
    return 'processing'
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ currentFilter: filter }, () => {
      this.loadData()
    })
  },

  switchDailyRange(e) {
    const range = e.currentTarget.dataset.range
    if (!range || range === this.data.currentDailyRange) return
    this.setData({ currentDailyRange: range }, () => {
      this.loadData()
    })
  },

  getFlowStatusText(status, type) {
    if (type === 'refund') return '退款'
    if (status === 'ARRIVED' || status === 'SETTLED') return '已结算'
    if (status === 'TRANSFERRING') return '打款中'
    if (status === 'WAITING_T1' || status === 'PENDING') return '待结算'
    return '收入'
  },

  getRangeLabel(range) {
    if (range === 'yesterday') return '昨日'
    if (range === 'week') return '近7日'
    if (range === 'month') return '本月'
    return '今日'
  },

  getDateKey(date) {
    const target = new Date(date)
    if (!(target instanceof Date) || isNaN(target.getTime())) return ''
    const month = `${target.getMonth() + 1}`.padStart(2, '0')
    const day = `${target.getDate()}`.padStart(2, '0')
    return `${target.getFullYear()}-${month}-${day}`
  }
})
