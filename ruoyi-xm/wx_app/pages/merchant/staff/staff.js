const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const ROLE_PERMISSION_TEXT = {
  owner: '店长可管理商品、门店、员工、财务和营销，并处理订单与核销。',
  member: '店员可处理订单、扫码核销、手动核销和查看核销记录。'
}

const USERNAME_PATTERN = /^[A-Za-z0-9_]{4,20}$/
const PHONE_PATTERN = /^1[3-9]\d{9}$/
const PASSWORD_MIN_LENGTH = 6

const app = getApp()

const createNewStaffForm = () => ({
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 'member'
})

const createEditStaffForm = () => ({
  staffId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 'member',
  status: 'ACTIVE'
})

const normalizeRoleKey = (roleKey = '') => {
  if (roleKey === 'manager') return 'owner'
  if (roleKey === 'clerk') return 'member'
  return roleKey === 'owner' ? 'owner' : 'member'
}

const buildStaffPayload = (form = {}, options = {}) => {
  const payload = {
    staffId: form.staffId,
    username: (form.username || '').trim(),
    password: (form.password || '').trim(),
    realName: (form.realName || '').trim(),
    phone: (form.phone || '').trim(),
    role: normalizeRoleKey(form.role)
  }

  if (options.includeUsername && !payload.username) {
    return { error: '请输入登录账号' }
  }
  if (options.includeUsername && !USERNAME_PATTERN.test(payload.username)) {
    return { error: '账号需为 4-20 位字母、数字或下划线' }
  }
  if (options.requirePassword && !payload.password) {
    return { error: '请输入初始密码' }
  }
  if (payload.password && payload.password.length < PASSWORD_MIN_LENGTH) {
    return { error: '密码至少 6 位' }
  }
  if (!payload.realName) {
    return { error: '请输入员工姓名' }
  }
  if (payload.phone && !PHONE_PATTERN.test(payload.phone)) {
    return { error: '请输入正确的手机号' }
  }
  if (!options.includeUsername) {
    delete payload.username
  }
  if (!payload.password) {
    delete payload.password
  }
  if (!payload.staffId) {
    delete payload.staffId
  }

  return { payload }
}

Page({
  data: {
    canManageStaff: false,
    staffList: [],
    showAddPanel: false,
    showEditPanel: false,
    newStaff: createNewStaffForm(),
    editStaff: createEditStaffForm()
  },

  onShow() {
    if (!app.needMerchantLogin()) return
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
        const normalizedList = this.normalizeStaffList(staffList)
        this.syncCurrentUser(normalizedList)
        this.setData({ staffList: normalizedList })
      })
      .catch((err) => {
        util.showToast(err.message || '员工数据加载失败')
      })
  },

  normalizeStaffList(staffList = []) {
    return staffList
      .map((item) => {
        const roleKey = normalizeRoleKey(item.roleKey)
        return {
          ...item,
          name: item.name || item.realName || '',
          username: item.username || '',
          phone: item.phone || '',
          roleKey,
          roleName: roleKey === 'owner' ? '店长' : '店员',
          permissionSummary: ROLE_PERMISSION_TEXT[roleKey] || '',
          status: item.status === 'DISABLED' ? 'DISABLED' : 'ACTIVE',
          statusText: item.status === 'DISABLED' ? '已停用' : '启用中'
        }
      })
      .sort((left, right) => {
        if (left.roleKey === right.roleKey) {
          return Number(left.staffId || 0) - Number(right.staffId || 0)
        }
        return left.roleKey === 'owner' ? -1 : 1
      })
  },

  syncCurrentUser(staffList = []) {
    const currentUser = app.globalData.staffUser || {}
    const matchedStaff = staffList.find((item) => item.staffId === currentUser.staffId)
    if (!matchedStaff) return
    app.globalData.staffUser = matchedStaff
    app.globalData.permissionCodes = matchedStaff.permissions || []
    wx.setStorageSync('merchantStaffUser', matchedStaff)
  },

  showAddStaff() {
    this.setData({
      showAddPanel: true,
      newStaff: createNewStaffForm()
    })
  },

  hideAddPanel() {
    this.setData({ showAddPanel: false })
  },

  showEditStaff(e) {
    const staffId = Number(e.currentTarget.dataset.id)
    const target = this.data.staffList.find((item) => item.staffId === staffId)
    if (!target) return

    this.setData({
      showEditPanel: true,
      editStaff: {
        staffId: target.staffId,
        username: target.username || '',
        password: '',
        realName: target.name || '',
        phone: target.phone || '',
        role: normalizeRoleKey(target.roleKey),
        status: target.status || 'ACTIVE'
      }
    })
  },

  hideEditPanel() {
    this.setData({ showEditPanel: false })
  },

  preventTap() {},

  handleNewStaffInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`newStaff.${key}`]: e.detail.value
    })
  },

  handleEditStaffInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`editStaff.${key}`]: e.detail.value
    })
  },

  submitAddStaff() {
    const result = buildStaffPayload(this.data.newStaff, {
      includeUsername: true,
      requirePassword: true
    })
    if (result.error) {
      util.showToast(result.error)
      return
    }

    api
      .addMerchantStaff(result.payload)
      .then(() => {
        util.showToast('员工已创建', 'success')
        this.setData({
          showAddPanel: false,
          newStaff: createNewStaffForm()
        })
        this.loadData()
      })
      .catch((err) => {
        util.showToast(err.message || '创建员工失败')
      })
  },

  submitEditStaff() {
    const result = buildStaffPayload(this.data.editStaff)
    const payload = result.payload

    if (result.error) {
      util.showToast(result.error)
      return
    }
    if (!payload || !payload.staffId) {
      util.showToast('员工信息有误')
      return
    }

    api
      .updateMerchantStaff(payload)
      .then(() => {
        util.showToast('员工信息已更新', 'success')
        this.setData({
          showEditPanel: false,
          editStaff: createEditStaffForm()
        })
        this.loadData()
      })
      .catch((err) => {
        util.showToast(err.message || '更新员工失败')
      })
  },

  toggleStaffStatus(e) {
    const staffId = Number(e.currentTarget.dataset.id)
    const target = this.data.staffList.find((item) => item.staffId === staffId)
    if (!target) return

    const nextStatus = target.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    api
      .updateStaffPermission({
        staffId,
        status: nextStatus
      })
      .then((staffList = []) => {
        const normalizedList = this.normalizeStaffList(staffList)
        this.syncCurrentUser(normalizedList)
        this.setData({ staffList: normalizedList })
        util.showToast(nextStatus === 'ACTIVE' ? '员工已启用' : '员工已停用', 'success')
      })
      .catch((err) => {
        util.showToast(err.message || '员工状态更新失败')
      })
  }
})
