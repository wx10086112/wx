const util = require('../../utils/util')
const api = require('../../api/index')

Page({
  data: {
    keyword: '',
    hotList: [],
    historyList: [],
    merchantList: [],
    productList: []
  },

  onLoad() {
    const historyList = wx.getStorageSync('search_history') || []
    this.setData({ historyList })
    this.loadSuggestions()
  },

  loadSuggestions() {
    Promise.all([
      api.getMerchantList({}).catch(() => []),
      api.getGrouponList({}).catch(() => [])
    ]).then(([merchantList, grouponList]) => {
      this.setData({
        merchantList: merchantList || [],
        productList: grouponList || []
      })
    })
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
  },

  onProductTap(e) {
    const product = e.currentTarget.dataset.item
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  onMerchantTap(e) {
    const merchant = e.currentTarget.dataset.item
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${merchant.id}`)
  }
})
