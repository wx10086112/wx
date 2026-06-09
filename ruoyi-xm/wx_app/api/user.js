const { get, post, put, del } = require('./request')

const login = (appid, code) => {
  return get('/wxmini/login', { appid, code })
}

const bindPhoneByCode = (code) => {
  return post('/wxmini/user/phone/bind', { code })
}

const getUserInfo = () => {
  return get('/wxmini/user/info')
}

const updateUserInfo = (data) => {
  return put('/wxmini/user/info', data)
}

const cancelAccount = () => {
  return del('/wxmini/user/account')
}

module.exports = {
  login,
  bindPhoneByCode,
  getUserInfo,
  updateUserInfo,
  cancelAccount
}
