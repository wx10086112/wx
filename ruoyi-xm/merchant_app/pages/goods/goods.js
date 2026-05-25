const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    canManageGoods: false,
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '上架中', value: 'ON_SHELF' },
      { label: '已下架', value: 'OFF_SHELF' }
    ],
    currentTab: 'ALL',
    goodsList: [],
    batchMode: false,
    selectedIds: []
  },

  onShow() {
    if (!app.needLogin()) return
    this.setData({
      canManageGoods: app.hasAnyPermission(['goods.manage'])
    })
    this.loadData()
  },

  loadData() {
    api
      .getMerchantGoodsList()
      .then((goodsList = []) => {
        this.renderGoods(goodsList)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
      })
  },

  renderGoods(sourceList = []) {
    const goodsList = sourceList
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
      .map((item) => ({
        ...item,
        priceText: util.formatPrice(item.price),
        originalPriceText: util.formatPrice(item.originalPrice),
        selected: this.data.selectedIds.includes(item.goodsId),
        lowStock: item.status === 'ON_SHELF' && Number(item.stock || 0) <= 20
      }))

    this.setData({
      goodsList: this.filterGoods(goodsList, this.data.currentTab)
    })
  },

  filterGoods(goodsList = [], tab = 'ALL') {
    if (tab === 'ALL') return goodsList
    return goodsList.filter((item) => item.status === tab)
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ currentTab: tab }, () => {
      this.loadData()
    })
  },

  toggleGoodsStatus(e) {
    if (!app.needPermission(['goods.manage'])) return
    const goodsId = Number(e.currentTarget.dataset.id)
    const targetGoods = this.data.goodsList.find((item) => item.goodsId === goodsId) || {}
    const nextStatus = targetGoods.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'

    api
      .updateMerchantGoodsStatus({
        goodsId,
        status: nextStatus
      })
      .then(() => {
        util.showToast('状态已更新', 'success')
        this.loadData()
      })
      .catch(() => {
        util.showToast('操作失败，请重试')
      })
  },

  goEditGoods(e) {
    if (!app.needPermission(['goods.manage'])) return
    const goodsId = e.currentTarget.dataset.id
    util.navigateTo(`/pages/goods-edit/goods-edit?goodsId=${goodsId}`)
  },

  goAddGoods() {
    if (!app.needPermission(['goods.manage'])) return
    util.navigateTo('/pages/goods-edit/goods-edit')
  },

  /* --- 批量操作 --- */
  toggleBatchMode() {
    this.setData({
      batchMode: !this.data.batchMode,
      selectedIds: []
    }, () => this.loadData())
  },

  toggleSelectGoods(e) {
    const goodsId = Number(e.currentTarget.dataset.id)
    let selectedIds = [...this.data.selectedIds]
    const index = selectedIds.indexOf(goodsId)
    if (index > -1) {
      selectedIds.splice(index, 1)
    } else {
      selectedIds.push(goodsId)
    }
    this.setData({ selectedIds }, () => this.loadData())
  },

  selectAll() {
    const allIds = this.data.goodsList.map((item) => item.goodsId)
    const isAllSelected = this.data.selectedIds.length === allIds.length
    this.setData({
      selectedIds: isAllSelected ? [] : allIds
    }, () => this.loadData())
  },

  batchOnShelf() {
    if (!this.data.selectedIds.length) {
      util.showToast('请先选择商品')
      return
    }
    util.showModal('批量上架', `确定上架选中的 ${this.data.selectedIds.length} 个商品吗？`).then((confirm) => {
      if (!confirm) return
      api
        .batchUpdateGoodsStatus(this.data.selectedIds, 'ON_SHELF')
        .then((res) => {
          const count = (res && res.count) || this.data.selectedIds.length
          util.showToast(`已上架 ${count} 个商品`, 'success')
          this.setData({ selectedIds: [], batchMode: false })
          this.loadData()
        })
        .catch(() => {
          util.showToast('批量上架失败')
        })
    })
  },

  batchOffShelf() {
    if (!this.data.selectedIds.length) {
      util.showToast('请先选择商品')
      return
    }
    util.showModal('批量下架', `确定下架选中的 ${this.data.selectedIds.length} 个商品吗？`).then((confirm) => {
      if (!confirm) return
      api
        .batchUpdateGoodsStatus(this.data.selectedIds, 'OFF_SHELF')
        .then((res) => {
          const count = (res && res.count) || this.data.selectedIds.length
          util.showToast(`已下架 ${count} 个商品`, 'success')
          this.setData({ selectedIds: [], batchMode: false })
          this.loadData()
        })
        .catch(() => {
          util.showToast('批量下架失败')
        })
    })
  }
})
