const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')

Page({
  data: {
    merchantId: null,
    merchantConfig: {},
    merchant: {},
    grouponList: [],
    albumList: [],
    reviewList: [],
    loading: true,
    isCollected: false
  },

  onLoad(options) {
    this.setData({
      merchantId: parseInt(options.id || 1, 10),
      merchantConfig: templateService.getTemplateSection('merchantDetail')
    })
    this.loadMerchantDetail()
  },

  loadMerchantDetail() {
    this.setData({ loading: true })

    setTimeout(() => {
      const merchant = mock.merchantList.find((item) => item.id === this.data.merchantId) || mock.merchantList[0]
      const grouponList = mock.grouponList.filter((item) => item.merchantId === merchant.id)
      const reviewList = mock.reviewList.filter((item) => item.merchantId === merchant.id).slice(0, 2)
      const albumList = [merchant.coverImage, merchant.avatar, ...grouponList.map((item) => item.image)].slice(0, 5)

      this.setData({
        merchant,
        grouponList,
        reviewList,
        albumList,
        loading: false
      })
    }, 180)
  },

  onGrouponTap(e) {
    const product = e.detail.product
    util.navigateTo(`/pages/product-detail/product-detail?id=${product.id}`)
  },

  onGrouponBuy(e) {
    const product = e.detail.product
    util.navigateTo(`/pages/checkout/checkout?id=${product.id}`)
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
    wx.openLocation({
      latitude: merchant.latitude,
      longitude: merchant.longitude,
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

  toggleCollect() {
    this.setData({
      isCollected: !this.data.isCollected
    })
    util.showToast(this.data.isCollected ? '已收藏门店' : '已取消收藏', 'success')
  },

  goHome() {
    util.switchTab('/pages/home/home')
  },

  goOrder() {
    util.switchTab('/pages/order/order')
  },

  onShareAppMessage() {
    return {
      title: this.data.merchant.name || '门店详情',
      path: `/pages/merchant-detail/merchant-detail?id=${this.data.merchantId}`
    }
  }
})
