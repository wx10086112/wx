const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

Page({
  data: {
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '成功', value: 'SUCCESS' },
      { label: '异常', value: 'FAILED' }
    ],
    currentTab: 'ALL',
    recordList: []
  },

  onShow() {
    if (!app.needMerchantLogin() || !app.needPermission(['verify.record', 'verify.scan', 'verify.manual'])) return
    this.loadData()
  },

  switchTab(e) {
    this.setData({
      currentTab: e.currentTarget.dataset.tab
    }, () => this.loadData())
  },

  loadData() {
    api
      .getVerifyRecordList({
        status: this.data.currentTab === 'ALL' ? '' : this.data.currentTab
      })
      .then((recordList = []) => {
        util.setVerifyRecordList(recordList)
        this.renderRecords(recordList)
      })
      .catch(() => {
        this.renderRecords(util.getVerifyRecordList())
      })
  },

  renderRecords(sourceList = []) {
    const recordList = sourceList
      .filter((item) => this.data.currentTab === 'ALL' || item.status === this.data.currentTab)
      .sort((a, b) => (b.verifyTime || 0) - (a.verifyTime || 0))
      .map((item) => {
        const isFailed = item.status === 'FAILED'
        const hasOrder = !!item.orderNo
        const writeOffCode = item.writeOffCode || ''
        const displayTitle = hasOrder ? item.title : (isFailed ? '核销失败' : item.title)
        const codeMetaText = hasOrder
          ? `核销码 ${writeOffCode || '-'} · 订单 ${item.orderNo}`
          : ''

        return {
          ...item,
          displayTitle,
          codeMetaText,
          verifyTimeText: util.formatDate(item.verifyTime),
          payAmountText: util.formatPrice(item.payAmount),
          statusText: isFailed ? '异常' : '成功',
          statusClass: isFailed ? 'orange' : 'green'
        }
      })

    this.setData({ recordList })
  }
})
