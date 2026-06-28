const util = require('../../utils/util')
const merchantApi = require('../../api/merchant')
const { toDetailThumbnailUrl } = require('../../utils/image-url')

const normalizeMerchantDetail = (merchant = {}) => {
  const tags = Array.isArray(merchant.tags) ? merchant.tags : []
  const serviceAbilityTags = Array.isArray(merchant.serviceAbilityTags) ? merchant.serviceAbilityTags : []
  const facilityTags = Array.isArray(merchant.facilityTags) ? merchant.facilityTags : []
  const albumList = Array.isArray(merchant.albumList) ? merchant.albumList.filter(Boolean) : []
  const heroImage = toDetailThumbnailUrl(merchant.coverImage || albumList[0] || merchant.avatar || '')

  return {
    ...merchant,
    heroImage,
    serviceAbilityTags,
    facilityTags,
    displayTags: tags.filter((tag) => !['营业中', '休息中'].includes(tag)).slice(0, 3),
    businessStatusText: merchant.businessStatus ? '营业中' : '休息中',
    bookingText: merchant.supportBooking === false ? '到店即用' : '可预约'
  }
}

Page({
  data: {
    merchantId: null,
    merchant: {},
    storeList: [],
    loading: true,
    loadFailed: false
  },

  onLoad(options) {
    const merchantId = parseInt(options.id, 10)
    this.setData({
      merchantId: Number.isNaN(merchantId) ? null : merchantId
    })
    this.loadMerchantDetail()
  },

  loadMerchantDetail() {
    if (!this.data.merchantId) {
      this.setData({ loading: false, loadFailed: true })
      util.showToast('商家信息不存在')
      return
    }

    this.setData({ loading: true, loadFailed: false })

    merchantApi.getMerchantDetail(this.data.merchantId)
      .then((res) => {
        const detailData = res.data || res || {}
        const merchant = normalizeMerchantDetail(detailData.merchant || detailData)
        const storeList = Array.isArray(detailData.storeList)
          ? detailData.storeList
          : (Array.isArray(merchant.storeList) ? merchant.storeList : [])

        this.setData({
          merchant,
          storeList,
          loading: false,
          loadFailed: false
        })
      })
      .catch(() => {
        this.setData({ loading: false, loadFailed: true })
        util.showToast('加载失败，请重试')
      })
  },

  makePhoneCall() {
    if (!this.data.merchant.phone) {
      util.showToast('暂无联系电话')
      return
    }
    wx.makePhoneCall({
      phoneNumber: this.data.merchant.phone
    })
  },

  viewLocation() {
    const merchant = this.data.merchant
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

  goHome() {
    util.switchTab('/pages/home/home')
  },

  goOrder() {
    util.navigateTo('/pages/order/order')
  },

  onShareAppMessage() {
    return {
      title: this.data.merchant.name || '门店详情',
      path: `/pages/merchant-detail/merchant-detail?id=${this.data.merchantId}`
    }
  }
})
