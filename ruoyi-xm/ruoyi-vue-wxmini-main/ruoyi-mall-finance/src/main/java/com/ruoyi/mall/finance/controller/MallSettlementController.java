package com.ruoyi.mall.finance.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;
import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.domain.PlatformTransferRecord;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import com.ruoyi.mall.finance.service.IPlatformTransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/mall/settlement")
public class MallSettlementController extends BaseController {

    @Resource
    private IMerchantSettlementRecordService merchantSettlementService;
    @Resource
    private IDistributorSettlementRecordService distributorSettlementService;
    @Resource
    private IOrderProfitLedgerService profitLedgerService;
    @Resource
    private IPlatformTransferService platformTransferService;

    // ==================== 商家结算 ====================

    @DataScopeBiz(merchantAlias = "msr", distributorAlias = "msr")
    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/merchant/list")
    public TableDataInfo merchantList(MerchantSettlementRecord query) {
        startPage();
        List<MerchantSettlementRecord> list = merchantSettlementService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/merchant/{id}")
    public AjaxResult merchantDetail(@PathVariable Long id) {
        MerchantSettlementRecord record = merchantSettlementService.selectById(id);
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "商家结算记录");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/batch-transfer")
    public AjaxResult merchantBatchTransfer(@RequestBody List<Long> ids) {
        AjaxResult denied = checkMerchantSettlementAccess(ids);
        if (denied != null) {
            return denied;
        }
        merchantSettlementService.batchTransfer(ids);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/mark-arrived")
    public AjaxResult merchantMarkArrived(@RequestBody List<Long> ids) {
        AjaxResult denied = checkMerchantSettlementAccess(ids);
        if (denied != null) {
            return denied;
        }
        merchantSettlementService.batchMarkArrived(ids);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/mark-failed")
    public AjaxResult merchantMarkFailed(@RequestBody MerchantSettlementRecord params) {
        MerchantSettlementRecord record = merchantSettlementService.selectById(params.getId());
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "商家结算记录");
        if (denied != null) {
            return denied;
        }
        merchantSettlementService.markFailed(params.getId(), params.getFailReason());
        return AjaxResult.success();
    }

    // ==================== 分销商结算 ====================

    @DataScopeBiz(merchantAlias = "dsr", distributorAlias = "dsr")
    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/distributor/list")
    public TableDataInfo distributorList(DistributorSettlementRecord query) {
        startPage();
        List<DistributorSettlementRecord> list = distributorSettlementService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/distributor/{id}")
    public AjaxResult distributorDetail(@PathVariable Long id) {
        DistributorSettlementRecord record = distributorSettlementService.selectById(id);
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "分销商结算记录");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/batch-arrived")
    public AjaxResult distributorBatchArrived(@RequestBody List<Long> ids) {
        AjaxResult denied = checkDistributorSettlementAccess(ids);
        if (denied != null) {
            return denied;
        }
        distributorSettlementService.batchMarkArrived(ids);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/mark-failed")
    public AjaxResult distributorMarkFailed(@RequestBody DistributorSettlementRecord params) {
        DistributorSettlementRecord record = distributorSettlementService.selectById(params.getId());
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "分销商结算记录");
        if (denied != null) {
            return denied;
        }
        distributorSettlementService.markFailed(params.getId(), params.getFailReason());
        return AjaxResult.success();
    }

    // ==================== 分账流水 ====================

    @DataScopeBiz(merchantAlias = "opl", distributorAlias = "opl")
    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/profit-ledger/list")
    public TableDataInfo profitLedgerList(OrderProfitLedger query) {
        startPage();
        List<OrderProfitLedger> list = profitLedgerService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/profit-ledger/{id}")
    public AjaxResult profitLedgerDetail(@PathVariable Long id) {
        OrderProfitLedger record = profitLedgerService.selectById(id);
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "分账流水");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(record);
    }

    // ==================== 商家微信转账 ====================

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/transfer/{settlementId}")
    public AjaxResult merchantTransfer(@PathVariable Long settlementId) {
        MerchantSettlementRecord settlement = merchantSettlementService.selectById(settlementId);
        AjaxResult denied = checkAccess(settlement != null ? settlement.getMerchantId() : null,
                settlement != null ? settlement.getDistributorId() : null,
                "商家结算记录");
        if (denied != null) {
            return denied;
        }
        String operatorId = getUsername();
        PlatformTransferRecord record = platformTransferService.createMerchantTransfer(settlementId, operatorId);
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/batch-transfer-real")
    public AjaxResult merchantBatchTransferReal(@RequestBody List<Long> ids) {
        AjaxResult denied = checkMerchantSettlementAccess(ids);
        if (denied != null) {
            return denied;
        }
        String operatorId = getUsername();
        List<PlatformTransferRecord> records = platformTransferService.batchCreateMerchantTransfer(ids, operatorId);
        return AjaxResult.success(records);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/merchant/transfer/{transferNo}")
    public AjaxResult merchantTransferDetail(@PathVariable String transferNo) {
        PlatformTransferRecord record = platformTransferService.selectByTransferNo(transferNo);
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "转账记录");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(record);
    }

    // ==================== 分销商微信转账 ====================

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/transfer/{settlementId}")
    public AjaxResult distributorTransfer(@PathVariable Long settlementId) {
        DistributorSettlementRecord settlement = distributorSettlementService.selectById(settlementId);
        AjaxResult denied = checkAccess(settlement != null ? settlement.getMerchantId() : null,
                settlement != null ? settlement.getDistributorId() : null,
                "分销商结算记录");
        if (denied != null) {
            return denied;
        }
        String operatorId = getUsername();
        PlatformTransferRecord record = platformTransferService.createDistributorTransfer(settlementId, operatorId);
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/batch-transfer-real")
    public AjaxResult distributorBatchTransferReal(@RequestBody List<Long> ids) {
        AjaxResult denied = checkDistributorSettlementAccess(ids);
        if (denied != null) {
            return denied;
        }
        String operatorId = getUsername();
        List<PlatformTransferRecord> records = platformTransferService.batchCreateDistributorTransfer(ids, operatorId);
        return AjaxResult.success(records);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/distributor/transfer/{transferNo}")
    public AjaxResult distributorTransferDetail(@PathVariable String transferNo) {
        PlatformTransferRecord record = platformTransferService.selectByTransferNo(transferNo);
        AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                record != null ? record.getDistributorId() : null,
                "转账记录");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(record);
    }

    // ==================== 转账记录 ====================

    @DataScopeBiz(merchantAlias = "t", distributorAlias = "t")
    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/transfer/list")
    public TableDataInfo transferList(PlatformTransferRecord query) {
        startPage();
        List<PlatformTransferRecord> list = platformTransferService.selectList(query);
        return getDataTable(list);
    }

    private AjaxResult checkMerchantSettlementAccess(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return AjaxResult.error("请选择商家结算记录");
        }
        for (Long id : ids) {
            MerchantSettlementRecord record = merchantSettlementService.selectById(id);
            AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                    record != null ? record.getDistributorId() : null,
                    "商家结算记录");
            if (denied != null) {
                return denied;
            }
        }
        return null;
    }

    private AjaxResult checkDistributorSettlementAccess(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return AjaxResult.error("请选择分销商结算记录");
        }
        for (Long id : ids) {
            DistributorSettlementRecord record = distributorSettlementService.selectById(id);
            AjaxResult denied = checkAccess(record != null ? record.getMerchantId() : null,
                    record != null ? record.getDistributorId() : null,
                    "分销商结算记录");
            if (denied != null) {
                return denied;
            }
        }
        return null;
    }

    private AjaxResult checkAccess(Long merchantId, Long distributorId, String label) {
        if (merchantId == null && distributorId == null) {
            return AjaxResult.error(label + "不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return AjaxResult.error("无权限查看该" + label);
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(distributorId)) {
            return AjaxResult.error("无权限查看该" + label);
        }
        return null;
    }
}
