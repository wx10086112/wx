const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    overview: {},
    ledgerList: [],
    settlementRecordList: [],
    filterTabs: [
      { label: '全部', value: 'ALL' },
      { label: '今日', value: 'TODAY' },
      { label: '本月', value: 'MONTH' }
    ],
    currentFilter: 'ALL'
  },

  onShow() {
    if (!app.needLogin() || !app.needPermission(['finance.manage'])) return
    this.loadData()
  },

  loadData() {
    api
      .getSettlementOverview()
      .then((overview = {}) => {
        this.renderSettlement(overview)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
      })
  },

  renderSettlement(overview = {}) {
    const filteredLedgerList = this.filterLedgerByDate(overview.ledgerList || [], this.data.currentFilter)
    const ledgerList = filteredLedgerList.slice(0, 20).map((item) => ({
      ...item,
      orderAmountText: util.formatPrice(item.orderAmount),
      merchantAmountText: util.formatPrice(item.merchantAmount),
      platformFeeAmountText: util.formatPrice(item.platformFeeAmount),
      finishTimeText: util.formatDate(item.finishTime),
      settleTimeText: util.formatDate(item.settleTime),
      statusText: item.status === 'SETTLED' ? '已结算' : 'T+1待结算'
    }))
    const settlementRecordList = (overview.settlementRecordList || []).slice(0, 20).map((item) => ({
      ...item,
      amountText: util.formatPrice(item.amount),
      applyTimeText: util.formatDate(item.applyTime),
      expectedTransferTimeText: util.formatDate(item.expectedTransferTime),
      arriveTimeText: item.arriveTime ? util.formatDate(item.arriveTime) : '',
      statusText: this.formatSettlementStatus(item.status)
    }))

    this.setData({
      overview: {
        ...overview,
        todayIncomeText: util.formatPrice(overview.todayIncomeAmount),
        monthIncomeText: util.formatPrice(overview.monthIncomeAmount),
        pendingSettleText: util.formatPrice(overview.pendingSettleAmount),
        settledText: util.formatPrice(overview.settledAmount),
        processingText: util.formatPrice(overview.processingAmount),
        pendingAutoTransferText: util.formatPrice(overview.pendingAutoTransferAmount),
        platformFeeText: util.formatPrice(overview.platformFeeAmount)
      },
      ledgerList,
      settlementRecordList
    })
  },

  formatSettlementStatus(status) {
    const statusMap = {
      WAITING_T1: '待T+1打款',
      TRANSFERRING: '打款中',
      ARRIVED: '已到账',
      FAILED: '到账失败'
    }
    return statusMap[status] || '待处理'
  },

  filterLedgerByDate(ledgerList = [], filter = 'ALL') {
    if (filter === 'ALL') return ledgerList
    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).getTime()

    if (filter === 'TODAY') {
      return ledgerList.filter((item) => (item.finishTime || 0) >= todayStart)
    }
    if (filter === 'MONTH') {
      return ledgerList.filter((item) => (item.finishTime || 0) >= monthStart)
    }
    return ledgerList
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ currentFilter: filter }, () => {
      this.loadData()
    })
  }
})
