package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.merchant.service.IMerchantUserService;
import com.ruoyi.system.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/merchant-user")
public class MallMerchantUserController extends BaseController {

    @Autowired
    private IMerchantUserService merchantUserService;

    @Autowired
    private IMerchantService merchantService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 商家用户列表
     */
    @DataScopeBiz(merchantAlias = "merchant_user")
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(MerchantUser query) {
        startPage();
        List<MerchantUser> list = merchantUserService.selectMerchantUserList(query);
        return getDataTable(list);
    }

    /**
     * 商家用户详情
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        AjaxResult check = checkMerchantUserOwnership(id);
        if (check != null) {
            return check;
        }
        return success(merchantUserService.selectMerchantUserById(id));
    }

    /**
     * 新增商家用户
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:add')")
    @Log(title = "商家用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MerchantUser merchantUser) {
        normalizeMerchantUserForAdd(merchantUser);
        AjaxResult check = checkMerchantOwnership(merchantUser.getMerchantId());
        if (check != null) {
            return check;
        }
        if (StringUtils.isBlank(merchantUser.getUsername())) {
            return error("登录账号不能为空");
        }
        if (StringUtils.isBlank(merchantUser.getRealName())) {
            return error("姓名不能为空");
        }
        if (!merchantUserService.checkUsernameUnique(merchantUser.getUsername(), null)) {
            return error("新增用户'" + merchantUser.getUsername() + "'失败，登录账号已存在");
        }
        return toAjax(merchantUserService.insertMerchantUser(merchantUser));
    }

    /**
     * 修改商家用户
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商家用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MerchantUser merchantUser) {
        AjaxResult check = checkMerchantUserOwnership(merchantUser.getId());
        if (check != null) {
            return check;
        }
        MerchantUser existing = merchantUserService.selectMerchantUserById(merchantUser.getId());
        merchantUser.setMerchantId(existing.getMerchantId());
        if (!merchantUserService.checkUsernameUnique(merchantUser.getUsername(), merchantUser.getId())) {
            return error("修改用户'" + merchantUser.getUsername() + "'失败，登录账号已存在");
        }
        return toAjax(merchantUserService.updateMerchantUser(merchantUser));
    }

    /**
     * 删除商家用户
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @Log(title = "商家用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            AjaxResult check = checkMerchantUserOwnership(id);
            if (check != null) {
                return check;
            }
        }
        return toAjax(merchantUserService.deleteMerchantUserByIds(ids));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商家用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd/{id}")
    public AjaxResult resetPwd(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        AjaxResult check = checkMerchantUserOwnership(id);
        if (check != null) {
            return check;
        }
        String newPassword = body != null ? body.get("password") : null;
        if (newPassword == null || newPassword.trim().isEmpty()) {
            newPassword = configService.selectConfigByKey("sys.user.initPassword");
            if (newPassword == null) {
                newPassword = "123456";
            }
        }
        merchantUserService.resetPassword(id, newPassword);
        return success();
    }

    /**
     * 状态切换
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商家用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MerchantUser merchantUser) {
        AjaxResult check = checkMerchantUserOwnership(merchantUser.getId());
        if (check != null) {
            return check;
        }
        merchantUserService.changeStatus(merchantUser.getId(), merchantUser.getStatus());
        return success();
    }

    /**
     * 校验账号是否唯一
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/checkUsernameUnique")
    public AjaxResult checkUsernameUnique(@RequestParam String username,
                                          @RequestParam(required = false) Long id) {
        boolean unique = merchantUserService.checkUsernameUnique(username, id);
        return success(unique);
    }

    private AjaxResult checkMerchantUserOwnership(Long merchantUserId) {
        MerchantUser merchantUser = merchantUserService.selectMerchantUserById(merchantUserId);
        if (merchantUser == null) {
            return error("商家用户不存在");
        }
        return checkMerchantOwnership(merchantUser.getMerchantId());
    }

    private AjaxResult checkMerchantOwnership(Long merchantId) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return error("无权操作该商家用户");
        }
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant == null) {
            return error("关联商家不存在");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return error("无权操作该商家用户");
        }
        return null;
    }

    private void normalizeMerchantUserForAdd(MerchantUser merchantUser) {
        merchantUser.setUsername(StringUtils.trimToNull(merchantUser.getUsername()));
        merchantUser.setPassword(defaultPasswordIfBlank(merchantUser.getPassword()));
        merchantUser.setRealName(StringUtils.trimToNull(merchantUser.getRealName()));
        merchantUser.setPhone(StringUtils.trimToNull(merchantUser.getPhone()));
        merchantUser.setRole(StringUtils.defaultIfBlank(merchantUser.getRole(), "member"));
        if (merchantUser.getStatus() == null) {
            merchantUser.setStatus(1);
        }
    }

    private String defaultPasswordIfBlank(String password) {
        if (StringUtils.isNotBlank(password)) {
            return password.trim();
        }
        String defaultPassword = configService.selectConfigByKey("sys.user.initPassword");
        return StringUtils.defaultIfBlank(defaultPassword, "123456");
    }
}
