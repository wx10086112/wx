const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')
const { toListThumbnailUrl } = require('../../../utils/image-url')

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
    },
    merchantNavList: util.getMerchantNavList('goods')
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
      .catch((err = {}) => {
        this.renderGoods([])
        util.showToast(err.message || '商品列表加载失败')
      })
  },

  renderGoods(sourceList = []) {
    const selectedIdSet = new Set(this.data.selectedIds)
    const goodsList = [...sourceList]
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
      .map((item) => ({
        ...item,
        imageUrl: toListThumbnailUrl(item.imageUrl),
        priceText: util.formatPrice(item.price),
        originalPriceText: util.formatPrice(item.originalPrice),
        imageCropStyle: buildImageCropStyle(item.imageCrop),
        selected: selectedIdSet.has(item.goodsId)
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
      this.renderGoods(util.getGoodsList())
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
      .catch((err = {}) => {
        util.showToast(err.message || '状态更新失败，请重试')
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
    }, () => this.renderGoods(util.getGoodsList()))
  },

  toggleSelectGoods(e) {
    const goodsId = Number(e.currentTarget.dataset.id)
    let selectedIds = [...this.data.selectedIds]
    const index = selectedIds.indexOf(goodsId)
    const nextSelected = index === -1
    if (index > -1) {
      selectedIds.splice(index, 1)
    } else {
      selectedIds.push(goodsId)
    }
    const visibleIndex = this.data.goodsList.findIndex((item) => item.goodsId === goodsId)
    const nextData = { selectedIds }
    if (visibleIndex > -1) {
      nextData[`goodsList[${visibleIndex}].selected`] = nextSelected
    }
    this.setData(nextData)
  },

  selectAll() {
    const allIds = this.data.goodsList.map((item) => item.goodsId)
    const isAllSelected = this.data.selectedIds.length === allIds.length
    this.setData({
      selectedIds: isAllSelected ? [] : allIds
    }, () => this.renderGoods(util.getGoodsList()))
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
    Promise.all(
      this.data.selectedIds.map((goodsId) =>
        api.updateMerchantGoodsStatus({ goodsId, status })
      )
    )
      .then(() => {
        util.showToast('批量操作成功', 'success')
        this.setData({
          selectedIds: [],
          batchMode: false
        })
        this.closeConfirmModal()
        this.loadData()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '批量操作失败，请重试')
      })
  },

  goMerchantTab(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
