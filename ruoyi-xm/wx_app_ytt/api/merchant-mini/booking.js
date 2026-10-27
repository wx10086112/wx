const { get, post } = require('../../utils/merchant-request')

const getMerchantBookingList = (data = {}) => get('/wxmini/merchant-mini/booking/list', data)
const confirmMerchantBooking = (bookingNo) => post(`/wxmini/merchant-mini/booking/confirm/${bookingNo}`)
const completeMerchantBooking = (bookingNo) => post(`/wxmini/merchant-mini/booking/complete/${bookingNo}`)
const cancelMerchantBooking = (bookingNo) => post(`/wxmini/merchant-mini/booking/cancel/${bookingNo}`)

module.exports = {
  getMerchantBookingList,
  confirmMerchantBooking,
  completeMerchantBooking,
  cancelMerchantBooking
}
