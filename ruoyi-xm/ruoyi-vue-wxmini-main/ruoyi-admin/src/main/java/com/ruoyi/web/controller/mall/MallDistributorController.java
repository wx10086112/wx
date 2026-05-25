package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
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
        List<Distributor> list = distributorService.selectDistributorList(distributor);
        return getDataTable(list);
    }

    /**
     * 获取分销商详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
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
        return toAjax(distributorService.updateDistributor(distributor));
    }

    /**
     * 删除分销商
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:remove')")
    @Log(title = "分销商管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(distributorService.deleteDistributorByIds(ids));
    }

    /**
     * 修改分销商状态（启用/禁用）
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:status')")
    @Log(title = "分销商状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@RequestBody Distributor distributor) {
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
        String password = params.get("password") != null ? params.get("password").toString() : "123456";
        return toAjax(distributorService.resetPassword(id, password));
    }

    /**
     * 切换为分销商视角
     */
    @PreAuthorize("@ss.hasPermi('mall:distributor:switch')")
    @Log(title = "切换分销商视角", businessType = BusinessType.OTHER)
    @PostMapping("/switch/{id}")
    public AjaxResult switchDistributor(@PathVariable Long id) {
        Distributor distributor = distributorService.selectDistributorById(id);
        if (distributor == null) {
            return AjaxResult.error("分销商不存在");
        }
        if (distributor.getStatus() != 1) {
            return AjaxResult.error("该分销商已被禁用");
        }
        // 将视角信息存入 session
        Map<String, Object> viewInfo = new HashMap<>();
        viewInfo.put("viewRole", "DISTRIBUTOR_ADMIN");
        viewInfo.put("viewDistributorId", id);
        viewInfo.put("viewDistributorName", distributor.getName());
        getSession().setAttribute("distributorView", viewInfo);
        return AjaxResult.success("已切换为分销商: " + distributor.getName(), viewInfo);
    }

    /**
     * 返回超级管理员视角
     */
    @PostMapping("/switch-back")
    public AjaxResult switchBack() {
        getSession().removeAttribute("distributorView");
        return AjaxResult.success("已返回超级管理员视角");
    }

    /**
     * 获取当前视角信息
     */
    @GetMapping("/view-info")
    public AjaxResult getViewInfo() {
        Object view = getSession().getAttribute("distributorView");
        Map<String, Object> result = new HashMap<>();
        result.put("isSwitched", view != null);
        result.put("viewInfo", view);
        return AjaxResult.success(result);
    }
}
