const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const api = require('../../api/index')

Page({
  data: {
    merchantId: null,
    merchantConfig: {},
    merchant: {},
    albumList: [],
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

    Promise.all([
      api.getMerchantDetail(this.data.merchantId).catch(() => null),
      api.getGrouponList({ merchantId: this.data.merchantId }).catch(() => null)
    ]).then(([merchantData, grouponResult]) => {
      const merchant = merchantData || mock.merchantList.find((item) => item.id === this.data.merchantId) || mock.merchantList[0]
      const grouponList = grouponResult || mock.grouponList.filter((item) => item.merchantId === merchant.merchantId)
      const albumList = (merchant.albumList && merchant.albumList.length
        ? merchant.albumList
        : [merchant.coverImage, merchant.avatar, ...(grouponList.map((item) => item.image || item.imageUrl).filter(Boolean))]
      ).slice(0, 6)

      this.setData({
        merchant,
        albumList,
        loading: false
      })
    }).catch(() => {
      const merchant = mock.merchantList.find((item) => item.id === this.data.merchantId) || mock.merchantList[0]
      const grouponList = mock.grouponList.filter((item) => item.merchantId === merchant.merchantId)
      const albumList = (merchant.albumList && merchant.albumList.length
        ? merchant.albumList
        : [merchant.coverImage, merchant.avatar, ...grouponList.map((item) => item.image)]
      ).slice(0, 6)

      this.setData({
        merchant,
        albumList,
        loading: false
      })
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
    util.navigateTo('/pages/order/order')
  },

  onShareAppMessage() {
    return {
      title: this.data.merchant.name || '门店详情',
      path: `/pages/merchant-detail/merchant-detail?id=${this.data.merchantId}`
    }
  }
})
