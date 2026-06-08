const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()
const DEFAULT_IMAGE_CROP = {
  x: 0,
  y: 0,
  scale: 1
}

const clamp = (value, min, max) => Math.min(Math.max(Number(value) || 0, min), max)
const getCropLimit = (scale = 1) => {
  const renderedPercent = 130 * clamp(scale || 1, 1, 2.2)
  return ((renderedPercent - 100) / (renderedPercent * 2)) * 100
}
const normalizeCrop = (crop = {}) => ({
  x: 0,
  y: 0,
  scale: clamp(crop.scale || 1, 1, 2.2)
})
const normalizeCropWithOffset = (crop = {}) => {
  const normalized = normalizeCrop(crop)
  const limit = getCropLimit(normalized.scale)
  return {
    ...normalized,
    x: clamp(crop.x, -limit, limit),
    y: clamp(crop.y, -limit, limit)
  }
}
const buildCropStyle = (crop = DEFAULT_IMAGE_CROP) => {
  const normalized = normalizeCropWithOffset(crop)
  return `transform: translate(${normalized.x}%, ${normalized.y}%) scale(${normalized.scale});`
}

const chooseSingleImage = () => {
  if (wx.chooseMedia) {
    return new Promise((resolve, reject) => {
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const filePath = res.tempFiles && res.tempFiles[0] && res.tempFiles[0].tempFilePath
          filePath ? resolve(filePath) : reject(new Error('未选择图片'))
        },
        fail: reject
      })
    })
  }

  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count: 1,
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFilePaths && res.tempFilePaths[0]
        filePath ? resolve(filePath) : reject(new Error('未选择图片'))
      },
      fail: reject
    })
  })
}

const isChooseImageCancel = (err = {}) => {
  const message = String(err.errMsg || err.message || '')
  return message.includes('cancel')
}

Page({
  data: {
    goodsId: null,
    uploadingImage: false,
    cropScaleValue: 100,
    imageCropStyle: buildCropStyle(DEFAULT_IMAGE_CROP),
    cropTouch: null,
    form: {
      title: '',
      subtitle: '',
      imageUrl: '',
      imageCrop: DEFAULT_IMAGE_CROP,
      price: '',
      originalPrice: '',
      stock: '',
      validPeriod: '',
      verifyNotice: '',
      status: 'ON_SHELF'
    }
  },

  onLoad(options) {
    this.setData({
      goodsId: options.goodsId ? Number(options.goodsId) : null
    })
  },

  onShow() {
    if (!app.needMerchantLogin() || !app.needPermission(['goods.manage'])) return
    this.loadData()
  },

  loadData() {
    if (!this.data.goodsId) return
    api
      .getMerchantGoodsList()
      .then((goodsList = []) => {
        util.setGoodsList(goodsList)
        this.renderGoodsForm(goodsList)
      })
      .catch((err = {}) => {
        util.showToast(err.message || '商品信息加载失败')
      })
  },

  renderGoodsForm(goodsList = []) {
    const targetGoods = goodsList.find((item) => item.goodsId === this.data.goodsId)
    if (!targetGoods) return
    const imageCrop = normalizeCropWithOffset(targetGoods.imageCrop || DEFAULT_IMAGE_CROP)
    this.setData({
      form: {
        ...targetGoods,
        imageCrop,
        price: util.formatPrice(targetGoods.price),
        originalPrice: util.formatPrice(targetGoods.originalPrice),
        stock: String(targetGoods.stock)
      },
      cropScaleValue: Math.round(imageCrop.scale * 100),
      imageCropStyle: buildCropStyle(imageCrop)
    })
  },

  handleInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`form.${key}`]: e.detail.value
    })
  },

  handleStatusChange(e) {
    this.setData({
      'form.status': e.detail.value ? 'ON_SHELF' : 'OFF_SHELF'
    })
  },

  setImageCrop(nextCrop) {
    const imageCrop = normalizeCropWithOffset(nextCrop)
    this.setData({
      'form.imageCrop': imageCrop,
      cropScaleValue: Math.round(imageCrop.scale * 100),
      imageCropStyle: buildCropStyle(imageCrop)
    })
  },

  handleCropTouchStart(e) {
    const touch = e.touches && e.touches[0]
    if (!touch) return
    this.setData({
      cropTouch: {
        startX: touch.clientX,
        startY: touch.clientY,
        originX: this.data.form.imageCrop.x || 0,
        originY: this.data.form.imageCrop.y || 0
      }
    })
  },

  handleCropTouchMove(e) {
    const touch = e.touches && e.touches[0]
    const cropTouch = this.data.cropTouch
    if (!touch || !cropTouch) return

    const deltaX = touch.clientX - cropTouch.startX
    const deltaY = touch.clientY - cropTouch.startY
    this.setImageCrop({
      ...this.data.form.imageCrop,
      x: cropTouch.originX + deltaX / 4,
      y: cropTouch.originY + deltaY / 4
    })
  },

  handleCropTouchEnd() {
    this.setData({ cropTouch: null })
  },

  handleCropScaleChanging(e) {
    this.setImageCrop({
      ...this.data.form.imageCrop,
      scale: Number(e.detail.value || 100) / 100
    })
  },

  adjustCropScale(e) {
    const delta = Number(e.currentTarget.dataset.delta || 0)
    this.setImageCrop({
      ...this.data.form.imageCrop,
      scale: (this.data.form.imageCrop.scale || 1) + delta
    })
  },

  resetImageCrop() {
    this.setImageCrop(DEFAULT_IMAGE_CROP)
  },

  saveGoods() {
    const form = this.data.form
    if (!form.title || !form.price) {
      util.showToast('请完善套餐名称和价格')
      return
    }

    const nextItem = {
      goodsId: this.data.goodsId || null,
      title: form.title,
      subtitle: form.subtitle,
      imageUrl: form.imageUrl,
      imageCrop: normalizeCropWithOffset(form.imageCrop),
      price: Math.round(Number(form.price || 0) * 100),
      originalPrice: Math.round(Number(form.originalPrice || 0) * 100),
      stock: Number(form.stock || 0),
      validPeriod: form.validPeriod,
      verifyNotice: form.verifyNotice,
      status: form.status,
      sales: form.sales || 0,
      sort: form.sort
    }

    api
      .saveMerchantGoods(nextItem)
      .then((savedGoods) => {
        this.syncLocalGoods(savedGoods || nextItem)
        util.showToast('保存成功', 'success')
        this.backToList()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '保存失败，请重试')
      })
  },

  syncLocalGoods(goods) {
    const currentGoodsList = util.getGoodsList()
    const exists = currentGoodsList.some((item) => item.goodsId === goods.goodsId)
    const nextGoodsList = exists
      ? currentGoodsList.map((item) => (item.goodsId === goods.goodsId ? { ...item, ...goods } : item))
      : [
          {
            ...goods,
            sort: goods.sort || currentGoodsList.length + 1
          },
          ...currentGoodsList
        ]

    util.setGoodsList(nextGoodsList)
  },

  backToList() {
    setTimeout(() => {
      wx.navigateBack({ delta: 1 })
    }, 300)
  },

  chooseGoodsImage() {
    if (!app.needPermission(['goods.manage'])) return
    if (this.data.uploadingImage) return

    chooseSingleImage()
      .then((filePath) => {
        this.setData({ uploadingImage: true })
        util.showLoading('上传中...')
        return api.uploadMerchantGoodsImage(filePath).then((response = {}) => ({ filePath, response }))
      })
      .then(({ filePath, response }) => {
        const imageCrop = DEFAULT_IMAGE_CROP
        this.setData({
          'form.imageUrl': response.url || filePath,
          'form.imageCrop': imageCrop,
          cropScaleValue: Math.round(imageCrop.scale * 100),
          imageCropStyle: buildCropStyle(imageCrop)
        })
        util.showToast('图片已上传', 'success')
      })
      .catch((err) => {
        if (!isChooseImageCancel(err)) {
          util.showToast(err.message || '图片上传失败')
        }
      })
      .finally(() => {
        if (this.data.uploadingImage) {
          this.setData({ uploadingImage: false })
          util.hideLoading()
        }
      })
  }
})
