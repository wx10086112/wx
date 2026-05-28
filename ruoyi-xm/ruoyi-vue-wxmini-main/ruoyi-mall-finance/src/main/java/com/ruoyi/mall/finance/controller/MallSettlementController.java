package com.ruoyi.mall.finance.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
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

    @DataScopeBiz(merchantAlias = "merchant_settlement_record")
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
        return AjaxResult.success(merchantSettlementService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/batch-transfer")
    public AjaxResult merchantBatchTransfer(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            MerchantSettlementRecord record = merchantSettlementService.selectById(id);
            if (record != null && "WAITING_T1".equals(record.getStatus())) {
                record.setStatus("TRANSFERRING");
                record.setTransferTime(new Date());
                merchantSettlementService.updateById(record);
            }
        }
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/mark-arrived")
    public AjaxResult merchantMarkArrived(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            MerchantSettlementRecord record = merchantSettlementService.selectById(id);
            if (record != null && !"ARRIVED".equals(record.getStatus()) && !"CANCELLED".equals(record.getStatus())) {
                record.setStatus("ARRIVED");
                record.setArriveTime(new Date());
                merchantSettlementService.updateById(record);
            }
        }
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/mark-failed")
    public AjaxResult merchantMarkFailed(@RequestBody MerchantSettlementRecord params) {
        MerchantSettlementRecord record = merchantSettlementService.selectById(params.getId());
        if (record != null) {
            record.setStatus("FAILED");
            record.setFailReason(params.getFailReason());
            merchantSettlementService.updateById(record);
        }
        return AjaxResult.success();
    }

    // ==================== 分销商结算 ====================

    @DataScopeBiz(distributorAlias = "distributor_settlement_record")
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
        return AjaxResult.success(distributorSettlementService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/batch-arrived")
    public AjaxResult distributorBatchArrived(@RequestBody List<Long> ids) {
        distributorSettlementService.batchMarkArrived(ids);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/mark-failed")
    public AjaxResult distributorMarkFailed(@RequestBody DistributorSettlementRecord params) {
        distributorSettlementService.markFailed(params.getId(), params.getFailReason());
        return AjaxResult.success();
    }

    // ==================== 分账流水 ====================

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
        return AjaxResult.success(profitLedgerService.selectById(id));
    }

    // ==================== 商家微信转账 ====================

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/transfer/{settlementId}")
    public AjaxResult merchantTransfer(@PathVariable Long settlementId) {
        String operatorId = getUsername();
        PlatformTransferRecord record = platformTransferService.createMerchantTransfer(settlementId, operatorId);
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/merchant/batch-transfer-real")
    public AjaxResult merchantBatchTransferReal(@RequestBody List<Long> ids) {
        String operatorId = getUsername();
        List<PlatformTransferRecord> records = platformTransferService.batchCreateMerchantTransfer(ids, operatorId);
        return AjaxResult.success(records);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/merchant/transfer/{transferNo}")
    public AjaxResult merchantTransferDetail(@PathVariable String transferNo) {
        return AjaxResult.success(platformTransferService.selectByTransferNo(transferNo));
    }

    // ==================== 分销商微信转账 ====================

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/transfer/{settlementId}")
    public AjaxResult distributorTransfer(@PathVariable Long settlementId) {
        String operatorId = getUsername();
        PlatformTransferRecord record = platformTransferService.createDistributorTransfer(settlementId, operatorId);
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:edit')")
    @PostMapping("/distributor/batch-transfer-real")
    public AjaxResult distributorBatchTransferReal(@RequestBody List<Long> ids) {
        String operatorId = getUsername();
        List<PlatformTransferRecord> records = platformTransferService.batchCreateDistributorTransfer(ids, operatorId);
        return AjaxResult.success(records);
    }

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/distributor/transfer/{transferNo}")
    public AjaxResult distributorTransferDetail(@PathVariable String transferNo) {
        return AjaxResult.success(platformTransferService.selectByTransferNo(transferNo));
    }

    // ==================== 转账记录 ====================

    @PreAuthorize("@ss.hasPermi('mall:settlement:list')")
    @GetMapping("/transfer/list")
    public TableDataInfo transferList(PlatformTransferRecord query) {
        startPage();
        List<PlatformTransferRecord> list = platformTransferService.selectList(query);
        return getDataTable(list);
    }
}
