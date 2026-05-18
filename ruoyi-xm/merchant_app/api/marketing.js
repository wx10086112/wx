const { get, post, put } = require('../utils/request')

const getCouponList = () => get('/wxmini/merchant-mini/marketing/coupon/list')
const saveCoupon = (data) => post('/wxmini/merchant-mini/marketing/coupon/save', data)
const updateCouponStatus = (data) => put('/wxmini/merchant-mini/marketing/coupon/status', data)
const getPromotionList = () => get('/wxmini/merchant-mini/marketing/promotion/list')
const savePromotion = (data) => post('/wxmini/merchant-mini/marketing/promotion/save', data)

module.exports = {
  getCouponList,
  saveCoupon,
  updateCouponStatus,
  getPromotionList,
  savePromotion
}
