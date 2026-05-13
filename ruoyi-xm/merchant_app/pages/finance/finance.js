const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    overview: {},
    ledgerList: [],
    withdrawList: [],
    withdrawAmount: ''
  },

  onShow() {
    if (!app.needLogin() || !app.needPermission(['finance.manage'])) return
    this.loadData()
  },

  loadData() {
    api
      .getFinanceOverview()
      .then((overview = {}) => {
        this.renderFinance(overview)
      })
      .catch(() => {
        this.renderFinance(util.buildFinanceOverview())
      })
  },

  renderFinance(overview = {}) {
    const ledgerList = (overview.ledgerList || []).slice(0, 10).map((item) => ({
      ...item,
      orderAmountText: util.formatPrice(item.orderAmount),
      merchantAmountText: util.formatPrice(item.merchantAmount),
      platformFeeAmountText: util.formatPrice(item.platformFeeAmount),
      finishTimeText: util.formatDate(item.finishTime),
      settleTimeText: util.formatDate(item.settleTime),
      statusText: item.status === 'SETTLED' ? '已结算' : 'T+1待结算'
    }))
    const withdrawList = (overview.withdrawList || []).slice(0, 10).map((item) => ({
      ...item,
      amountText: util.formatPrice(item.amount),
      applyTimeText: util.formatDate(item.applyTime),
      statusText: item.status === 'PROCESSING' ? '处理中' : '已到账'
    }))

    this.setData({
      overview: {
        ...overview,
        todayIncomeText: util.formatPrice(overview.todayIncomeAmount),
        monthIncomeText: util.formatPrice(overview.monthIncomeAmount),
        pendingSettleText: util.formatPrice(overview.pendingSettleAmount),
        withdrawableText: util.formatPrice(overview.withdrawableAmount),
        platformFeeText: util.formatPrice(overview.platformFeeAmount)
      },
      ledgerList,
      withdrawList
    })
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
  }
})
