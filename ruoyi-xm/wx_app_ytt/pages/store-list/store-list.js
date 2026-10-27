const merchantApi = require('../../api/merchant')
const storeUtil = require('../../utils/store')

const toRad = (value) => (Number(value) * Math.PI) / 180

const calcDistance = (lat1, lng1, lat2, lng2) => {
  const earthRadius = 6371000
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
  return 2 * earthRadius * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const formatDistance = (meters) => {
  return meters < 1000 ? Math.round(meters) + 'm' : (meters / 1000).toFixed(1) + 'km'
}

Page({
  data: {
    merchantId: null,
    merchantName: '',
    stores: [],
    currentStoreId: null,
    loading: true,
    errorText: ''
  },

  onLoad(options) {
    const merchantId = parseInt(options.id, 10)
    const current = storeUtil.getCurrentStore()
    this.setData({
      merchantId: Number.isNaN(merchantId) ? null : merchantId,
      currentStoreId: current ? current.id : null
    })
    this.loadStores()
  },

  loadStores() {
    this.setData({ loading: true, errorText: '' })
    this.resolveMerchantId()
      .then((merchantId) => this.fetchDetail(merchantId))
      .catch(() => {
        this.setData({ loading: false, errorText: '门店加载失败，请重试' })
      })
  },

  resolveMerchantId() {
    if (this.data.merchantId) {
      return Promise.resolve(this.data.merchantId)
    }
    return merchantApi.getMerchantList({}).then((res) => {
      const payload = res.data || res || []
      const first = Array.isArray(payload) ? payload[0] : null
      const merchantId = first && (first.merchantId || first.id)
      if (!merchantId) {
        throw new Error('no merchant')
      }
      this.setData({ merchantId })
      return merchantId
    })
  },

  fetchDetail(merchantId) {
    return merchantApi.getMerchantDetail(merchantId).then((res) => {
      const data = res.data || res || {}
      const storeList = Array.isArray(data.storeList) ? data.storeList : []
      return this.applyLocation(storeList).then((located) => {
        this.setData({
          merchantName: data.name || '',
          stores: located,
          loading: false
        })
      })
    })
  },

  applyLocation(storeList) {
    const list = storeList.map((store) => ({ ...store }))
    return new Promise((resolve) => {
      wx.getLocation({
        type: 'gcj02',
        success: (location) => {
          list.forEach((store) => {
            if (store.latitude && store.longitude) {
              const meters = calcDistance(location.latitude, location.longitude,
                Number(store.latitude), Number(store.longitude))
              store.distanceValue = meters
              store.distanceText = formatDistance(meters)
            }
          })
          list.sort((a, b) => {
            if (a.distanceValue == null && b.distanceValue == null) {
              return (b.isMain || 0) - (a.isMain || 0)
            }
            if (a.distanceValue == null) return 1
            if (b.distanceValue == null) return -1
            return a.distanceValue - b.distanceValue
          })
          resolve(list)
        },
        fail: (err) => {
          console.warn('[getLocation fail]', err)
          list.forEach((store) => {
            store.distanceText = ''
          })
          resolve(list)
        }
      })
    })
  },

  selectStore(e) {
    const store = this.data.stores[e.currentTarget.dataset.index]
    if (!store) {
      return
    }
    storeUtil.setCurrentStore({ ...store, merchantId: this.data.merchantId })
    this.setData({ currentStoreId: store.id })
    wx.showToast({ title: '已切换门店', icon: 'success' })
    setTimeout(() => {
      wx.navigateBack()
    }, 300)
  },

  openLocation(e) {
    const store = this.data.stores[e.currentTarget.dataset.index]
    if (!store) {
      return
    }
    if (!store.latitude || !store.longitude) {
      wx.showToast({ title: '该门店暂未配置位置', icon: 'none' })
      return
    }
    wx.openLocation({
      latitude: Number(store.latitude),
      longitude: Number(store.longitude),
      name: store.name,
      address: store.address || ''
    })
  },

  callStore(e) {
    const store = this.data.stores[e.currentTarget.dataset.index]
    if (!store || !store.phone) {
      wx.showToast({ title: '暂无联系电话', icon: 'none' })
      return
    }
    wx.makePhoneCall({ phoneNumber: String(store.phone) })
  }
})
