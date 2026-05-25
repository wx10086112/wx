const util = require('../../utils/util')
const api = require('../../api/index')

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
    totalResultCount: 0
  },

  onLoad(options) {
    const keyword = options.keyword || ''
    this.setData({ keyword })
    this.doSearch(keyword)
  },

  doSearch(keyword) {
    Promise.all([
      api.getMerchantList({ keyword }).catch(() => []),
      api.getGrouponList({ keyword }).catch(() => [])
    ]).then(([merchantList, grouponList]) => {
      merchantList = merchantList || []
      grouponList = grouponList || []
      this.setData({
        productList: grouponList,
        merchantList,
        totalResultCount: grouponList.length + merchantList.length
      })
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
      this.setData({ priceAsc: !this.data.priceAsc })
      return
    }
    this.setData({ sortKey })
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
