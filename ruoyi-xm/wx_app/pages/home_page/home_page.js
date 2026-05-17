const util = require('../../utils/util')
const api = require('../../api')

Page({
  data: {
    loading: false,
    list: [],
    page: 1,
    pageSize: 10,
    hasMore: true
  },

  onLoad(options) {
    console.log('页面参数:', options)
    this.loadData()
  },

  onShow() {
    
  },

  onPullDownRefresh() {
    this.setData({
      page: 1,
      hasMore: true
    })
    this.loadData().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.loadMore()
    }
  },

  loadData() {
    this.setData({ loading: true })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        this.setData({
          list: [
            { id: 1, title: '示例数据 1', desc: '这是描述信息', time: '2026-05-09' },
            { id: 2, title: '示例数据 2', desc: '这是描述信息', time: '2026-05-09' },
            { id: 3, title: '示例数据 3', desc: '这是描述信息', time: '2026-05-09' }
          ],
          loading: false
        })
        resolve()
      }, 500)
    })
  },

  loadMore() {
    this.setData({ 
      loading: true,
      page: this.data.page + 1
    })
    
    setTimeout(() => {
      this.setData({
        loading: false,
        hasMore: false
      })
    }, 500)
  },

  onItemTap(e) {
    const item = e.currentTarget.dataset.item
    console.log('点击项:', item)
    util.showToast('点击了: ' + item.title)
  },

  onButtonTap() {
    util.showToast('按钮点击')
  },

  goOtherPage() {
    util.navigateTo('/pages/home/home')
  },

  onShareAppMessage() {
    return {
      title: '页面模板',
      path: '/pages/home_page/home_page'
    }
  }
})
