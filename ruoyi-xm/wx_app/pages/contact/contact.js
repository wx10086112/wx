const templateService = require('../../services/template')

Page({
  data: {
    brandInfo: templateService.getTemplateSection('brandInfo')
  }
})
