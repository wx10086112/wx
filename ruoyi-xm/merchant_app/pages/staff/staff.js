const mock = require('../../data/mock')
const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    canManageStaff: false,
    permissionOptions: mock.permissionOptions,
    staffList: [],
    selectedStaffId: null,
    selectedPermissionList: []
  },

  onShow() {
    if (!app.needLogin()) return
    const canManageStaff = app.hasAnyPermission(['staff.manage'])
    this.setData({ canManageStaff })
    if (canManageStaff) {
      this.loadData()
    }
  },

  loadData() {
    api
      .getStaffList()
      .then((staffList = []) => {
        util.setStaffList(staffList)
        this.renderStaffList(staffList)
      })
      .catch(() => {
        this.renderStaffList(util.getStaffList())
      })
  },

  renderStaffList(staffList = []) {
    const selectedStaff = staffList.find((item) => item.staffId === this.data.selectedStaffId) || staffList[0] || {}
    this.setData({
      staffList,
      selectedStaffId: selectedStaff.staffId || null,
      selectedPermissionList: selectedStaff.permissions || []
    })
  },

  selectStaff(e) {
    const staffId = Number(e.currentTarget.dataset.id)
    const targetStaff = this.data.staffList.find((item) => item.staffId === staffId) || {}
    this.setData({
      selectedStaffId: staffId,
      selectedPermissionList: targetStaff.permissions || []
    })
  },

  handlePermissionChange(e) {
    this.setData({
      selectedPermissionList: e.detail.value
    })
  },

  toggleStaffStatus(e) {
    const staffId = Number(e.currentTarget.dataset.id)
    const targetStaff = this.data.staffList.find((item) => item.staffId === staffId) || {}
    const nextStatus = targetStaff.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'

    api
      .updateStaffPermission({
        staffId,
        status: nextStatus
      })
      .then((staffList = []) => {
        this.applyStaffList(staffList)
        util.showToast('员工状态已更新', 'success')
      })
      .catch(() => {
        const nextStaffList = this.data.staffList.map((item) =>
          item.staffId === staffId
            ? {
                ...item,
                status: nextStatus
              }
            : item
        )
        this.applyStaffList(nextStaffList)
        util.showToast('后端未联通，已更新本地演示数据')
      })
  },

  savePermission() {
    api
      .updateStaffPermission({
        staffId: this.data.selectedStaffId,
        permissions: this.data.selectedPermissionList
      })
      .then((staffList = []) => {
        this.applyStaffList(staffList)
        util.showToast('权限已保存', 'success')
      })
      .catch(() => {
        const nextStaffList = this.data.staffList.map((item) =>
          item.staffId === this.data.selectedStaffId
            ? {
                ...item,
                permissions: this.data.selectedPermissionList
              }
            : item
        )
        this.applyStaffList(nextStaffList)
        util.showToast('后端未联通，已保存本地演示数据')
      })
  },

  applyStaffList(staffList = []) {
    util.setStaffList(staffList)
    this.renderStaffList(staffList)
    this.syncCurrentUser(staffList)
  },

  syncCurrentUser(staffList = []) {
    const currentUser = app.globalData.staffUser || {}
    const matchedStaff = staffList.find((item) => item.staffId === currentUser.staffId)
    if (!matchedStaff) return
    const nextStaffUser = {
      ...currentUser,
      permissions: matchedStaff.permissions || []
    }
    app.globalData.staffUser = nextStaffUser
    app.globalData.permissionCodes = nextStaffUser.permissions
    wx.setStorageSync('merchantStaffUser', nextStaffUser)
  }
})
