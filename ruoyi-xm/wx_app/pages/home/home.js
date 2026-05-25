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

    const app = getApp()
    const appid = app.appId

    return this.getUserLocation()
      .then((userLocation) => {
        this.setData({ userLocation })

        const templateConfig = templateService.getTemplateConfig()
        this.setData({
          brandInfo: templateConfig.brandInfo,
          homeConfig: templateConfig.home
        })

        return api.getMerchantHome(appid)
      })
      .then((data) => {
        data = data || {}
        const merchant = data.merchant || {}
        const grouponList = data.products || []

        // 如果有定位信息，计算距离
        if (this.data.userLocation && merchant.latitude && merchant.longitude) {
          const dist = this.calcDistance(
            this.data.userLocation.latitude,
            this.data.userLocation.longitude,
            merchant.latitude,
            merchant.longitude
          )
          merchant.distance = dist < 1000 ? Math.round(dist) + 'm' : (dist / 1000).toFixed(1) + 'km'
        }
        merchant.distance = merchant.distance || '距离计算中'

        this.setData({
          currentMerchant: merchant,
          grouponList,
          displayGrouponList: grouponList,
          currentLocation: merchant.distance,
          loading: false
        })
      })
      .catch(() => {
        this.setData({
          currentMerchant: {},
          grouponList: [],
          displayGrouponList: [],
          loading: false,
          currentLocation: '定位失败'
        })
        util.showToast('加载失败，请重试')
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

  calcDistance(lat1, lng1, lat2, lng2) {
    const R = 6371000
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLng = (lng2 - lng1) * Math.PI / 180
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
      + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180)
      * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
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

  onStoreTabTap(e) {
    const tabType = e.currentTarget.dataset.type
    const tabKey = e.currentTarget.dataset.key

    if (tabType === 'products') {
      this.setData({ activeStoreTab: tabKey })
      return
    }

    if (!this.data.currentMerchant.id) {
      util.showToast('暂无商家信息')
      return
    }
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${this.data.currentMerchant.id}`)
  },

  goMerchantDetail() {
    if (!this.data.currentMerchant.id) {
      util.showToast('暂无商家信息')
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
      latitude: merchant.latitude,
      longitude: merchant.longitude,
      name: merchant.name,
      address: merchant.address
    })
  },

  onProductTap(e) {
    const product = e.detail.product
    if (!product || !product.id) return
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  onProductBuy(e) {
    const product = e.detail.product
    if (!product || !product.id) return
    util.navigateTo(`/pages/checkout/checkout?id=${product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.brandInfo.name || '门店服务',
      path: '/pages/home/home'
    }
  }
})
