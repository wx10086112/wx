const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    goodsId: null,
    form: {
      title: '',
      subtitle: '',
      categoryName: '',
      imageUrl: '',
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
    if (!app.needLogin() || !app.needPermission(['goods.manage'])) return
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
      .catch(() => {
        this.renderGoodsForm(util.getGoodsList())
      })
  },

  renderGoodsForm(goodsList = []) {
    const targetGoods = goodsList.find((item) => item.goodsId === this.data.goodsId)
    if (!targetGoods) return
    this.setData({
      form: {
        ...targetGoods,
        price: util.formatPrice(targetGoods.price),
        originalPrice: util.formatPrice(targetGoods.originalPrice),
        stock: String(targetGoods.stock)
      }
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
      categoryName: form.categoryName,
      imageUrl: form.imageUrl,
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
      .catch(() => {
        const localGoods = {
          ...nextItem,
          goodsId: this.data.goodsId || Date.now()
        }
        this.syncLocalGoods(localGoods)
        util.showToast('后端未联通，已保存本地演示数据')
        this.backToList()
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
    const chooseImage = wx.chooseMedia
      ? new Promise((resolve, reject) => {
          wx.chooseMedia({
            count: 1,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => resolve(res.tempFiles[0].tempFilePath),
            fail: reject
          })
        })
      : new Promise((resolve, reject) => {
          wx.chooseImage({
            count: 1,
            sourceType: ['album', 'camera'],
            success: (res) => resolve(res.tempFilePaths[0]),
            fail: reject
          })
        })

    chooseImage
      .then((filePath) => {
        api
          .uploadMerchantGoodsImage(filePath)
          .then((response = {}) => {
            this.setData({
              'form.imageUrl': response.url || filePath
            })
            util.showToast('图片已上传', 'success')
          })
          .catch(() => {
            this.setData({
              'form.imageUrl': filePath
            })
            util.showToast('后端未联通，已使用本地图片')
          })
      })
      .catch(() => {
        util.showToast('未选择图片')
      })
  }
})
