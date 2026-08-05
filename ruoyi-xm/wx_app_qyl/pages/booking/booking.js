const util = require('../../utils/util')
const templateService = require('../../services/template')
const merchantApi = require('../../api/merchant')
const productApi = require('../../api/product')
const cartStore = require('../../utils/cart')
const { toListThumbnailUrl } = require('../../utils/image-url')

const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'
const PEOPLE_OPTIONS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10+']
const TABLE_OPTIONS = ['1号桌', '2号桌', '3号桌', '4号桌', '5号桌', '6号桌', '7号桌', '8号桌', '包间A', '包间B']

const normalizeText = (value, fallback) => {
  return value === undefined || value === null || value === '' ? fallback : value
}

const extractList = (res = {}) => {
  const payload = res.data || res.rows || res.list || res.records || res
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.rows)) return payload.rows
  if (payload && Array.isArray(payload.list)) return payload.list
  if (payload && Array.isArray(payload.records)) return payload.records
  return []
}

const normalizeMerchant = (merchant = {}) => ({
  ...merchant,
  id: merchant.id || merchant.merchantId || '',
  name: normalizeText(merchant.name || merchant.storeName || merchant.merchantName, '门店信息待完善'),
  address: normalizeText(merchant.address, '门店地址待完善'),
  businessStatus: merchant.businessStatus !== false,
  businessStatusText: merchant.businessStatus === false ? '休息中' : '营业中'
})

const normalizeProduct = (item = {}, merchant = {}) => {
  const price = Number(item.price || 0)
  return {
    ...item,
    id: item.id || item.goodsId || item.productId,
    productId: item.productId || item.id || item.goodsId,
    merchantId: item.merchantId || merchant.id,
    merchantName: item.merchantName || merchant.name,
    title: normalizeText(item.title || item.name || item.productName, '精选服务'),
    subtitle: normalizeText(item.subtitle || item.description || item.remark, '到店使用，按门店规则确认'),
    categoryName: normalizeText(item.categoryName || item.category || item.typeName, '优惠服务'),
    image: toListThumbnailUrl(item.image || item.coverImage || item.productImage || DEFAULT_PRODUCT_IMAGE),
    price,
    priceText: util.formatPrice(price),
    totalSales: Number(item.totalSales || item.sales || 0),
    stock: Number(item.stock || 0),
    soldOut: Number(item.stock || 0) <= 0
  }
}

const buildCategories = (products = []) => {
  const names = []
  products.forEach((product) => {
    const name = product.categoryName || '优惠服务'
    if (!names.includes(name)) names.push(name)
  })
  return ['全部', ...names]
}

Page({
  data: {
    loading: true,
    statusBarHeight: 0,
    merchant: {},
    brandName: '预点单',
    categories: ['全部'],
    activeCategoryIndex: 0,
    productList: [],
    displayProductList: [],
    peopleOptions: PEOPLE_OPTIONS,
    selectedPeople: '1',
    showPeopleDialog: true,
    tableOptions: TABLE_OPTIONS,
    tableIndex: 1,
    tableNo: TABLE_OPTIONS[1],
    cartCount: 0,
    cartAmountText: '0.00'
  },

  onLoad() {
    this.initNavigation()
    this.loadData()
  },

  initNavigation() {
    try {
      const systemInfo = wx.getSystemInfoSync ? wx.getSystemInfoSync() : {}
      this.setData({ statusBarHeight: systemInfo.statusBarHeight || 0 })
    } catch (e) {
      this.setData({ statusBarHeight: 0 })
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
    if (!this.data.loading) {
      this.loadData()
    }
    this.syncCartSummary()
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadData() {
    this.setData({ loading: true })
    const templateConfig = templateService.getTemplateConfig()
    return Promise.all([
      merchantApi.getMerchantList().catch(() => []),
      productApi.getGrouponList().catch(() => [])
    ]).then(([merchantRes, productRes]) => {
      const merchant = normalizeMerchant(extractList(merchantRes)[0] || {})
      const productList = extractList(productRes).map((item) => normalizeProduct(item, merchant))
      const categories = buildCategories(productList)
      this.setData({
        brandName: (templateConfig.brandInfo && templateConfig.brandInfo.name) || merchant.name || '预点单',
        merchant,
        categories,
        productList,
        displayProductList: this.filterProducts(productList, categories[0]),
        activeCategoryIndex: 0,
        loading: false
      })
      this.syncCartSummary()
    }).catch(() => {
      this.setData({
        loading: false,
        merchant: normalizeMerchant({}),
        categories: ['全部'],
        productList: [],
        displayProductList: []
      })
      this.syncCartSummary()
    })
  },

  filterProducts(productList = this.data.productList, categoryName = this.data.categories[this.data.activeCategoryIndex]) {
    if (!categoryName || categoryName === '全部') return productList
    return productList.filter((product) => product.categoryName === categoryName)
  },

  syncCartSummary() {
    const summary = cartStore.buildSummary()
    this.setData({
      cartCount: summary.totalQuantity,
      cartAmountText: summary.totalAmountText
    })
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goMerchantDetail() {
    if (!this.data.merchant.id) {
      util.showToast('暂无门店详情')
      return
    }
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${this.data.merchant.id}`)
  },

  onCategoryTap(e) {
    const index = Number(e.currentTarget.dataset.index || 0)
    const categoryName = this.data.categories[index]
    this.setData({
      activeCategoryIndex: index,
      displayProductList: this.filterProducts(this.data.productList, categoryName)
    })
  },

  onProductTap(e) {
    const product = this.data.displayProductList[Number(e.currentTarget.dataset.index || 0)]
    if (!product || !product.id) return
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  addToCart(e) {
    const product = this.data.displayProductList[Number(e.currentTarget.dataset.index || 0)]
    if (!product) return
    if (product.soldOut) {
      util.showToast('当前商品已售罄')
      return
    }
    const result = cartStore.addItem(product)
    if (result.conflict) {
      util.showModal('更换门店商品', '购物车已有其他门店商品，是否清空后加入当前商品？').then((confirm) => {
        if (!confirm) return
        cartStore.replaceWithItem(result.nextItem)
        this.syncCartSummary()
        util.showToast('已加入购物车', 'success')
      })
      return
    }
    this.syncCartSummary()
    util.showToast(result.ok ? '已加入购物车' : result.message, result.ok ? 'success' : 'none')
  },

  openPeopleDialog() {
    this.setData({ showPeopleDialog: true })
  },

  closePeopleDialog() {
    this.setData({ showPeopleDialog: false })
  },

  noop() {},

  selectPeople(e) {
    this.setData({ selectedPeople: String(e.currentTarget.dataset.value || '1') })
  },

  onTableChange(e) {
    const tableIndex = Number(e.detail.value || 0)
    this.setData({
      tableIndex,
      tableNo: this.data.tableOptions[tableIndex]
    })
  },

  confirmPeopleDialog() {
    this.setData({ showPeopleDialog: false })
  },

  goCheckout() {
    if (!this.data.cartCount) {
      this.goHome()
      return
    }
    util.navigateTo('/pages/cart/cart')
  },

  onShareAppMessage() {
    return {
      title: `${this.data.brandName || '门店'}预点单`,
      path: '/pages/booking/booking'
    }
  }
})
