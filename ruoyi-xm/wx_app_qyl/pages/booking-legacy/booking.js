const app = getApp()
const util = require('../../utils/util')
const productApi = require('../../api/product')
const bookingApi = require('../../api/booking')
const { toListThumbnailUrl } = require('../../utils/image-url')

const DEFAULT_SERVICE_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

const pad = (value) => String(value).padStart(2, '0')

const formatPickerDate = (date) => {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const cleanServiceText = (value = '', fallback = '') => {
  const text = String(value || fallback || '')
    .replace(/\u56e2\u8d2d/g, '')
    .replace(/\u5957\u9910/g, '服务')
    .replace(/\u6838\u9500/g, '到店确认')
    .trim()
  return text || fallback
}

const extractList = (res = {}) => {
  const payload = res.data || res.rows || res.list || res.records || res
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.rows)) return payload.rows
  if (payload && Array.isArray(payload.list)) return payload.list
  if (payload && Array.isArray(payload.records)) return payload.records
  return []
}

const normalizeService = (item = {}) => ({
  ...item,
  serviceId: item.id || item.goodsId,
  title: cleanServiceText(item.title || item.name || item.productName, '门店预点单服务'),
  subtitle: cleanServiceText(item.subtitle || item.description, '选择到店时间，提交后等待门店确认'),
  image: toListThumbnailUrl(item.image || item.coverImage || item.productImage || DEFAULT_SERVICE_IMAGE),
  priceText: item.price ? util.formatPrice(item.price) : '',
  unavailable: Number(item.stock || 0) <= 0
})

Page({
  data: {
    serviceList: [],
    loading: true,
    showBookingSheet: false,
    selectedService: null,
    minBookingDate: formatPickerDate(new Date()),
    form: {
      bookingDate: formatPickerDate(new Date(Date.now() + 24 * 60 * 60 * 1000)),
      bookingClock: '12:00',
      contactName: '',
      contactPhone: '',
      peopleCount: 1,
      remark: ''
    },
    submitting: false
  },

  onLoad() {
    this.loadServices()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
  },

  onPullDownRefresh() {
    this.loadServices().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadServices() {
    this.setData({ loading: true })
    return productApi.getProductList()
      .then((res) => {
        this.setData({
          serviceList: extractList(res).map(normalizeService),
          loading: false
        })
      })
      .catch(() => {
        this.setData({
          serviceList: [],
          loading: false
        })
      })
  },

  openBookingSheet(e) {
    const service = this.data.serviceList[e.currentTarget.dataset.index]
    if (!app.needLogin()) return
    if (!service || service.unavailable) {
      util.showToast('当前服务暂不可预点单')
      return
    }
    const userInfo = app.globalData.userInfo || {}
    this.setData({
      selectedService: service,
      showBookingSheet: true,
      form: {
        ...this.data.form,
        contactName: userInfo.nickName || userInfo.nickname || this.data.form.contactName,
        contactPhone: userInfo.phone || userInfo.phoneNumber || this.data.form.contactPhone
      }
    })
  },

  closeBookingSheet() {
    if (this.data.submitting) return
    this.setData({
      showBookingSheet: false,
      selectedService: null
    })
  },

  noop() {},

  onDateChange(e) {
    this.setData({ 'form.bookingDate': e.detail.value })
  },

  onTimeChange(e) {
    this.setData({ 'form.bookingClock': e.detail.value })
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  submitBooking() {
    if (this.data.submitting) return
    const service = this.data.selectedService
    const form = this.data.form
    if (!service) return

    const bookingTime = new Date(`${form.bookingDate.replace(/-/g, '/')} ${form.bookingClock}:00`).getTime()
    if (!bookingTime || bookingTime <= Date.now()) {
      util.showToast('请选择未来的预点单时间')
      return
    }
    if (!form.contactPhone) {
      util.showToast('请填写联系电话')
      return
    }

    this.setData({ submitting: true })
    bookingApi.createBooking({
      productId: service.serviceId,
      bookingTime,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      peopleCount: Number(form.peopleCount || 1),
      remark: form.remark
    }).then(() => {
      util.showToast('预点单已提交', 'success')
      this.setData({
        submitting: false,
        showBookingSheet: false,
        selectedService: null,
        form: {
          ...form,
          remark: ''
        }
      })
    }).catch((err) => {
      this.setData({ submitting: false })
      util.showToast(err && err.msg ? err.msg : '预点单提交失败')
    })
  }
})
