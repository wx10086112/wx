const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')

Page({
  data: {
    brandInfo: {},
    homeConfig: {},
    currentLocation: '定位中...',
    userLocation: null,
    currentMerchant: {},
    merchantList: [],
    displayMerchantList: [],
    grouponList: [],
    displayGrouponList: [],
    bannerList: [],
    categoryList: [],
    storeTabs: [
      { label: '团购优惠', key: 'deals', type: 'products' },
      { label: '服务项目', key: 'services', type: 'products' },
      { label: '用户评价', key: 'reviews', type: 'reviews' },
      { label: '商家信息', key: 'info', type: 'info' }
    ],
    activeStoreTab: 'deals',
    selectedCategoryId: 0,
    sortType: 'sales',
    sortOptions: [],
    loading: true
  },

  onLoad() {
    this.loadData()
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
        const merchantList = this.buildLocatedMerchantList(mock.merchantList, userLocation)
        const sortOptions = this.buildSortOptions(templateConfig.home.sortOptions)
        const filteredData = this.buildFilteredLists({
          merchantList,
          grouponList: mock.grouponList,
          selectedCategoryId: this.data.selectedCategoryId,
          sortType: this.data.sortType
        })
        this.setData({
          brandInfo: templateConfig.brandInfo,
          homeConfig: templateConfig.home,
          userLocation,
          currentLocation: this.formatCurrentDistance(merchantList[0]),
          currentMerchant: merchantList[0] || {},
          merchantList,
          grouponList: mock.grouponList,
          bannerList: mock.bannerList,
          categoryList: mock.categoryList,
          sortOptions,
          displayMerchantList: filteredData.displayMerchantList,
          displayGrouponList: filteredData.displayGrouponList,
          loading: false
        })
      })
  },

  getUserLocation() {
    return new Promise((resolve) => {
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
  },

  formatCurrentDistance(merchant = {}) {
    return merchant.distance || '距离计算中'
  },

  toRadians(value) {
    return (value * Math.PI) / 180
  },

  calculateDistanceMeters(userLocation, merchant = {}) {
    if (!userLocation || !merchant.latitude || !merchant.longitude) {
      return Number(merchant.distanceValue || 0)
    }

    const earthRadius = 6371000
    const lat1 = this.toRadians(userLocation.latitude)
    const lat2 = this.toRadians(Number(merchant.latitude))
    const deltaLat = this.toRadians(Number(merchant.latitude) - userLocation.latitude)
    const deltaLng = this.toRadians(Number(merchant.longitude) - userLocation.longitude)
    const a =
      Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
      Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return Math.round(earthRadius * c)
  },

  formatDistance(distanceValue = 0) {
    if (distanceValue < 1000) {
      return `${Math.max(distanceValue, 0)}m`
    }

    if (distanceValue < 10000) {
      return `${(distanceValue / 1000).toFixed(1)}km`
    }

    return `${Math.round(distanceValue / 1000)}km`
  },

  buildLocatedMerchantList(merchantList = [], userLocation) {
    return merchantList.map((merchant) => {
      const distanceValue = this.calculateDistanceMeters(userLocation, merchant)
      return {
        ...merchant,
        distanceValue,
        distance: this.formatDistance(distanceValue),
        businessStatusText: merchant.businessStatus ? '营业中' : '休息中',
        bookingText: merchant.supportBooking === false ? '到店即用' : '可预约',
        displayTags: (merchant.tags || []).filter((tag) => tag !== '营业中' && tag !== '休息中')
      }
    })
  },

  buildSortOptions(sortOptions = []) {
    return sortOptions.filter((item) => item.value !== 'distance')
  },

  buildFilteredLists({ merchantList = [], grouponList = [], selectedCategoryId = 0, sortType = 'sales' }) {
    let filteredMerchants = merchantList.slice()
    let filteredProducts = grouponList.slice()

    if (selectedCategoryId) {
      filteredProducts = filteredProducts.filter((item) => item.categoryId === selectedCategoryId)
    }

    if (sortType === 'distance') {
      filteredMerchants.sort((a, b) => a.distanceValue - b.distanceValue)
      filteredProducts.sort((a, b) => {
        const merchantA = merchantList.find((m) => m.id === a.merchantId) || {}
        const merchantB = merchantList.find((m) => m.id === b.merchantId) || {}
        return (merchantA.distanceValue || 0) - (merchantB.distanceValue || 0)
      })
    }

    if (sortType === 'sales') {
      filteredMerchants.sort((a, b) => b.sales - a.sales)
      filteredProducts.sort((a, b) => b.sales - a.sales)
    }

    if (sortType === 'price') {
      filteredProducts.sort((a, b) => a.price - b.price)
      filteredMerchants.sort((a, b) => a.distanceValue - b.distanceValue)
    }

    return {
      displayMerchantList: filteredMerchants,
      displayGrouponList: filteredProducts
    }
  },

  applyFilters() {
    const { merchantList, grouponList, selectedCategoryId, sortType } = this.data
    const filteredData = this.buildFilteredLists({
      merchantList,
      grouponList,
      selectedCategoryId,
      sortType
    })
    this.setData({
      displayMerchantList: filteredData.displayMerchantList,
      displayGrouponList: filteredData.displayGrouponList
    })
  },

  onSearchTap() {
    util.navigateTo('/pages/search/search')
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

  onBannerTap(e) {
    const banner = e.currentTarget.dataset.banner
    if (banner.linkType === 'merchant') {
      util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${banner.linkId}`)
      return
    }
    util.navigateTo(`/pages/product-detail/product-detail?id=${banner.linkId}`)
  },

  onCategoryTap(e) {
    const categoryId = e.currentTarget.dataset.id
    this.setData(
      {
        selectedCategoryId: categoryId
      },
      () => {
        this.applyFilters()
      }
    )
  },

  onSortTap(e) {
    const sortType = e.currentTarget.dataset.sort
    this.setData(
      {
        sortType
      },
      () => {
        this.applyFilters()
      }
    )
  },

  onMerchantTap(e) {
    const merchant = e.currentTarget.dataset.merchant
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${merchant.id}`)
  },

  onStoreTabTap(e) {
    const tabType = e.currentTarget.dataset.type
    const tabKey = e.currentTarget.dataset.key

    if (tabType === 'products') {
      this.setData(
        {
          activeStoreTab: tabKey,
          selectedCategoryId: 0
        },
        () => {
          this.applyFilters()
        }
      )
      return
    }

    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${this.data.currentMerchant.id}`)
  },

  goMerchantDetail() {
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
      latitude: merchant.latitude,
      longitude: merchant.longitude,
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
    util.navigateTo(`/pages/checkout/checkout?id=${product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.brandInfo.name || '门店团购服务',
      path: '/pages/home/home'
    }
  }
})
