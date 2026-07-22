const app = getApp()
const util = require('../../utils/util')
const bookingApi = require('../../api/booking')
const { toListThumbnailUrl } = require('../../utils/image-url')

const DEFAULT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

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
    image: toListThumbnailUrl(item.image || DEFAULT_IMAGE),
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
    loading: true
  },

  onLoad() {
    this.loadBookings()
  },

  onShow() {
    if (!app.globalData.isLoggedIn) {
      this.goLogin()
      return
    }
    this.loadBookings()
  },

  onPullDownRefresh() {
    this.loadBookings().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadBookings() {
    if (!app.globalData.isLoggedIn) {
      this.setData({
        allBookings: [],
        bookingList: [],
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
          loading: false
        }, () => this.applyCurrentFilter())
      })
      .catch(() => {
        this.setData({
          allBookings: [],
          bookingList: [],
          loading: false
        })
      })
  },

  applyCurrentFilter() {
    const list = this.data.currentTab === 'ALL'
      ? this.data.allBookings
      : this.data.allBookings.filter((item) => item.status === this.data.currentTab)
    this.setData({ bookingList: list })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (!tab || tab === this.data.currentTab) return
    this.setData({ currentTab: tab }, () => this.applyCurrentFilter())
  },

  cancelBooking(e) {
    const bookingNo = e.currentTarget.dataset.no
    util.showModal('取消预点单', '确认取消这条预点单记录吗？').then((confirm) => {
      if (!confirm) return
      bookingApi.cancelBooking(bookingNo)
        .then(() => {
          util.showToast('预点单已取消', 'success')
          this.loadBookings()
        })
        .catch((err) => {
          util.showToast(err && err.msg ? err.msg : '取消失败')
        })
    })
  },

  goBooking() {
    wx.switchTab({ url: '/pages/booking/booking' })
  },

  goLogin() {
    util.navigateTo('/pages/login/login')
  }
})
