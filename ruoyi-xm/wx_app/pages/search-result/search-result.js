const mock = require('../../data/mock')

Page({
  data: {
    keyword: '',
    merchantList: [],
    productList: []
  },

  onLoad(options) {
    const keyword = options.keyword || ''
    const merchantList = mock.merchantList.filter((item) => item.name.includes(keyword) || item.categoryName.includes(keyword))
    const productList = mock.grouponList.filter((item) => item.title.includes(keyword) || item.subtitle.includes(keyword))
    this.setData({
      keyword,
      merchantList,
      productList
    })
  }
})
