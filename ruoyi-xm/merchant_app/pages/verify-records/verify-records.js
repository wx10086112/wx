const util = require('../../utils/util')
const api = require('../../api/index')

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
    if (!app.needLogin() || !app.needPermission(['verify.record', 'verify.scan', 'verify.manual'])) return
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
      .map((item) => ({
        ...item,
        verifyTimeText: util.formatDate(item.verifyTime),
        payAmountText: util.formatPrice(item.payAmount),
        statusText: item.status === 'FAILED' ? '异常' : '成功',
        statusClass: item.status === 'FAILED' ? 'orange' : 'green'
      }))

    this.setData({ recordList })
  }
})
