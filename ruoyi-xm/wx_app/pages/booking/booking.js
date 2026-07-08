const app = getApp()
const util = require('../../utils/util')
const productApi = require('../../api/product')
const bookingApi = require('../../api/booking')
const { toListThumbnailUrl } = require('../../utils/image-url')

const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

const STATUS_TABS = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已过期', value: 'EXPIRED' }
]

const STATUS_META = {
  PENDING: { text: '待处理', className: 'pending' },
  CONFIRMED: { text: '已确认', className: 'confirmed' },
  COMPLETED: { text: '已完成', className: 'completed' },
  CANCELLED: { text: '已取消', className: 'cancelled' },
  EXPIRED: { text: '已过期', className: 'expired' }
}

const pad = (value) => String(value).padStart(2, '0')

const formatPickerDate = (date) => {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const normalizeProduct = (item = {}) => ({
  ...item,
  title: item.title || item.name || item.productName || '团购套餐',
  image: toListThumbnailUrl(item.image || item.coverImage || item.productImage || DEFAULT_PRODUCT_IMAGE),
  priceText: util.formatPrice(item.price || 0),
  originalPriceText: item.originalPrice ? util.formatPrice(item.originalPrice) : '',
  totalSales: Number(item.totalSales || item.sales || 0),
  soldOut: Number(item.stock || 0) <= 0
})

const extractList = (res = {}) => {
  const payload = res.data || res.rows || res.list || res.records || res
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.rows)) return payload.rows
  if (payload && Array.isArray(payload.list)) return payload.list
  if (payload && Array.isArray(payload.records)) return payload.records
  return []
}

const normalizeBooking = (item = {}) => {
  const meta = STATUS_META[item.status] || { text: '未知', className: 'default' }
  return {
    ...item,
    image: toListThumbnailUrl(item.image || DEFAULT_PRODUCT_IMAGE),
    priceText: util.formatPrice(item.price || 0),
    statusText: meta.text,
    statusClass: meta.className,
    bookingTimeText: item.bookingTime ? util.formatDate(item.bookingTime, 'YYYY-MM-DD HH:mm') : '',
    createTimeText: item.createTime ? util.formatDate(item.createTime, 'YYYY-MM-DD HH:mm') : '',
    canCancel: item.status === 'PENDING' || item.status === 'CONFIRMED'
  }
}

Page({
  data: {
    tabs: STATUS_TABS,
    currentTab: 'ALL',
    allBookings: [],
    bookingList: [],
    bookingStats: [],
    productList: [],
    loading: true,
    productLoading: true,
    showBookingSheet: false,
    selectedProduct: null,
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
    this._hasLoaded = false
    this.loadData().then(() => {
      this._hasLoaded = true
    })
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
    if (this._hasLoaded) {
      this.loadData()
    }
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadData() {
    this.setData({ loading: true, productLoading: true })
    return Promise.all([
      this.loadBookings(),
      this.loadProducts()
    ])
  },

  loadBookings() {
    if (!app.globalData.isLoggedIn) {
      this.setData({
        allBookings: [],
        bookingList: [],
        bookingStats: this.buildStats([]),
        loading: false
      })
      return Promise.resolve()
    }

    this.setData({ loading: true })
    return bookingApi.getBookingList()
      .then((res) => {
        const allBookings = extractList(res).map(normalizeBooking)
        this.setData({
          allBookings,
          bookingStats: this.buildStats(allBookings),
          loading: false
        }, () => this.applyCurrentFilter())
      })
      .catch(() => {
        this.setData({
          allBookings: [],
          bookingList: [],
          bookingStats: this.buildStats([]),
          loading: false
        })
      })
  },

  loadProducts() {
    this.setData({ productLoading: true })
    return productApi.getGrouponList()
      .then((res) => {
        const productList = extractList(res).map(normalizeProduct)
        this.setData({
          productList,
          productLoading: false
        })
      })
      .catch(() => {
        this.setData({
          productList: [],
          productLoading: false
        })
      })
  },

  buildStats(list = []) {
    return STATUS_TABS.filter((tab) => tab.value !== 'ALL').map((tab) => ({
      ...tab,
      count: list.filter((item) => item.status === tab.value).length
    }))
  },

  getFilteredBookings(list = [], tab = this.data.currentTab) {
    if (tab === 'ALL') return list
    return list.filter((item) => item.status === tab)
  },

  applyCurrentFilter() {
    this.setData({
      bookingList: this.getFilteredBookings(this.data.allBookings, this.data.currentTab)
    })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (!tab || tab === this.data.currentTab) return
    this.setData({ currentTab: tab }, () => this.applyCurrentFilter())
  },

  openBookingSheet(e) {
    const product = this.data.productList[e.currentTarget.dataset.index]
    if (!app.needLogin()) return
    if (!product || product.soldOut) {
      util.showToast('当前套餐暂不可预约')
      return
    }
    const userInfo = app.globalData.userInfo || {}
    this.setData({
      selectedProduct: product,
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
      selectedProduct: null
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
    const product = this.data.selectedProduct
    const form = this.data.form
    if (!product) return

    const bookingTime = new Date(`${form.bookingDate.replace(/-/g, '/')} ${form.bookingClock}:00`).getTime()
    if (!bookingTime || bookingTime <= Date.now()) {
      util.showToast('请选择未来的预约时间')
      return
    }
    if (!form.contactPhone) {
      util.showToast('请填写联系电话')
      return
    }

    this.setData({ submitting: true })
    bookingApi.createBooking({
      productId: product.id || product.goodsId,
      bookingTime,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      peopleCount: Number(form.peopleCount || 1),
      remark: form.remark
    }).then(() => {
      util.showToast('预约已提交', 'success')
      this.setData({
        submitting: false,
        showBookingSheet: false,
        selectedProduct: null,
        currentTab: 'PENDING',
        form: {
          ...form,
          remark: ''
        }
      })
      this.loadBookings()
    }).catch((err) => {
      this.setData({ submitting: false })
      util.showToast(err && err.msg ? err.msg : '预约提交失败')
    })
  },

  cancelBooking(e) {
    const bookingNo = e.currentTarget.dataset.no
    util.showModal('取消预约', '确认取消这条预约记录吗？').then((confirm) => {
      if (!confirm) return
      bookingApi.cancelBooking(bookingNo)
        .then(() => {
          util.showToast('预约已取消', 'success')
          this.loadBookings()
        })
        .catch((err) => {
          util.showToast(err && err.msg ? err.msg : '取消失败')
        })
    })
  },

  goLogin() {
    util.navigateTo('/pages/login/login')
  }
})
