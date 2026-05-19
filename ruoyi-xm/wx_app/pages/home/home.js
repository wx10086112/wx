const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const api = require('../../api/index')

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
    storeTabs: [
      { label: '团购优惠', key: 'deals', type: 'products' },
      { label: '服务项目', key: 'services', type: 'products' },
      { label: '商家信息', key: 'info', type: 'info' }
    ],
    activeStoreTab: 'deals',
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
        this.setData({ userLocation })

        const templateConfig = templateService.getTemplateConfig()
        this.setData({
          brandInfo: templateConfig.brandInfo,
          homeConfig: templateConfig.home
        })

        return Promise.all([
          api.getMerchantList({
            latitude: userLocation ? userLocation.latitude : null,
            longitude: userLocation ? userLocation.longitude : null
          }).catch(() => null),
          api.getGrouponList({}).catch(() => null)
        ])
      })
      .then(([merchantResult, grouponResult]) => {
        const merchantList = merchantResult || mock.merchantList
        const grouponList = grouponResult || mock.grouponList

        const filteredData = this.buildFilteredLists({
          merchantList,
          grouponList
        })
        this.setData({
          merchantList,
          grouponList,
          currentLocation: this.formatCurrentDistance(merchantList[0]),
          currentMerchant: merchantList[0] || {},
          displayMerchantList: filteredData.displayMerchantList,
          displayGrouponList: filteredData.displayGrouponList,
          loading: false
        })
      })
      .catch(() => {
        this.setData({
          merchantList: mock.merchantList,
          grouponList: mock.grouponList,
          currentMerchant: mock.merchantList[0] || {},
          displayMerchantList: mock.merchantList,
          displayGrouponList: mock.grouponList,
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

  buildFilteredLists({ merchantList = [], grouponList = [] }) {
    return {
      displayMerchantList: merchantList,
      displayGrouponList: grouponList
    }
  },

  applyFilters() {
    const { merchantList, grouponList } = this.data
    const filteredData = this.buildFilteredLists({
      merchantList,
      grouponList
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
          activeStoreTab: tabKey
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
      title: this.data.brandInfo.name || '门店服务',
      path: '/pages/home/home'
    }
  }
})
