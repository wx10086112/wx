const mock = require('../../data/mock')
const util = require('../../utils/util')

Page({
  data: {
    keyword: '',
    hotList: ['SPA', '肩颈', '焕肤', '到店核销'],
    merchantList: mock.merchantList,
    productList: mock.grouponList.slice(0, 3)
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    const keyword = this.data.keyword.trim()
    util.navigateTo(`/pages/search-result/search-result?keyword=${keyword}`)
  },

  onQuickSearch(e) {
    const keyword = e.currentTarget.dataset.keyword
    util.navigateTo(`/pages/search-result/search-result?keyword=${keyword}`)
  }
})
