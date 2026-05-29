const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    overview: {},
    settlementAccount: {},
    settlementRecordList: [],
    ledgerList: [],
    filterTabs: [
      { label: '全部', value: 'ALL' },
      { label: '待到账', value: 'WAITING' },
      { label: '已到账', value: 'ARRIVED' },
      { label: '失败', value: 'FAILED' }
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
<<<<<<< HEAD
        this.renderFinance(util.buildFinanceOverview())
=======
        this.renderSettlement(util.buildFinanceOverview())
>>>>>>> 苏
      })
  },

  renderSettlement(overview = {}) {
    const settlementAccount = {
      ...(overview.settlementAccount || {})
    }
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
        statusText: this.getSettlementStatusText(item.status)
      }))

    const ledgerList = (overview.ledgerList || [])
      .slice(0, 20)
      .map((item) => ({
        ...item,
        merchantAmountText: util.formatPrice(item.merchantAmount),
        platformFeeAmountText: util.formatPrice(item.platformFeeAmount),
        finishTimeText: util.formatDate(item.finishTime),
        settleTimeText: util.formatDate(item.settleTime),
        statusText: item.status === 'SETTLED' ? '已进入到账记录' : '等待 T+1 自动打款'
      }))

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
      settlementAccount,
      settlementRecordList,
      ledgerList
    })
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

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ currentFilter: filter }, () => {
      this.loadData()
    })
<<<<<<< HEAD
  },

  handleAmountInput(e) {
    this.setData({
      withdrawAmount: e.detail.value
    })
  },

  submitWithdraw() {
    const amount = Math.round(Number(this.data.withdrawAmount || 0) * 100)
    if (!amount) {
      util.showToast('请输入提现金额')
      return
    }

    api
      .applyFinanceWithdraw({ amount })
      .then(() => {
        util.showToast('提现申请已提交', 'success')
        this.setData({ withdrawAmount: '' })
        this.loadData()
      })
      .catch(() => {
        const result = util.applyWithdraw(amount)
        util.showToast(result.message, result.success ? 'success' : 'none')
        if (result.success) {
          this.setData({ withdrawAmount: '' })
          this.loadData()
        }
      })
=======
>>>>>>> 苏
  }
})
