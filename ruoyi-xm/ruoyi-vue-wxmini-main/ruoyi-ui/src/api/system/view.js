import request from '@/utils/request'

// 切换为分销商视角
export function switchDistributorView(distributorId) {
  return request({
    url: '/system/view/switch-distributor/' + distributorId,
    method: 'post'
  })
}

// 返回平台视角
export function backPlatformView() {
  return request({
    url: '/system/view/back-platform',
    method: 'post'
  })
}

// 获取当前视角信息
export function getCurrentView() {
  return request({
    url: '/system/view/current',
    method: 'get'
  })
}
