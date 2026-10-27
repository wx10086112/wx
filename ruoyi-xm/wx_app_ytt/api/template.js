const { get } = require('./request')

const getTemplateConfig = (data = {}) => {
  return get('/wxmini/template/config', data)
}

module.exports = {
  getTemplateConfig
}
