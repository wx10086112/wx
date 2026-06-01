package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 分销商管理Controller
 */
@RestController
@RequestMapping("/mall/distributor")
public class MallDistributorController extends BaseController {

    @Resource
    private IDistributorService distributorService;

    /**
     * 查询分销商列表
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:list')")
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
    public AjaxResult add(@RequestBody Distributor distributor) {
        try {
            return toAjax(distributorService.insertDistributor(distributor));
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:edit')")
    @Log(title = "分销商管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Distributor distributor) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && distributor.getId() != null && !effId.equals(distributor.getId())) {
            return AjaxResult.error("无权修改该分销商");
        }
        return toAjax(distributorService.updateDistributor(distributor));
    }

    /**
     * 删除分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:remove')")
    @Log(title = "分销商管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null) {
            for (Long id : ids) {
                if (!effId.equals(id)) {
                    return AjaxResult.error("无权删除该分销商");
                }
            }
        }
        return toAjax(distributorService.deleteDistributorByIds(ids));
    }

    /**
     * 修改分销商状态（启用/禁用）
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:status')")
    @Log(title = "分销商状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@RequestBody Distributor distributor) {
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && distributor.getId() != null && !effId.equals(distributor.getId())) {
            return AjaxResult.error("无权修改该分销商状态");
        }
        return toAjax(distributorService.updateDistributor(distributor));
    }

    /**
     * 重置分销商密码
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:resetPwd')")
    @Log(title = "重置分销商密码", businessType = BusinessType.UPDATE)
    @PutMapping("/reset-password")
    public AjaxResult resetPassword(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long effId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effId != null && !effId.equals(id)) {
            return AjaxResult.error("无权重置该分销商密码");
        }
        String password = params.get("password") != null ? params.get("password").toString() : "123456";
        return toAjax(distributorService.resetPassword(id, password));
    }
}
