const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

const permissionOptions = [
  { label: '查看经营数据', value: 'stats.view' },
  { label: '订单处理', value: 'order.manage' },
  { label: '扫码核销', value: 'verify.scan' },
  { label: '手动核销', value: 'verify.manual' },
  { label: '核销记录', value: 'verify.record' },
  { label: '商品管理', value: 'goods.manage' },
  { label: '门店设置', value: 'store.manage' },
  { label: '员工权限', value: 'staff.manage' },
  { label: '财务收益', value: 'finance.manage' }
]

Page({
  data: {
    canManageStaff: false,
    permissionOptions,
    roleOptions: [
      { label: '店长', value: 'manager' },
      { label: '店员', value: 'clerk' }
    ],
    staffList: [],
    selectedStaffId: null,
    selectedPermissionList: [],
    showAddPanel: false,
    showEditPanel: false,
    newStaff: {
      name: '',
      phone: '',
      roleKey: 'clerk'
    },
    editStaff: {
      staffId: null,
      name: '',
      phone: ''
    }
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
        this.renderStaffList(staffList)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
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
      .then(() => {
        util.showToast('员工状态已更新', 'success')
        this.loadData()
      })
      .catch(() => {
        util.showToast('操作失败，请重试')
      })
  },

  savePermission() {
    api
      .updateStaffPermission({
        staffId: this.data.selectedStaffId,
        permissions: this.data.selectedPermissionList
      })
      .then(() => {
        util.showToast('权限已保存', 'success')
        this.loadData()
      })
      .catch(() => {
        util.showToast('保存失败，请重试')
      })
  },

  /* --- 新增员工 --- */
  showAddStaff() {
    this.setData({
      showAddPanel: true,
      newStaff: { name: '', phone: '', roleKey: 'clerk' }
    })
  },

  hideAddPanel() {
    this.setData({ showAddPanel: false })
  },

  handleNewStaffInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`newStaff.${key}`]: e.detail.value
    })
  },

  handleNewStaffRole(e) {
    this.setData({
      'newStaff.roleKey': this.data.roleOptions[e.detail.value].value
    })
  },

  submitAddStaff() {
    const { name, phone, roleKey } = this.data.newStaff
    if (!name.trim()) {
      util.showToast('请输入员工姓名')
      return
    }
    if (!phone.trim()) {
      util.showToast('请输入手机号')
      return
    }

    api.addMerchantStaff({ name: name.trim(), phone: phone.trim(), roleKey })
      .then(() => {
        util.showToast('添加成功', 'success')
        this.setData({ showAddPanel: false })
        this.loadData()
      })
      .catch(() => {
        util.showToast('添加失败，请重试')
      })
  },

  /* --- 编辑员工 --- */
  showEditStaff(e) {
    const staffId = Number(e.currentTarget.dataset.id)
    const target = this.data.staffList.find((item) => item.staffId === staffId) || {}
    this.setData({
      showEditPanel: true,
      editStaff: {
        staffId: target.staffId,
        name: target.name || '',
        phone: target.phone || ''
      }
    })
  },

  hideEditPanel() {
    this.setData({ showEditPanel: false })
  },

  handleEditStaffInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`editStaff.${key}`]: e.detail.value
    })
  },

  submitEditStaff() {
    const { staffId, name, phone } = this.data.editStaff
    if (!name.trim()) {
      util.showToast('请输入员工姓名')
      return
    }

    api.updateMerchantStaff({ staffId, name: name.trim(), phone: phone.trim() })
      .then(() => {
        util.showToast('修改成功', 'success')
        this.setData({ showEditPanel: false })
        this.loadData()
      })
      .catch(() => {
        util.showToast('修改失败，请重试')
      })
  }
})
