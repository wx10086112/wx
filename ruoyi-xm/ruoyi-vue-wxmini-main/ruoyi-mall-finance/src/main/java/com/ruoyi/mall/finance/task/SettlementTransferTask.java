package com.ruoyi.mall.finance.task;

import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IPlatformTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * T+1 自动打款定时任务
 * 扫描 WAITING_T1 且 expected_transfer_time <= now 的结算记录
 */
@Component
public class SettlementTransferTask {

    private static final Logger log = LoggerFactory.getLogger(SettlementTransferTask.class);

    @Resource
    private IMerchantSettlementRecordService settlementService;
    @Resource
    private IPlatformTransferService platformTransferService;

    /**
     * 每5分钟执行一次，扫描待打款的结算记录
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processWaitingTransfer() {
        log.info("T+1 自动打款任务开始执行");
        try {
            settlementService.processWaitingTransfer(50);
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
        log.info("超时转账状态同步任务开始执行");
        try {
            platformTransferService.processTimeoutTransfers(30, 50);
        } catch (Exception e) {
            log.error("超时转账状态同步任务异常: {}", e.getMessage(), e);
        }
        log.info("超时转账状态同步任务执行完成");
    }
}
