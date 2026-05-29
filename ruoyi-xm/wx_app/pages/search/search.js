const util = require('../../utils/util')

Page({
  data: {
    keyword: '',
    hotList: ['SPA', '肩颈', '焕肤', '到店核销', '护理', '轻养'],
    historyList: [],
    merchantList: [],
    productList: []
  },

  onLoad() {
    const historyList = wx.getStorageSync('search_history') || []
    this.setData({ historyList })
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    const keyword = (this.data.keyword || '').trim()
    this.saveHistory(keyword)
    util.navigateTo(`/pages/search-result/search-result?keyword=${keyword}`)
  },

  onQuickSearch(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.saveHistory(keyword)
    util.navigateTo(`/pages/search-result/search-result?keyword=${keyword}`)
  },

  onClearHistory() {
    wx.removeStorageSync('search_history')
    this.setData({ historyList: [] })
  },

  saveHistory(keyword) {
    if (!keyword) return
    let list = this.data.historyList.filter((item) => item !== keyword)
    list.unshift(keyword)
    list = list.slice(0, 10)
    wx.setStorageSync('search_history', list)
    this.setData({ historyList: list })
  }
})
