package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wxmini.domain.Merchant;
import com.ruoyi.wxmini.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantAuditController extends BaseController {

    @Autowired
    private IMerchantService merchantService;

    /**
     * 商家审核列表 - 查询 status=2(待审核) 的商家
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/audit/list")
    public TableDataInfo auditList(Merchant query) {
        startPage();
        List<Merchant> list = merchantService.selectMerchantList(query);
        return getDataTable(list);
    }

    /**
     * 商家审核操作
     * status: 1=通过, 0=拒绝
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @PutMapping("/audit/{id}/{status}")
    public AjaxResult audit(@PathVariable Long id, @PathVariable Integer status) {
        Merchant merchant = merchantService.selectMerchantById(id);
        if (merchant == null) {
            return error("商家不存在");
        }
        // status: 1=正常(通过), 0=禁用(拒绝)
        merchant.setStatus(status == 1 ? 1 : 0);
        return toAjax(merchantService.updateMerchant(merchant));
    }
}
