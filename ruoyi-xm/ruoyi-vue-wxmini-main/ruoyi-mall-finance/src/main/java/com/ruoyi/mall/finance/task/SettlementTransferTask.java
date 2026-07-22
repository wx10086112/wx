package com.ruoyi.mall.finance.task;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;
import com.ruoyi.mall.finance.config.WechatTransferSafetyGuard;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IPlatformTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * T+1 自动打款定时任务
 * 扫描 WAITING_T1 且 expected_transfer_time <= now 的结算记录，自动发起转账
 */
@Component
public class SettlementTransferTask {

    private static final Logger log = LoggerFactory.getLogger(SettlementTransferTask.class);

    @Resource
    private IMerchantSettlementRecordService settlementService;
    @Resource
    private IDistributorSettlementRecordService distributorSettlementService;
    @Resource
    private IPlatformTransferService platformTransferService;
    @Resource
    private WechatTransferSafetyGuard transferSafetyGuard;
    @Value("${wx.pay.transfer-task-enabled:false}")
    private boolean transferTaskEnabled;

    /**
     * 每5分钟执行一次，扫描待打款的结算记录并自动发起转账
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processWaitingTransfer() {
        if (!transferTaskEnabled) {
            return;
        }
        transferSafetyGuard.ensureTransferAllowed();
        log.info("T+1 自动打款任务开始执行");
        try {
            List<MerchantSettlementRecord> waitingList = settlementService.selectWaitingTransfer(50);
            for (MerchantSettlementRecord record : waitingList) {
                try {
                    platformTransferService.createMerchantTransfer(record.getId(), "system-auto");
                    log.info("自动发起转账: settlementNo={}, merchantId={}", record.getSettlementNo(), record.getMerchantId());
                } catch (Exception e) {
                    log.error("自动发起转账失败: settlementNo={}, error={}", record.getSettlementNo(), e.getMessage(), e);
                }
            }
            List<DistributorSettlementRecord> distributorWaitingList = distributorSettlementService.selectWaitingTransfer(50);
            for (DistributorSettlementRecord record : distributorWaitingList) {
                try {
                    platformTransferService.createDistributorTransfer(record.getId(), "system-auto");
                    log.info("auto distributor transfer created: settlementNo={}, distributorId={}",
                            record.getSettlementNo(), record.getDistributorId());
                } catch (Exception e) {
                    log.error("auto distributor transfer failed: settlementNo={}, error={}",
                            record.getSettlementNo(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("T+1 自动打款任务异常: {}", e.getMessage(), e);
        }
        log.info("T+1 自动打款任务执行完成");
    }

    /**
     * 每10分钟扫描超时转账记录，主动查询微信转账状态
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void processTimeoutTransfers() {
        if (!transferTaskEnabled) {
            return;
        }
        log.info("超时转账状态同步任务开始执行");
        try {
            platformTransferService.processTimeoutTransfers(30, 50);
        } catch (Exception e) {
            log.error("超时转账状态同步任务异常: {}", e.getMessage(), e);
        }
        log.info("超时转账状态同步任务执行完成");
    }
}
