const util = require('../../../utils/merchant-util')
const app = getApp()

const APPLY_STATUS_KEY = 'merchant_apply_status'

Page({
  data: {
    applyStatus: 'none',
    form: {
      storeName: '',
      contactName: '',
      contactPhone: '',
      address: '',
      licenseImage: '',
      idCardFrontImage: '',
      idCardBackImage: '',
      storeFrontImage: ''
    },
    submitting: false,
    reviewResult: null
  },

  onLoad() {
    this.checkApplyStatus()
  },

  checkApplyStatus() {
    const status = wx.getStorageSync(APPLY_STATUS_KEY)
    if (status) {
      this.setData({
        applyStatus: status.status || 'pending',
        reviewResult: status.reviewResult || null
      })
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
  },

  onChooseImage(e) {
    const field = e.currentTarget.dataset.field
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ [`form.${field}`]: res.tempFilePaths[0] })
      }
    })
  },

  onRemoveImage(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: '' })
  },

  onSubmit() {
    const { form } = this.data
    if (!form.storeName.trim()) {
      util.showToast('请填写店铺名称')
      return
    }
    if (!form.contactName.trim()) {
      util.showToast('请填写联系人姓名')
      return
    }
    if (!form.contactPhone.trim()) {
      util.showToast('请填写联系电话')
      return
    }
    if (!form.licenseImage) {
      util.showToast('请上传营业执照')
      return
    }
    if (!form.idCardFrontImage) {
      util.showToast('请上传身份证正面')
      return
    }
    if (!form.storeFrontImage) {
      util.showToast('请上传门头照片')
      return
    }

    this.setData({ submitting: true })
    util.showLoading('提交中...')

    setTimeout(() => {
      const applyData = {
        ...form,
        submitTime: Date.now(),
        status: 'pending'
      }
      wx.setStorageSync(APPLY_STATUS_KEY, applyData)
      this.setData({
        applyStatus: 'pending',
        submitting: false
      })
      util.hideLoading()
      util.showToast('提交成功，等待审核', 'success')
    }, 800)
  },

  onResubmit() {
    wx.removeStorageSync(APPLY_STATUS_KEY)
    this.setData({
      applyStatus: 'none',
      reviewResult: null
    })
  }
})
