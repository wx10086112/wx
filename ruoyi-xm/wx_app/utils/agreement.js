const AGREEMENT_CONSENT_KEY = 'o2o_agreement_consent_v1'
const AGREEMENT_VERSION = '2026-06-05'

const getAgreementConsent = () => {
  try {
    const consent = wx.getStorageSync(AGREEMENT_CONSENT_KEY)
    if (!consent || typeof consent !== 'object') return null
    return consent
  } catch (e) {
    return null
  }
}

const isAgreementAccepted = () => {
  const consent = getAgreementConsent()
  return !!(consent && consent.accepted && consent.version === AGREEMENT_VERSION)
}

const acceptAgreement = () => {
  const consent = {
    accepted: true,
    version: AGREEMENT_VERSION,
    acceptedAt: Date.now()
  }
  wx.setStorageSync(AGREEMENT_CONSENT_KEY, consent)
  return consent
}

const rejectAgreement = () => {
  wx.removeStorageSync(AGREEMENT_CONSENT_KEY)
}

const showAgreementRequiredToast = () => {
  wx.showToast({
    title: '请先阅读并同意相关协议',
    icon: 'none',
    duration: 2000
  })
}

const assertAgreementAccepted = () => {
  if (isAgreementAccepted()) return true
  showAgreementRequiredToast()
  return false
}

module.exports = {
  AGREEMENT_VERSION,
  getAgreementConsent,
  isAgreementAccepted,
  acceptAgreement,
  rejectAgreement,
  assertAgreementAccepted,
  showAgreementRequiredToast
}
