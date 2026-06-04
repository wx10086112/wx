const ensurePrivacyAuthorized = () => {
  return new Promise((resolve) => {
    if (!wx.requirePrivacyAuthorize) {
      resolve(true)
      return
    }

    wx.requirePrivacyAuthorize({
      success: () => {
        resolve(true)
      },
      fail: () => {
        wx.showToast({
          title: '请先同意微信隐私授权',
          icon: 'none',
          duration: 2000
        })
        resolve(false)
      }
    })
  })
}

module.exports = {
  ensurePrivacyAuthorized
}
