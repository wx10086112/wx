package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 超级管理员后台视角切换
 */
@RestController
@RequestMapping("/system/view")
public class AdminViewSwitchController extends BaseController {

    @Resource
    private TokenService tokenService;

    @Resource
    private IDistributorService distributorService;

    /**
     * 切换为分销商视角
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:switch')")
    @PostMapping("/switch-distributor/{id}")
    public AjaxResult switchDistributor(@PathVariable Long id) {
        // 只有超管可以切换
        if (!MallDataScopeHelper.isSuperAdmin()) {
            return AjaxResult.error("无权限操作");
        }
        Distributor distributor = distributorService.selectDistributorById(id);
        if (distributor == null) {
            return AjaxResult.error("分销商不存在");
        }
        if (distributor.getStatus() != 1) {
            return AjaxResult.error("该分销商已被禁用");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        loginUser.setActiveViewType("DISTRIBUTOR");
        loginUser.setActiveDistributorId(id);
        tokenService.setLoginUser(loginUser);
        Map<String, Object> result = new HashMap<>();
        result.put("activeViewType", "DISTRIBUTOR");
        result.put("activeDistributorId", id);
        result.put("activeDistributorName", distributor.getName());
        return AjaxResult.success("已切换为分销商: " + distributor.getName(), result);
    }

    /**
     * 返回平台视角
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:switch')")
    @PostMapping("/back-platform")
    public AjaxResult backPlatform() {
        if (!MallDataScopeHelper.isSuperAdmin()) {
            return AjaxResult.error("无权限操作");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        loginUser.setActiveViewType("PLATFORM");
        loginUser.setActiveDistributorId(null);
        tokenService.setLoginUser(loginUser);
        return AjaxResult.success("已返回平台视角");
    }

    /**
     * 获取当前视角信息
     */
    @GetMapping("/current")
    public AjaxResult getCurrent() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Map<String, Object> result = new HashMap<>();
        result.put("accountType", loginUser.getAccountType());
        result.put("activeViewType", loginUser.getActiveViewType());
        result.put("activeDistributorId", loginUser.getActiveDistributorId());
        return AjaxResult.success(result);
    }
}
