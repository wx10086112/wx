const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
<<<<<<< HEAD
=======
const merchantApi = require('../../api/merchant')
>>>>>>> 苏

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
<<<<<<< HEAD
      merchantId: parseInt(options.id || 1, 10),
=======
      merchantId: parseInt(options.id, 10),
>>>>>>> 苏
      merchantConfig: templateService.getTemplateSection('merchantDetail')
    })
    this.loadMerchantDetail()
  },

  loadMerchantDetail() {
    this.setData({ loading: true })

<<<<<<< HEAD
    setTimeout(() => {
      const merchant = mock.merchantList.find((item) => item.id === this.data.merchantId) || mock.merchantList[0]
      const grouponList = mock.grouponList.filter((item) => item.merchantId === merchant.id)
      const albumList = (merchant.albumList && merchant.albumList.length
        ? merchant.albumList
        : [merchant.coverImage, merchant.avatar, ...grouponList.map((item) => item.image)]
      ).slice(0, 6)
=======
    merchantApi.getMerchantDetail(this.data.merchantId)
      .then((res) => {
        const merchant = res.data || res || {}
        const albumList = (merchant.albumList && merchant.albumList.length
          ? merchant.albumList
          : [merchant.coverImage, merchant.avatar]
        ).filter(Boolean).slice(0, 6)
>>>>>>> 苏

        // 若商家相册为空，尝试从接口补充
        if (albumList.length <= 1) {
          merchantApi.getMerchantAlbum(this.data.merchantId)
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
          loading: false
        })
      })
<<<<<<< HEAD
    }, 180)
=======
      .catch(() => {
        this.setData({ loading: false })
        util.showToast('加载失败，请重试')
      })
>>>>>>> 苏
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
