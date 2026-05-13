const { get, post, put, del } = require('./request')

const login = (appid, code) => {
  return get('/wxmini/login', { appid, code })
}

const getUserInfo = () => {
  return get('/wxmini/user/info')
}

const updateUserInfo = (data) => {
  return put('/wxmini/user/info', data)
}

module.exports = {
  login,
  getUserInfo,
  updateUserInfo
}
