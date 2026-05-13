Page({
  data: {
    orderNo: '',
    score: 5,
    content: '',
    tagOptions: [
      { label: '服务细致', selected: false },
      { label: '环境舒适', selected: false },
      { label: '核销顺畅', selected: false },
      { label: '性价比高', selected: false }
    ],
    selectedTags: []
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || ''
    })
  },

  onScoreChange(e) {
    this.setData({
      score: Number(e.detail.value)
    })
  },

  onContentInput(e) {
    this.setData({
      content: e.detail.value
    })
  },

  toggleTag(e) {
    const tag = e.currentTarget.dataset.tag
    const selectedTags = this.data.selectedTags.includes(tag)
      ? this.data.selectedTags.filter((item) => item !== tag)
      : this.data.selectedTags.concat(tag)

    this.setData({
      selectedTags,
      tagOptions: this.data.tagOptions.map((item) => ({
        ...item,
        selected: selectedTags.includes(item.label)
      }))
    })
  },

  submitReview() {
    if (!this.data.content.trim()) {
      wx.showToast({
        title: '请填写评价内容',
        icon: 'none'
      })
      return
    }

    wx.showToast({
      title: '评价已提交',
      icon: 'success'
    })
    setTimeout(() => {
      wx.navigateBack()
    }, 600)
  }
})
