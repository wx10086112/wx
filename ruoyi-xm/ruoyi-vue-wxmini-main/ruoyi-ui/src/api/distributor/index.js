import request from '@/utils/request'

// 分销商列表
export function listDistributor(query) {
  return request({
    url: '/mall/distributor/list',
    method: 'get',
    params: query
  })
}

// 分销商详情
export function getDistributor(id) {
  return request({
    url: '/mall/distributor/' + id,
    method: 'get'
  })
}

// 新增分销商
export function addDistributor(data) {
  return request({
    url: '/mall/distributor',
    method: 'post',
    data: data
  })
}

// 修改分销商
export function updateDistributor(data) {
  return request({
    url: '/mall/distributor',
    method: 'put',
    data: data
  })
}

// 删除分销商
export function deleteDistributor(ids) {
  return request({
    url: '/mall/distributor/' + ids,
    method: 'delete'
  })
}

// 修改分销商状态
export function changeDistributorStatus(data) {
  return request({
    url: '/mall/distributor/status',
    method: 'put',
    data: data
  })
}

// 重置密码
export function resetDistributorPassword(data) {
  return request({
    url: '/mall/distributor/reset-password',
    method: 'put',
    data: data
  })
}

// 切换为分销商视角
export function switchDistributor(id) {
  return request({
    url: '/mall/distributor/switch/' + id,
    method: 'post'
  })
}

// 返回超级管理员视角
export function switchBackDistributor() {
  return request({
    url: '/mall/distributor/switch-back',
    method: 'post'
  })
}

// 获取当前视角信息
export function getViewInfo() {
  return request({
    url: '/mall/distributor/view-info',
    method: 'get'
  })
}
