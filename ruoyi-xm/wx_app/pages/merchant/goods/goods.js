const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()
const buildImageCropStyle = (crop = {}) => {
  const scale = Math.min(Math.max(Number(crop.scale || 1), 1), 2.2)
  const renderedPercent = 130 * scale
  const limit = ((renderedPercent - 100) / (renderedPercent * 2)) * 100
  const x = Math.min(Math.max(Number(crop.x || 0), -limit), limit)
  const y = Math.min(Math.max(Number(crop.y || 0), -limit), limit)
  return `transform: translate(${x}%, ${y}%) scale(${scale});`
}

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
    selectedIds: [],
    confirmModalVisible: false,
    confirmModal: {
      title: '',
      desc: '',
      action: ''
    }
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    this.setData({
      canManageGoods: app.hasAnyPermission(['goods.manage'])
    })
    this.loadData()
  },

  loadData() {
    api
      .getMerchantGoodsList()
      .then((goodsList = []) => {
        util.setGoodsList(goodsList)
        this.renderGoods(goodsList)
      })
      .catch(() => {
        this.renderGoods(util.getGoodsList())
      })
  },

  renderGoods(sourceList = []) {
    const goodsList = sourceList
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
      .map((item) => ({
        ...item,
        priceText: util.formatPrice(item.price),
        originalPriceText: util.formatPrice(item.originalPrice),
        imageCropStyle: buildImageCropStyle(item.imageCrop),
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
    const currentGoodsList = util.getGoodsList()
    const targetGoods = currentGoodsList.find((item) => item.goodsId === goodsId) || {}
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
        const nextGoodsList = currentGoodsList.map((item) =>
          item.goodsId === goodsId
            ? {
                ...item,
                status: nextStatus
              }
            : item
        )
        util.setGoodsList(nextGoodsList)
        util.showToast('后端未联通，已更新本地演示数据')
        this.loadData()
      })
  },

  goEditGoods(e) {
    if (!app.needPermission(['goods.manage'])) return
    const goodsId = e.currentTarget.dataset.id
    util.navigateTo(`/pages/merchant/goods-edit/goods-edit?goodsId=${goodsId}`)
  },

  goAddGoods() {
    if (!app.needPermission(['goods.manage'])) return
    util.navigateTo('/pages/merchant/goods-edit/goods-edit')
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
    this.openConfirmModal({
      title: '批量上架',
      desc: `确定上架选中的 ${this.data.selectedIds.length} 个商品吗？`,
      action: 'ON_SHELF'
    })
  },

  batchOffShelf() {
    if (!this.data.selectedIds.length) {
      util.showToast('请先选择商品')
      return
    }
    this.openConfirmModal({
      title: '批量下架',
      desc: `确定下架选中的 ${this.data.selectedIds.length} 个商品吗？`,
      action: 'OFF_SHELF'
    })
  },

  openConfirmModal(confirmModal) {
    this.setData({
      confirmModalVisible: true,
      confirmModal
    })
  },

  closeConfirmModal() {
    this.setData({
      confirmModalVisible: false,
      confirmModal: {
        title: '',
        desc: '',
        action: ''
      }
    })
  },

  confirmBatchAction() {
    const status = this.data.confirmModal.action
    if (!status) return
    const result = util.batchUpdateGoodsStatus(this.data.selectedIds, status)
    util.showToast(result.message, 'success')
    this.setData({
      selectedIds: [],
      batchMode: false
    })
    this.closeConfirmModal()
    this.loadData()
  }
})
