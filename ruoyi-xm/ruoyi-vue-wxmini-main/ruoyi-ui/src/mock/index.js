/**
 * Mock工具 - 模拟API返回数据
 * 后期接入真实接口时，删除此文件，将API改为axios请求即可
 */

export function mockSuccess(data, msg = '操作成功') {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg,
        data
      })
    }, 300 + Math.random() * 300)
  })
}

export function mockPage(list, pageNum = 1, pageSize = 10) {
  const start = (pageNum - 1) * pageSize
  const end = start + pageSize
  const pageList = list.slice(start, end)
  return mockSuccess({
    list: pageList,
    total: list.length,
    pageNum,
    pageSize
  })
}

export function mockError(msg = '操作失败') {
  return new Promise((_, reject) => {
    setTimeout(() => {
      reject({ code: 500, msg })
    }, 200)
  })
}
