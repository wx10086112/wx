const { get, post } = require('./request')

const getBookingList = (data = {}) => {
  return get('/wxmini/booking/list', data)
}

const createBooking = (data) => {
  return post('/wxmini/booking/create', data)
}

const cancelBooking = (bookingNo) => {
  return post(`/wxmini/booking/cancel/${bookingNo}`)
}

module.exports = {
  getBookingList,
  createBooking,
  cancelBooking
}
