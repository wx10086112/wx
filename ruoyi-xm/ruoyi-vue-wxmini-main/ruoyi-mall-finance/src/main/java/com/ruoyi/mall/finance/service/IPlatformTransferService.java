package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.PlatformTransferRecord;

import java.util.List;

public interface IPlatformTransferService {

    PlatformTransferRecord selectById(Long id);

    PlatformTransferRecord selectByTransferNo(String transferNo);

    List<PlatformTransferRecord> selectList(PlatformTransferRecord query);

    /**
     * 为单条商家结算记录发起转账
     */
    PlatformTransferRecord createMerchantTransfer(Long settlementId, String operatorId);

    /**
     * 批量为商家结算记录发起转账
     */
    List<PlatformTransferRecord> batchCreateMerchantTransfer(List<Long> settlementIds, String operatorId);

    /**
     * 为单条分销商结算记录发起转账
     */
    PlatformTransferRecord createDistributorTransfer(Long settlementId, String operatorId);

    /**
     * 批量为分销商结算记录发起转账
     */
    List<PlatformTransferRecord> batchCreateDistributorTransfer(List<Long> settlementIds, String operatorId);

    /**
     * 处理微信转账回调
     */
    void handleTransferNotify(String notifyBody);

    /**
     * 查询微信转账状态并更新
     */
    void queryAndUpdateTransferStatus(String transferNo);

    /**
     * 扫描超时转账记录，主动查询微信状态
     */
    void processTimeoutTransfers(int timeoutMinutes, int batchSize);
}
