const AGREEMENT_SCOPES = {
  user: {
    key: 'o2o_agreement_consent_v1',
    version: '2026-06-05'
  },
  merchant: {
    key: 'o2o_merchant_agreement_consent_v1',
    version: '2026-06-08'
  }
}

const AGREEMENT_VERSION = AGREEMENT_SCOPES.user.version
const MERCHANT_AGREEMENT_VERSION = AGREEMENT_SCOPES.merchant.version

const normalizeAudience = (audience = 'user') => {
  return AGREEMENT_SCOPES[audience] ? audience : 'user'
}

const getScopeConfig = (audience = 'user') => {
  return AGREEMENT_SCOPES[normalizeAudience(audience)]
}

const getAgreementConsent = (audience = 'user') => {
  try {
    const consent = wx.getStorageSync(getScopeConfig(audience).key)
    if (!consent || typeof consent !== 'object') return null
    return consent
  } catch (e) {
    return null
  }
}

const isAgreementAccepted = (audience = 'user') => {
  const config = getScopeConfig(audience)
  const consent = getAgreementConsent(audience)
  return !!(consent && consent.accepted && consent.version === config.version)
}

const acceptAgreement = (audience = 'user') => {
  const config = getScopeConfig(audience)
  const consent = {
    accepted: true,
    audience: normalizeAudience(audience),
    version: config.version,
    acceptedAt: Date.now()
  }
  wx.setStorageSync(config.key, consent)
  return consent
}

const rejectAgreement = (audience = 'user') => {
  wx.removeStorageSync(getScopeConfig(audience).key)
}

const showAgreementRequiredToast = () => {
  wx.showToast({
    title: '请先阅读并同意相关协议',
    icon: 'none',
    duration: 2000
  })
}

const assertAgreementAccepted = (audience = 'user') => {
  if (isAgreementAccepted(audience)) return true
  showAgreementRequiredToast()
  return false
}

module.exports = {
  AGREEMENT_SCOPES,
  AGREEMENT_VERSION,
  MERCHANT_AGREEMENT_VERSION,
  getAgreementConsent,
  isAgreementAccepted,
  acceptAgreement,
  rejectAgreement,
  assertAgreementAccepted,
  showAgreementRequiredToast
}
