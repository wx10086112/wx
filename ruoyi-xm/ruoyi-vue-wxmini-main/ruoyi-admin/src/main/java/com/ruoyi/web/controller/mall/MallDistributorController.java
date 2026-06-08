package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import com.ruoyi.system.mapper.SysMenuMapper;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * 分销商管理Controller
 */
@RestController
@RequestMapping("/mall/distributor")
public class MallDistributorController extends BaseController {

    private static final String ACCOUNT_TYPE_DISTRIBUTOR = "DISTRIBUTOR";
    private static final String DISTRIBUTOR_ROLE_KEY = "DISTRIBUTOR_ADMIN";
    private static final String DISTRIBUTOR_ROLE_NAME = "分销商管理员";
    private static final String ROLE_SCOPE_DISTRIBUTOR = "DISTRIBUTOR";
    private static final String DATA_SCOPE_SELF = "5";
    private static final String DATA_SCOPE_TYPE_DISTRIBUTOR_SELF = "DISTRIBUTOR_SELF";

    @Resource
    private IDistributorService distributorService;

    @Resource
    private IMerchantService merchantService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysRoleService sysRoleService;

    @Resource
    private SysMenuMapper sysMenuMapper;

    /**
     * 查询分销商列表
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:list')")
    @DataScopeBiz(distributorAlias = "d", merchantAlias = "d")
    @GetMapping("/list")
    public TableDataInfo list(Distributor distributor) {
        startPage();
        // 分销商账号或超管切换分销商视角时，只能查看当前分销商自身。
        Long effectiveDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effectiveDistributorId != null) {
            distributor.setId(effectiveDistributorId);
        }
        List<Distributor> list = distributorService.selectDistributorList(distributor);
        return getDataTable(list);
    }

    /**
     * 获取分销商详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && !effId.equals(id)) {
            return AjaxResult.error("无权查看该分销商");
        }
        return AjaxResult.success(distributorService.selectDistributorById(id));
    }

    /**
     * 新增分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:add')")
    @Log(title = "分销商管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@RequestBody Distributor distributor) {
        try {
            String rawPassword = distributor.getPassword();
            int rows = distributorService.insertDistributor(distributor);
            if (rows > 0) {
                syncDistributorLoginUser(distributor, rawPassword, true);
                merchantService.clearRevivedDistributorBindings(distributor.getId());
            }
            return toAjax(rows);
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:edit')")
    @Log(title = "分销商管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@RequestBody Distributor distributor) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && distributor.getId() != null && !effId.equals(distributor.getId())) {
            return AjaxResult.error("无权修改该分销商");
        }
        if (distributor.getId() == null) {
            return AjaxResult.error("分销商ID不能为空");
        }
        if (distributorService.selectDistributorById(distributor.getId()) == null) {
            return AjaxResult.error("分销商不存在");
        }
        int rows = distributorService.updateDistributor(distributor);
        if (rows > 0) {
            Distributor saved = distributorService.selectDistributorById(distributor.getId());
            syncDistributorLoginUser(saved, null, false);
        }
        return toAjax(rows);
    }

    /**
     * 删除分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:remove')")
    @Log(title = "分销商管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null) {
            for (Long id : ids) {
                if (!effId.equals(id)) {
                    return AjaxResult.error("无权删除该分销商");
                }
            }
        }
        for (Long id : ids) {
            Distributor distributor = distributorService.selectDistributorById(id);
            deleteDistributorLoginUser(distributor);
        }
        merchantService.clearDistributorBindingsByDistributorIds(ids);
        return toAjax(distributorService.deleteDistributorByIds(ids));
    }

    /**
     * 修改分销商状态（启用/禁用）
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:status')")
    @Log(title = "分销商状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult changeStatus(@RequestBody Distributor distributor) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && distributor.getId() != null && !effId.equals(distributor.getId())) {
            return AjaxResult.error("无权修改该分销商状态");
        }
        int rows = distributorService.updateDistributor(distributor);
        if (rows > 0) {
            Distributor saved = distributorService.selectDistributorById(distributor.getId());
            syncDistributorLoginUser(saved, null, false);
        }
        return toAjax(rows);
    }

    /**
     * 重置分销商密码
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:resetPwd')")
    @Log(title = "重置分销商密码", businessType = BusinessType.UPDATE)
    @PutMapping("/reset-password")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult resetPassword(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && !effId.equals(id)) {
            return AjaxResult.error("无权重置该分销商密码");
        }
        Distributor distributor = distributorService.selectDistributorById(id);
        if (distributor == null) {
            return AjaxResult.error("分销商不存在");
        }
        String password = params.get("password") != null ? params.get("password").toString() : "123456";
        int rows = distributorService.resetPassword(id, password);
        if (rows > 0) {
            resetDistributorLoginPassword(distributor, password);
        }
        return toAjax(rows);
    }

    private void syncDistributorLoginUser(Distributor distributor, String rawPassword, boolean requirePassword) {
        if (distributor == null) {
            return;
        }
        Long roleId = findDistributorRoleId();
        String username = normalizeUsername(distributor.getUsername());
        if (username == null) {
            throw new RuntimeException("分销商后台登录账号不能为空");
        }

        SysUser boundUser = findDistributorLoginUser(distributor.getId());
        SysUser usernameUser = sysUserService.selectUserByUserName(username);
        if (usernameUser != null && (boundUser == null || !usernameUser.getUserId().equals(boundUser.getUserId()))) {
            throw new RuntimeException("登录账号已被其他后台账号占用");
        }

        if (boundUser == null) {
            if (requirePassword && (rawPassword == null || rawPassword.trim().isEmpty())) {
                throw new RuntimeException("分销商后台登录密码不能为空");
            }
            SysUser user = buildDistributorSysUser(distributor);
            user.setPassword(SecurityUtils.encryptPassword(rawPassword));
            user.setRoleIds(new Long[]{roleId});
            user.setCreateBy(getUsername());
            sysUserService.insertUser(user);
            return;
        }

        SysUser updateUser = buildDistributorSysUser(distributor);
        updateUser.setUserId(boundUser.getUserId());
        updateUser.setUpdateBy(getUsername());
        sysUserService.updateUserProfile(updateUser);
        sysUserService.insertUserAuth(boundUser.getUserId(), new Long[]{roleId});
    }

    private SysUser buildDistributorSysUser(Distributor distributor) {
        SysUser user = new SysUser();
        user.setUserName(normalizeUsername(distributor.getUsername()));
        user.setNickName(distributor.getName());
        user.setPhonenumber(distributor.getPhone());
        user.setStatus(distributor.getStatus() != null && distributor.getStatus() == 1 ? "0" : "1");
        user.setAccountType(ACCOUNT_TYPE_DISTRIBUTOR);
        user.setDistributorId(distributor.getId());
        user.setRemark("分销商后台账号：" + distributor.getName());
        return user;
    }

    private void resetDistributorLoginPassword(Distributor distributor, String rawPassword) {
        SysUser user = findDistributorLoginUser(distributor.getId());
        if (user == null) {
            syncDistributorLoginUser(distributor, rawPassword, true);
            return;
        }
        sysUserService.resetUserPwd(user.getUserName(), SecurityUtils.encryptPassword(rawPassword));
    }

    private void deleteDistributorLoginUser(Distributor distributor) {
        if (distributor == null) {
            return;
        }
        SysUser user = findDistributorLoginUser(distributor.getId());
        if (user != null) {
            sysUserService.deleteUserById(user.getUserId());
        }
    }

    private SysUser findDistributorLoginUser(Long distributorId) {
        if (distributorId == null) {
            return null;
        }
        SysUser query = new SysUser();
        query.setAccountType(ACCOUNT_TYPE_DISTRIBUTOR);
        List<SysUser> users = sysUserService.selectUserList(query);
        for (SysUser user : users) {
            if (distributorId.equals(user.getDistributorId())) {
                return user;
            }
        }
        return null;
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String trimmed = username.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long findDistributorRoleId() {
        Long[] menuIds = resolveDistributorDefaultMenuIds();
        List<SysRole> roles = sysRoleService.selectRoleAll();
        for (SysRole role : roles) {
            if (DISTRIBUTOR_ROLE_KEY.equals(role.getRoleKey())) {
                role.setRoleName(DISTRIBUTOR_ROLE_NAME);
                role.setRoleSort(10);
                role.setDataScope(DATA_SCOPE_SELF);
                role.setRoleScope(ROLE_SCOPE_DISTRIBUTOR);
                role.setDataScopeType(DATA_SCOPE_TYPE_DISTRIBUTOR_SELF);
                role.setMenuCheckStrictly(true);
                role.setDeptCheckStrictly(true);
                role.setStatus("0");
                role.setRemark("分销商后台默认角色");
                role.setMenuIds(menuIds);
                role.setUpdateBy(getUsername());
                sysRoleService.updateRole(role);
                return role.getRoleId();
            }
        }

        SysRole role = new SysRole();
        role.setRoleName(DISTRIBUTOR_ROLE_NAME);
        role.setRoleKey(DISTRIBUTOR_ROLE_KEY);
        role.setRoleSort(10);
        role.setDataScope(DATA_SCOPE_SELF);
        role.setRoleScope(ROLE_SCOPE_DISTRIBUTOR);
        role.setDataScopeType(DATA_SCOPE_TYPE_DISTRIBUTOR_SELF);
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        role.setStatus("0");
        role.setRemark("分销商后台默认角色");
        role.setMenuIds(menuIds);
        role.setCreateBy(getUsername());
        sysRoleService.insertRole(role);
        return role.getRoleId();
    }

    private Long[] resolveDistributorDefaultMenuIds() {
        SysMenu query = new SysMenu();
        query.setStatus("0");
        List<SysMenu> menus = sysMenuMapper.selectMenuList(query);
        Map<Long, SysMenu> menuMap = new HashMap<>();
        for (SysMenu menu : menus) {
            menuMap.put(menu.getMenuId(), menu);
        }

        Set<Long> menuIds = new LinkedHashSet<>();
        for (SysMenu menu : menus) {
            if (!isDistributorDefaultMenu(menu)) {
                continue;
            }
            appendMenuWithParents(menu, menuMap, menuIds);
        }
        return menuIds.toArray(new Long[0]);
    }

    private boolean isDistributorDefaultMenu(SysMenu menu) {
        String perms = menu.getPerms();
        if (perms == null) {
            return false;
        }
        String trimmed = perms.trim();
        return trimmed.startsWith("mall:") && !trimmed.startsWith("mall:distributor:");
    }

    private void appendMenuWithParents(SysMenu menu, Map<Long, SysMenu> menuMap, Set<Long> menuIds) {
        SysMenu current = menu;
        while (current != null && current.getMenuId() != null && menuIds.add(current.getMenuId())) {
            Long parentId = current.getParentId();
            if (parentId == null || parentId == 0L) {
                break;
            }
            current = menuMap.get(parentId);
        }
    }
}
