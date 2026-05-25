const util = require('../../utils/util')
const orderApi = require('../../api/order')

Page({
  data: {
    orderNo: '',
    order: {},
    rating: 5,
    ratingText: '非常满意',
    content: '',
    imageList: [],
    submitting: false,
    submitted: false,
    ratingTexts: ['非常差', '较差', '一般', '满意', '非常满意']
  },

  onLoad(options) {
    const orderNo = options.orderNo || ''
    this.setData({ orderNo })
    if (orderNo) {
      this.loadOrder(orderNo)
    }
  },

  loadOrder(orderNo) {
    orderApi.getOrderDetail(orderNo)
      .then((order) => {
        this.setData({
          order: {
            ...order,
            payAmountText: ((order.payAmount || order.price || 0) / 100).toFixed(2)
          }
        })
      })
      .catch(() => {
        util.showToast('加载订单信息失败')
      })
  },

  onStarTap(e) {
    const rating = Number(e.currentTarget.dataset.rating)
    this.setData({
      rating,
      ratingText: this.data.ratingTexts[rating - 1] || ''
    })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onChooseImage() {
    const remaining = 3 - this.data.imageList.length
    if (remaining <= 0) {
      util.showToast('最多上传 3 张图片')
      return
    }

    wx.chooseImage({
      count: remaining,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          imageList: this.data.imageList.concat(res.tempFilePaths)
        })
      }
    })
  },

  onRemoveImage(e) {
    const idx = e.currentTarget.dataset.index
    const imageList = this.data.imageList.filter((_, i) => i !== idx)
    this.setData({ imageList })
  },

  onSubmit() {
    const { content, submitting } = this.data
    if (submitting) return

    if (!content.trim()) {
      util.showToast('请输入评价内容')
      return
    }

    this.setData({ submitting: true })
    util.showLoading('提交中...')

    setTimeout(() => {
      this.setData({ submitting: false, submitted: true })
      util.hideLoading()
      util.showToast('评价成功', 'success')
    }, 600)
  },

  onDone() {
    util.navigateBack()
  }
})
