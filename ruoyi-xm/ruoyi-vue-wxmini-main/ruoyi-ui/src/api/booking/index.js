import request from '@/utils/request'

export function listBooking(query) {
  return request({
    url: '/mall/booking/list',
    method: 'get',
    params: query
  })
}

export function getBooking(id) {
  return request({
    url: '/mall/booking/' + id,
    method: 'get'
  })
}

export function confirmBooking(id) {
  return request({
    url: '/mall/booking/confirm/' + id,
    method: 'post'
  })
}

export function completeBooking(id) {
  return request({
    url: '/mall/booking/complete/' + id,
    method: 'post'
  })
}

export function cancelBooking(id) {
  return request({
    url: '/mall/booking/cancel/' + id,
    method: 'post'
  })
}

export function expireBooking(id) {
  return request({
    url: '/mall/booking/expire/' + id,
    method: 'post'
  })
}
