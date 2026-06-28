const util = require('../../utils/util')
const templateService = require('../../services/template')
const merchantApi = require('../../api/merchant')
const productApi = require('../../api/product')
const privacy = require('../../utils/privacy')
const { toListThumbnailUrl } = require('../../utils/image-url')

const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

const normalizeText = (value, fallback) => {
  return value === undefined || value === null || value === '' ? fallback : value
}

Page({
  data: {
    brandInfo: {},
    homeConfig: {},
    currentLocation: '定位中...',
    userLocation: null,
    hasMerchantData: false,
    currentMerchant: {},
    merchantList: [],
    grouponList: [],
    displayGrouponList: [],
    loading: true
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadData() {
    this.setData({
      loading: true,
      currentLocation: '定位中...'
    })

    return this.getUserLocation()
      .then((userLocation) => {
        const templateConfig = templateService.getTemplateConfig()

        const merchantParams = {}
        if (userLocation) {
          merchantParams.latitude = userLocation.latitude
          merchantParams.longitude = userLocation.longitude
        }

        return Promise.all([
          merchantApi.getMerchantList(merchantParams),
          productApi.getGrouponList()
        ]).then(([merchantRes, grouponRes]) => {
          const merchantPayload = merchantRes.data || merchantRes || []
          const merchantList = (Array.isArray(merchantPayload) ? merchantPayload : []).map((m) => this.normalizeMerchant(m))
          const grouponList = (grouponRes.data || grouponRes || []).map((item) => ({
            ...item,
            title: normalizeText(item.title || item.name || item.productName, '精选服务'),
            image: toListThumbnailUrl(item.image || item.coverImage || item.productImage || DEFAULT_PRODUCT_IMAGE),
            soldOut: Number(item.stock || 0) <= 0,
            tags: item.tags || []
          }))

          const currentMerchant = merchantList[0] || this.buildEmptyMerchant()
          const hasMerchantData = !!currentMerchant.id

          const filteredData = this.buildFilteredLists({ merchantList, grouponList })
          this.setData({
            brandInfo: templateConfig.brandInfo,
            homeConfig: templateConfig.home,
            userLocation,
            currentLocation: hasMerchantData ? this.formatCurrentDistance(currentMerchant) : '暂未获取门店信息',
            hasMerchantData,
            currentMerchant,
            merchantList,
            grouponList,
            displayGrouponList: filteredData.displayGrouponList,
            loading: false
          })
        })
      })
      .catch(() => {
        this.setData({
          currentLocation: '暂未获取门店信息',
          hasMerchantData: false,
          currentMerchant: this.buildEmptyMerchant(),
          merchantList: [],
          grouponList: [],
          displayGrouponList: [],
          loading: false
        })
      })
  },

  getUserLocation() {
    return new Promise((resolve) => {
      privacy.ensurePrivacyAuthorized().then((authorized) => {
        if (!authorized) {
          resolve(null)
          return
        }

        wx.getLocation({
          type: 'gcj02',
          success: (res) => {
            resolve({
              latitude: res.latitude,
              longitude: res.longitude,
              accuracy: res.accuracy
            })
          },
          fail: () => {
            resolve(null)
          }
        })
      })
    })
  },

  formatCurrentDistance(merchant = {}) {
    return merchant.distance || ''
  },

  normalizeMerchant(m = {}) {
    const businessStatus = m.businessStatus !== false
    const tags = Array.isArray(m.tags) ? m.tags : []
    return {
      ...m,
      name: normalizeText(m.name || m.storeName || m.merchantName, '门店信息待完善'),
      address: normalizeText(m.address, '门店地址待完善'),
      sales: normalizeText(m.sales || m.monthSales, 0),
      distance: m.distance || '',
      businessStatus,
      businessStatusText: businessStatus ? '营业中' : '休息中',
      bookingText: m.supportBooking === false ? '到店即用' : '可预约',
      displayTags: tags.filter((tag) => !['营业中', '休息中'].includes(tag)).slice(0, 3)
    }
  },

  buildEmptyMerchant() {
    return {
      id: '',
      name: '暂无门店信息',
      address: '暂无门店地址',
      sales: 0,
      distance: '',
      businessStatus: false,
      businessStatusText: '未营业',
      bookingText: '',
      displayTags: []
    }
  },

  buildFilteredLists({ grouponList = [] }) {
    return {
      displayGrouponList: grouponList
    }
  },

  refreshLocation() {
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userLocation'] === false) {
          wx.showModal({
            title: '开启定位',
            content: '需要获取当前位置，用于计算你到本店的距离。',
            success: (modalRes) => {
              if (!modalRes.confirm) return
              wx.openSetting({
                success: () => {
                  this.loadData()
                }
              })
            }
          })
          return
        }

        this.loadData()
      },
      fail: () => {
        this.loadData()
      }
    })
  },

  goMerchantDetail() {
    if (!this.data.currentMerchant.id) {
      util.showToast('暂无商家详情')
      return
    }
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${this.data.currentMerchant.id}`)
  },

  callMerchant() {
    if (!this.data.currentMerchant.phone) {
      util.showToast('暂无联系电话')
      return
    }
    wx.makePhoneCall({
      phoneNumber: this.data.currentMerchant.phone
    })
  },

  openMerchantMap() {
    const merchant = this.data.currentMerchant
    if (!merchant.latitude || !merchant.longitude) {
      util.showToast('暂无门店位置')
      return
    }
    wx.openLocation({
      latitude: Number(merchant.latitude),
      longitude: Number(merchant.longitude),
      name: merchant.name,
      address: merchant.address
    })
  },

  onProductTap(e) {
    const product = e.detail.product
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  onProductBuy(e) {
    const product = e.detail.product
    if (!product || product.soldOut || Number(product.stock || 0) <= 0) {
      util.showToast('当前商品已售罄')
      return
    }
    util.navigateTo(`/pages/checkout/checkout?id=${product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.brandInfo.name || '门店服务',
      path: '/pages/home/home'
    }
  }
})
