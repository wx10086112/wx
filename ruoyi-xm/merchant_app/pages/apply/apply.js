const util = require('../../utils/util')

const app = getApp()

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
    submitting: false
  },

  onLoad() {},

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

    util.showToast('入驻申请接口尚未实现，请联系管理员')
  },

  onResubmit() {
    this.setData({
      applyStatus: 'none'
    })
  }
})
