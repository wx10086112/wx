const { get, post, put, del } = require('./request')

const login = (appid, code) => {
  return get('/wxmini/login', { appid, code })
}

const quickLogin = (loginCode, phoneCode) => {
  return post('/wxmini/login/quick', { loginCode, phoneCode })
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

module.exports = {
  login,
  quickLogin,
  bindPhoneByCode,
  getUserInfo,
  updateUserInfo
}
