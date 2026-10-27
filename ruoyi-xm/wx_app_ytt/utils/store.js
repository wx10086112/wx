const STORAGE_KEY = 'current_store'

const getCurrentStore = () => {
  try {
    return wx.getStorageSync(STORAGE_KEY) || null
  } catch (e) {
    return null
  }
}

const setCurrentStore = (store = {}) => {
  if (!store || !store.id) {
    return
  }
  try {
    wx.setStorageSync(STORAGE_KEY, {
      id: store.id,
      merchantId: store.merchantId,
      name: store.name || '',
      address: store.address || '',
      phone: store.phone || '',
      businessHours: store.businessHours || '',
      latitude: store.latitude,
      longitude: store.longitude,
      distanceText: store.distanceText || '',
      savedAt: Date.now()
    })
  } catch (e) {
    // ignore storage quota errors
  }
}

const clearCurrentStore = () => {
  try {
    wx.removeStorageSync(STORAGE_KEY)
  } catch (e) {
    // ignore
  }
}

module.exports = {
  getCurrentStore,
  setCurrentStore,
  clearCurrentStore
}
