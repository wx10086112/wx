const mock = require('../../data/mock')
const util = require('../../utils/util')

Page({
  data: {
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
<<<<<<< HEAD
    const order = mock.orderList.find((item) => item.orderNo === orderNo) || mock.orderList[0] || {}
    this.setData({
      order: {
        ...order,
        payAmountText: ((order.payAmount || order.price || 0) / 100).toFixed(2)
      }
    })
=======
    if (orderNo) {
      orderApi.getOrderDetail(orderNo)
        .then((res) => {
          const order = res.data || res || {}
          this.setData({
            order: {
              ...order,
              payAmountText: ((order.payAmount || order.price || 0) / 100).toFixed(2)
            }
          })
        })
        .catch(() => {
          this.setData({ order: {} })
        })
    }
>>>>>>> 苏
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
    const { rating, content, order, submitting } = this.data
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
      util.showToast('评价功能暂未开放', 'none')
    }, 500)
  },

  onDone() {
    util.navigateBack()
  }
})
