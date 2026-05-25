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
    const id = parseInt(options.id, 10)
    if (!id || isNaN(id)) {
      util.showToast('商家不存在')
      setTimeout(() => util.navigateBack(), 1500)
      return
    }
    this.setData({
      merchantId: id,
      merchantConfig: templateService.getTemplateSection('merchantDetail')
    })
    this.loadMerchantDetail()
  },

  loadMerchantDetail() {
    this.setData({ loading: true })

    Promise.all([
      api.getMerchantDetail(this.data.merchantId),
      api.getGrouponList({ merchantId: this.data.merchantId })
    ]).then(([merchantData, grouponResult]) => {
      const merchant = merchantData || {}
      const grouponList = grouponResult || []
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
      this.setData({
        merchant: {},
        albumList: [],
        loading: false
      })
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
