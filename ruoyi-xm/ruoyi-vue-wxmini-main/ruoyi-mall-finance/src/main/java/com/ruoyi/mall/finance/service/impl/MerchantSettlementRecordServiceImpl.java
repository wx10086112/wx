package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.mapper.MerchantSettlementRecordMapper;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Resource
    private MerchantSettlementRecordMapper settlementMapper;

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
        throw new UnsupportedOperationException("请调用 PlatformTransferService 发起微信转账，禁止仅修改结算状态");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkArrived(List<Long> ids) {
        throw new UnsupportedOperationException("请通过微信转账回调或状态同步更新到账状态，禁止手工标记到账");
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
    public void createSettlementForOrder(String orderNo, Long merchantId, Long storeId, BigDecimal orderAmount,
                                         BigDecimal merchantAmount, BigDecimal platformFeeAmount, String title) {
        MerchantSettlementRecord existing = settlementMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("订单 {} 已存在结算记录 {}, 跳过", orderNo, existing.getSettlementNo());
            return;
        }

        Date expectedTransferTime = calcExpectedTransferTime();

        MerchantSettlementRecord record = new MerchantSettlementRecord();
        record.setSettlementNo(generateSettlementNo());
        record.setMerchantId(merchantId);
        record.setStoreId(storeId);
        record.setOrderNo(orderNo);
        record.setTitle(title);
        record.setOrderAmount(safeAmount(orderAmount));
        record.setMerchantAmount(safeAmount(merchantAmount));
        record.setPlatformFeeAmount(safeAmount(platformFeeAmount));
        record.setStatus(STATUS_WAITING_T1);
        record.setApplyTime(new Date());
        record.setExpectedTransferTime(expectedTransferTime);

        settlementMapper.insert(record);
        log.info("生成结算记录: settlementNo={}, orderNo={}, orderAmount={}, merchantAmount={}, platformFee={}",
                record.getSettlementNo(), orderNo, record.getOrderAmount(),
                record.getMerchantAmount(), record.getPlatformFeeAmount());
    }

    @Override
    public List<MerchantSettlementRecord> selectWaitingTransfer(int limit) {
        return settlementMapper.selectWaitingTransfer(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWaitingTransfer(int batchSize) {
        throw new UnsupportedOperationException("请通过 SettlementTransferTask 调用 PlatformTransferService 发起微信转账");
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
        if (STATUS_WAITING_T1.equals(currentStatus)) {
            record.setStatus(STATUS_CANCELLED);
            record.setFailReason(failReason);
            settlementMapper.updateById(record);
            log.info("退款逆向：订单 {} 结算记录 {} 已取消", orderNo, record.getSettlementNo());
        } else if (STATUS_TRANSFERRING.equals(currentStatus)) {
            record.setStatus(STATUS_REFUND_PROCESSING);
            record.setFailReason(failReason);
            settlementMapper.updateById(record);
            log.info("退款逆向：订单 {} 结算记录 {} 进入退款处理中", orderNo, record.getSettlementNo());
        } else if (STATUS_ARRIVED.equals(currentStatus)) {
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
            log.info("退款逆向：订单 {} 生成负向结算记录 {}", orderNo, reverse.getSettlementNo());
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

    @Override
    public List<Map<String, Object>> selectDailyFlowSummary(Long merchantId, String startDate, String endDate) {
        return settlementMapper.selectDailyFlowSummary(merchantId, startDate, endDate);
    }

    @Override
    public List<MerchantSettlementRecord> selectDailyFlowDetails(Long merchantId, String startDate, String endDate, Integer limit) {
        return settlementMapper.selectDailyFlowDetails(merchantId, startDate, endDate, limit);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
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
