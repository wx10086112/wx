const util = require('../../utils/util')
const merchantApi = require('../../api/merchant')

const normalizeMerchantDetail = (merchant = {}) => {
  const tags = Array.isArray(merchant.tags) ? merchant.tags : []
  const serviceAbilityTags = Array.isArray(merchant.serviceAbilityTags) ? merchant.serviceAbilityTags : []
  const facilityTags = Array.isArray(merchant.facilityTags) ? merchant.facilityTags : []
  return {
    ...merchant,
    serviceAbilityTags,
    facilityTags,
    displayTags: tags.filter((tag) => !['营业中', '休息中'].includes(tag)),
    businessStatusText: merchant.businessStatus ? '营业中' : '休息中',
    bookingText: merchant.supportBooking === false ? '到店即用' : '可预约'
  }
}

Page({
  data: {
    merchantId: null,
    merchant: {},
    albumList: [],
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
        const merchant = normalizeMerchantDetail(res.data || res || {})
        const albumList = (merchant.albumList && merchant.albumList.length
          ? merchant.albumList
          : [merchant.coverImage, merchant.avatar]
        ).filter(Boolean).slice(0, 6)
        const albumMerchantId = merchant.merchantId || merchant.id

        if (albumList.length <= 1) {
          merchantApi.getMerchantAlbum(albumMerchantId)
            .then((albumRes) => {
              const apiAlbum = (albumRes.data || albumRes || {}).albumList || []
              if (apiAlbum.length > 0) {
                this.setData({ albumList: apiAlbum.slice(0, 6) })
              }
            })
            .catch(() => {})
        }

        this.setData({
          merchant,
          albumList,
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

  previewAlbum(e) {
    const index = e.currentTarget.dataset.index
    wx.previewImage({
      current: this.data.albumList[index],
      urls: this.data.albumList
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
