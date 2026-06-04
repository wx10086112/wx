package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.mapper.MerchantSettlementRecordMapper;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MerchantSettlementRecordServiceImpl implements IMerchantSettlementRecordService {

    private static final Logger log = LoggerFactory.getLogger(MerchantSettlementRecordServiceImpl.class);

    private static final String STATUS_WAITING_T1 = "WAITING_T1";
    private static final String STATUS_TRANSFERRING = "TRANSFERRING";
    private static final String STATUS_ARRIVED = "ARRIVED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REFUND_PROCESSING = "REFUND_PROCESSING";
    private static final String STATUS_REVERSED = "REVERSED";

    private static final BigDecimal MERCHANT_RATE = new BigDecimal("0.9");
    private static final BigDecimal PLATFORM_RATE = new BigDecimal("0.1");

    @Resource
    private MerchantSettlementRecordMapper settlementMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;

    @Override
    public MerchantSettlementRecord selectById(Long id) {
        return settlementMapper.selectById(id);
    }

    @Override
    public MerchantSettlementRecord selectByIdForUpdate(Long id) {
        return settlementMapper.selectByIdForUpdate(id);
    }

    @Override
    public MerchantSettlementRecord selectBySettlementNo(String settlementNo) {
        return settlementMapper.selectBySettlementNo(settlementNo);
    }

    @Override
    public MerchantSettlementRecord selectByOrderNo(String orderNo) {
        return settlementMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<MerchantSettlementRecord> selectList(MerchantSettlementRecord query) {
        return settlementMapper.selectList(query);
    }

    @Override
    public List<MerchantSettlementRecord> selectByMerchantIdAndStatuses(Long merchantId, List<String> statuses) {
        return settlementMapper.selectByMerchantIdAndStatuses(merchantId, statuses);
    }

    @Override
    public int insert(MerchantSettlementRecord record) {
        return settlementMapper.insert(record);
    }

    @Override
    public int updateById(MerchantSettlementRecord record) {
        return settlementMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchTransfer(List<Long> ids) {
        for (Long id : ids) {
            MerchantSettlementRecord record = settlementMapper.selectById(id);
            if (record != null && STATUS_WAITING_T1.equals(record.getStatus())) {
                record.setStatus(STATUS_TRANSFERRING);
                record.setTransferTime(new Date());
                settlementMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkArrived(List<Long> ids) {
        for (Long id : ids) {
            MerchantSettlementRecord record = settlementMapper.selectById(id);
            if (record != null && !STATUS_ARRIVED.equals(record.getStatus()) && !STATUS_CANCELLED.equals(record.getStatus())) {
                record.setStatus(STATUS_ARRIVED);
                record.setArriveTime(new Date());
                settlementMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long id, String failReason) {
        MerchantSettlementRecord record = settlementMapper.selectById(id);
        if (record != null) {
            record.setStatus(STATUS_FAILED);
            record.setFailReason(failReason);
            settlementMapper.updateById(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSettlementForOrder(String orderNo, Long merchantId, Long storeId, BigDecimal payAmount, String title) {
        // 幂等：同一 order_no 不重复生成
        MerchantSettlementRecord existing = settlementMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("订单 {} 已存在结算记录 {}, 跳过", orderNo, existing.getSettlementNo());
            return;
        }

        // 计算商家金额（元）= payAmount * 0.9，向下取整
        BigDecimal merchantAmount = payAmount.multiply(MERCHANT_RATE).setScale(2, RoundingMode.DOWN);
        BigDecimal platformFeeAmount = payAmount.subtract(merchantAmount);

        // 预计打款时间 = 明天 10:00 (T+1)
        Date expectedTransferTime = calcExpectedTransferTime();

        MerchantSettlementRecord record = new MerchantSettlementRecord();
        record.setSettlementNo(generateSettlementNo());
        record.setMerchantId(merchantId);
        record.setStoreId(storeId);
        record.setOrderNo(orderNo);
        record.setTitle(title);
        record.setOrderAmount(payAmount);
        record.setMerchantAmount(merchantAmount);
        record.setPlatformFeeAmount(platformFeeAmount);
        record.setStatus(STATUS_WAITING_T1);
        record.setApplyTime(new Date());
        record.setExpectedTransferTime(expectedTransferTime);

        settlementMapper.insert(record);
        log.info("生成结算记录: settlementNo={}, orderNo={}, merchantAmount={}, platformFee={}",
                record.getSettlementNo(), orderNo, merchantAmount, platformFeeAmount);
    }

    @Override
    public List<MerchantSettlementRecord> selectWaitingTransfer(int limit) {
        return settlementMapper.selectWaitingTransfer(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWaitingTransfer(int batchSize) {
        List<MerchantSettlementRecord> waitingList = settlementMapper.selectWaitingTransfer(batchSize);
        for (MerchantSettlementRecord record : waitingList) {
            try {
                // 微信打款能力暂未接入，先标记为 TRANSFERRING
                record.setStatus(STATUS_TRANSFERRING);
                record.setTransferTime(new Date());
                settlementMapper.updateById(record);
                log.info("结算记录 {} 进入打款流程 (TRANSFERRING)", record.getSettlementNo());
            } catch (Exception e) {
                log.error("处理结算记录 {} 失败: {}", record.getSettlementNo(), e.getMessage(), e);
                record.setStatus(STATUS_FAILED);
                record.setFailReason("系统异常: " + e.getMessage());
                settlementMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundReverse(String orderNo, String failReason) {
        MerchantSettlementRecord record = settlementMapper.selectByOrderNo(orderNo);
        if (record == null) {
            log.warn("退款逆向：订单 {} 无结算记录，跳过", orderNo);
            return;
        }

        String currentStatus = record.getStatus();

        // 场景1: WAITING_T1 → 取消
        if (STATUS_WAITING_T1.equals(currentStatus)) {
            record.setStatus(STATUS_CANCELLED);
            record.setFailReason(failReason);
            settlementMapper.updateById(record);
            log.info("退款逆向: 订单 {} 结算记录 {} 已取消 (WAITING_T1)", orderNo, record.getSettlementNo());
        }
        // 场景2: TRANSFERRING → REFUND_PROCESSING
        else if (STATUS_TRANSFERRING.equals(currentStatus)) {
            record.setStatus(STATUS_REFUND_PROCESSING);
            record.setFailReason(failReason);
            settlementMapper.updateById(record);
            log.info("退款逆向: 订单 {} 结算记录 {} 进入退款处理中 (TRANSFERRING)", orderNo, record.getSettlementNo());
        }
        // 场景3: ARRIVED → 生成负向记录
        else if (STATUS_ARRIVED.equals(currentStatus)) {
            MerchantSettlementRecord reverse = new MerchantSettlementRecord();
            reverse.setSettlementNo(generateSettlementNo());
            reverse.setMerchantId(record.getMerchantId());
            reverse.setStoreId(record.getStoreId());
            reverse.setOrderNo(record.getOrderNo());
            reverse.setTitle(record.getTitle() + "(退款)");
            reverse.setOrderAmount(record.getOrderAmount().negate());
            reverse.setMerchantAmount(record.getMerchantAmount().negate());
            reverse.setPlatformFeeAmount(record.getPlatformFeeAmount().negate());
            reverse.setStatus(STATUS_REVERSED);
            reverse.setApplyTime(new Date());
            reverse.setReverseRecordId(record.getId());
            settlementMapper.insert(reverse);
            log.info("退款逆向: 订单 {} 生成负向结算记录 {} (原记录 {} 已到账)",
                    orderNo, reverse.getSettlementNo(), record.getSettlementNo());
        }
    }

    @Override
    public BigDecimal sumMerchantAmountByStatus(Long merchantId, String status) {
        return settlementMapper.sumMerchantAmountByStatus(merchantId, status);
    }

    @Override
    public BigDecimal sumMerchantAmountToday(Long merchantId) {
        return settlementMapper.sumMerchantAmountToday(merchantId);
    }

    @Override
    public BigDecimal sumMerchantAmountThisMonth(Long merchantId) {
        return settlementMapper.sumMerchantAmountThisMonth(merchantId);
    }

    @Override
    public Integer countCompletedByMerchantId(Long merchantId) {
        return settlementMapper.countCompletedByMerchantId(merchantId);
    }

    private Date calcExpectedTransferTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String generateSettlementNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "STL" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
