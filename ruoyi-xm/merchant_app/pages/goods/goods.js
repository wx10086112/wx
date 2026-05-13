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
    goodsList: []
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
        originalPriceText: util.formatPrice(item.originalPrice)
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
    util.navigateTo(`/pages/goods-edit/goods-edit?goodsId=${goodsId}`)
  },

  goAddGoods() {
    if (!app.needPermission(['goods.manage'])) return
    util.navigateTo('/pages/goods-edit/goods-edit')
  }
})
