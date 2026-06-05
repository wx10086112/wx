const api = require('../../api/index')
const { defaultTemplateConfig } = require('./default-template')

const isPlainObject = (value) => {
  return Object.prototype.toString.call(value) === '[object Object]'
}

const clone = (value) => JSON.parse(JSON.stringify(value))

let templateConfigCache = null

const mergeConfig = (baseValue, overrideValue) => {
  if (Array.isArray(baseValue)) {
    return Array.isArray(overrideValue) && overrideValue.length ? clone(overrideValue) : clone(baseValue)
  }

  if (isPlainObject(baseValue)) {
    const result = {}
    const overrideObject = isPlainObject(overrideValue) ? overrideValue : {}
    Object.keys(baseValue).forEach((key) => {
      result[key] = mergeConfig(baseValue[key], overrideObject[key])
    })
    Object.keys(overrideObject).forEach((key) => {
      if (result[key] === undefined) {
        result[key] = clone(overrideObject[key])
      }
    })
    return result
  }

  return overrideValue !== undefined && overrideValue !== null ? overrideValue : baseValue
}

const buildLocalTemplateConfig = () => {
  return clone(defaultTemplateConfig)
}

const normalizeTemplateConfig = (rawConfig = {}) => {
  return mergeConfig(defaultTemplateConfig, rawConfig)
}

const resolveTemplateResponse = (response = {}) => {
  if (response.data) {
    return response.data
  }
  return response
}

const setTemplateConfigCache = (config = {}) => {
  templateConfigCache = normalizeTemplateConfig(config)
  return clone(templateConfigCache)
}

const getTemplateConfig = () => {
  if (templateConfigCache) {
    return clone(templateConfigCache)
  }
  templateConfigCache = buildLocalTemplateConfig()
  return clone(templateConfigCache)
}

const getTemplateSection = (sectionKey) => {
  const templateConfig = getTemplateConfig()
  return templateConfig[sectionKey] || {}
}

const fetchTemplateConfig = (options = {}) => {
  const { useRemote = false, force = false, requestData = {} } = options

  if (templateConfigCache && !force) {
    return Promise.resolve(clone(templateConfigCache))
  }

  if (!useRemote) {
    templateConfigCache = buildLocalTemplateConfig()
    return Promise.resolve(clone(templateConfigCache))
  }

  return api
    .getTemplateConfig(requestData)
    .then((response) => {
      return setTemplateConfigCache(resolveTemplateResponse(response))
    })
    .catch(() => {
      templateConfigCache = buildLocalTemplateConfig()
      return clone(templateConfigCache)
    })
}

module.exports = {
  buildLocalTemplateConfig,
  normalizeTemplateConfig,
  setTemplateConfigCache,
  fetchTemplateConfig,
  getTemplateConfig,
  getTemplateSection
}
