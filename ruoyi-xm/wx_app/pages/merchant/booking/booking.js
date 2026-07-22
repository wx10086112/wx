const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')
const { toListThumbnailUrl } = require('../../../utils/image-url')

const app = getApp()

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已过期', value: 'EXPIRED' }
]

const normalizeTab = (tab) => (tabs.some((item) => item.value === tab) ? tab : 'ALL')

Page({
  data: {
    tabs,
    currentTab: 'ALL',
    bookingList: [],
    loading: false,
    merchantNavList: util.getMerchantNavList('workbench')
  },

  onLoad(options = {}) {
    this.setData({
      currentTab: normalizeTab(options.status || options.tab)
    })
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    this.loadData()
  },

  loadData() {
    const status = this.data.currentTab === 'ALL' ? '' : this.data.currentTab
    this.setData({ loading: true })
    api.getMerchantBookingList({ status })
      .then((list = []) => {
        this.renderBookingList(list)
      })
      .catch((err = {}) => {
        this.setData({ bookingList: [] })
        util.showToast(err.message || '预点单列表加载失败')
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  renderBookingList(list = []) {
    const bookingList = list
      .slice()
      .sort((a, b) => Number(b.bookingTime || 0) - Number(a.bookingTime || 0))
      .map((item) => {
        const statusMeta = util.getBookingStatusMeta(item.status)
        return {
          ...item,
          image: toListThumbnailUrl(item.image),
          statusMeta,
          priceText: util.formatPrice(item.price),
          bookingTimeText: util.formatDate(item.bookingTime),
          createTimeText: util.formatDate(item.createTime),
          canConfirm: item.status === 'PENDING',
          canComplete: item.status === 'CONFIRMED',
          canCancel: item.status === 'PENDING' || item.status === 'CONFIRMED'
        }
      })
    this.setData({ bookingList })
  },

  switchTab(e) {
    this.setData({ currentTab: normalizeTab(e.currentTarget.dataset.tab) }, () => {
      this.loadData()
    })
  },

  confirmBooking(e) {
    this.updateBooking(e.currentTarget.dataset.no, api.confirmMerchantBooking, '预点单已确认')
  },

  completeBooking(e) {
    this.updateBooking(e.currentTarget.dataset.no, api.completeMerchantBooking, '预点单已完成')
  },

  cancelBooking(e) {
    const bookingNo = e.currentTarget.dataset.no
    wx.showModal({
      title: '取消预点单',
      content: '确认取消这条预点单吗？',
      confirmText: '确认取消',
      success: (res) => {
        if (!res.confirm) return
        this.updateBooking(bookingNo, api.cancelMerchantBooking, '预点单已取消')
      }
    })
  },

  updateBooking(bookingNo, action, successText) {
    if (!bookingNo || typeof action !== 'function') return
    action(bookingNo)
      .then(() => {
        util.showToast(successText, 'success')
        this.loadData()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '预点单操作失败，请重试')
      })
  },

  callCustomer(e) {
    const phone = e.currentTarget.dataset.phone
    if (!phone) {
      util.showToast('暂无联系电话')
      return
    }
    wx.makePhoneCall({ phoneNumber: phone })
  },

  goMerchantTab(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
