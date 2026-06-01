const mock = require('../../data/mock')
const util = require('../../utils/util')
<<<<<<< HEAD
=======
const merchantApi = require('../../api/merchant')
const productApi = require('../../api/product')
>>>>>>> 苏

const SORT_OPTIONS = [
  { label: '综合', value: 'default' },
  { label: '距离', value: 'distance' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' }
]

Page({
  data: {
    keyword: '',
    sortKey: 'default',
    sortOptions: SORT_OPTIONS,
    priceAsc: true,
    productList: [],
    merchantList: [],
    totalResultCount: 0,
    loading: true
  },

  onLoad(options) {
    const keyword = options.keyword || ''
    this.setData({ keyword })
    this.doSearch(keyword, 'default')
  },

<<<<<<< HEAD
  doSearch(keyword, sortKey) {
    let productList = mock.grouponList.filter((item) => {
      if (!keyword) return true
      return (
        item.title.includes(keyword) ||
        item.subtitle.includes(keyword) ||
        item.categoryName.includes(keyword) ||
        item.description.includes(keyword)
      )
    })

    let merchantList = mock.merchantList.filter((item) => {
      if (!keyword) return true
      return (
        item.name.includes(keyword) ||
        item.shortName.includes(keyword) ||
        (item.tags || []).some((tag) => tag.includes(keyword))
      )
    })

    productList = this.sortList(productList, sortKey)
    merchantList = this.sortList(merchantList, sortKey)

    this.setData({
      productList,
      merchantList,
      sortKey,
      totalResultCount: productList.length + merchantList.length
=======
  doSearch(keyword) {
    this.setData({ loading: true })

    Promise.all([
      productApi.getGrouponList({ keyword }).catch(() => ({ data: [] })),
      merchantApi.getMerchantList({ keyword }).catch(() => ({ data: [] }))
    ]).then(([productRes, merchantRes]) => {
      let productList = (productRes.data || productRes || [])
      let merchantList = (merchantRes.data || merchantRes || [])

      if (keyword) {
        productList = productList.filter((item) =>
          (item.title || '').includes(keyword) ||
          (item.subtitle || '').includes(keyword) ||
          (item.merchantName || '').includes(keyword)
        )
        merchantList = merchantList.filter((item) =>
          (item.name || '').includes(keyword) ||
          (item.shortName || '').includes(keyword) ||
          (item.tags || []).some((tag) => tag.includes(keyword))
        )
      }

      productList = this.sortList(productList, this.data.sortKey)
      merchantList = this.sortList(merchantList, this.data.sortKey)

      this.setData({
        productList,
        merchantList,
        totalResultCount: productList.length + merchantList.length,
        loading: false
      })
>>>>>>> 苏
    })
  },

  sortList(list, sortKey) {
    const sorted = [...list]
    if (sortKey === 'distance') {
      sorted.sort((a, b) => (a.distanceValue || 0) - (b.distanceValue || 0))
    } else if (sortKey === 'sales') {
      sorted.sort((a, b) => (b.sales || 0) - (a.sales || 0))
    } else if (sortKey === 'price') {
      if (this.data.priceAsc) {
        sorted.sort((a, b) => (a.price || 0) - (b.price || 0))
      } else {
        sorted.sort((a, b) => (b.price || 0) - (a.price || 0))
      }
    }
    return sorted
  },

  onSortTap(e) {
    const sortKey = e.currentTarget.dataset.key
    if (sortKey === 'price' && this.data.sortKey === 'price') {
      this.setData({ priceAsc: !this.data.priceAsc }, () => {
<<<<<<< HEAD
        this.doSearch(this.data.keyword, sortKey)
      })
      return
    }
    this.doSearch(this.data.keyword, sortKey)
=======
        this.doSearch(this.data.keyword)
      })
      return
    }
    this.setData({ sortKey }, () => {
      this.doSearch(this.data.keyword)
    })
>>>>>>> 苏
  },

  onProductTap(e) {
    const product = e.detail.product
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  onProductBuy(e) {
    const product = e.detail.product
    util.navigateTo(`/pages/checkout/checkout?id=${product.id}`)
  },

  onMerchantTap(e) {
    const merchant = e.currentTarget.dataset.item
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${merchant.id}`)
  }
})
