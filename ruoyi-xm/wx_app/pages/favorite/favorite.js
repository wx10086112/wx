const mock = require('../../data/mock')
const util = require('../../utils/util')

Page({
  data: {
    favoriteList: [],
    isEmpty: true
  },

  onLoad() {
    this.loadFavorites()
  },

  onShow() {
    this.loadFavorites()
  },

  loadFavorites() {
    this.setData({ favoriteList: [], isEmpty: true })
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
