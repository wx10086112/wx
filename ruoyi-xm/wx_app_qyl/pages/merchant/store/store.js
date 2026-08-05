const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

Page({
  data: {
    canManageStore: false,
    form: {
      storeName: '',
      brandSlogan: '',
      businessHours: '',
      phone: '',
      address: '',
      businessStatus: true,
      supportRefund: true,
      supportBooking: true
    }
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    const canManageStore = app.hasAnyPermission(['store.manage'])
    this.setData({ canManageStore })
    if (canManageStore) {
      this.loadData()
    }
  },

  loadData() {
    api
      .getMerchantProfile()
      .then((storeInfo = {}) => {
        util.setStoreInfo(storeInfo)
        this.renderStoreForm(storeInfo)
      })
      .catch((err = {}) => {
        util.showToast(err.message || '门店信息加载失败')
      })
  },

  renderStoreForm(storeInfo = {}) {
    this.setData({
      form: {
        storeName: storeInfo.storeName || '',
        brandSlogan: storeInfo.brandSlogan || '',
        businessHours: storeInfo.businessHours || '',
        phone: storeInfo.phone || '',
        address: storeInfo.address || '',
        businessStatus: storeInfo.businessStatus !== false,
        supportRefund: storeInfo.supportRefund !== false,
        supportBooking: storeInfo.supportBooking !== false
      }
    })
  },

  handleInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`form.${key}`]: e.detail.value
    })
  },

  handleSwitchChange(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`form.${key}`]: e.detail.value
    })
  },

  saveStore() {
    const form = this.data.form
    const storeInfo = {
      storeName: form.storeName,
      brandSlogan: form.brandSlogan,
      businessHours: form.businessHours,
      phone: form.phone,
      address: form.address,
      businessStatus: form.businessStatus,
      supportRefund: form.supportRefund,
      supportBooking: form.supportBooking
    }

    api
      .updateMerchantProfile(storeInfo)
      .then((savedStoreInfo) => {
        util.setStoreInfo(savedStoreInfo || storeInfo)
        util.showToast('门店信息已保存', 'success')
        this.renderStoreForm(savedStoreInfo || storeInfo)
      })
      .catch((err = {}) => {
        util.showToast(err.message || '门店信息保存失败')
      })
  }
})
