package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;
import com.ruoyi.mall.finance.mapper.DistributorSettlementRecordMapper;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
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
import java.util.UUID;

@Service
public class DistributorSettlementRecordServiceImpl implements IDistributorSettlementRecordService {

    private static final Logger log = LoggerFactory.getLogger(DistributorSettlementRecordServiceImpl.class);

    @Resource
    private DistributorSettlementRecordMapper distributorSettlementMapper;

    @Override
    public DistributorSettlementRecord selectById(Long id) {
        return distributorSettlementMapper.selectById(id);
    }

    @Override
    public DistributorSettlementRecord selectByIdForUpdate(Long id) {
        return distributorSettlementMapper.selectByIdForUpdate(id);
    }

    @Override
    public DistributorSettlementRecord selectBySettlementNo(String settlementNo) {
        return distributorSettlementMapper.selectBySettlementNo(settlementNo);
    }

    @Override
    public List<DistributorSettlementRecord> selectList(DistributorSettlementRecord query) {
        return distributorSettlementMapper.selectList(query);
    }

    @Override
    public int updateById(DistributorSettlementRecord record) {
        return distributorSettlementMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSettlementForOrder(String orderNo, Long merchantId, Long distributorId, BigDecimal commissionAmount, BigDecimal rate) {
        if (distributorId == null || commissionAmount == null || commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 幂等检查（通过订单号和分销商ID组合检查）
        DistributorSettlementRecord query = new DistributorSettlementRecord();
        query.setOrderNo(orderNo);
        query.setDistributorId(distributorId);
        List<DistributorSettlementRecord> existing = distributorSettlementMapper.selectList(query);
        if (existing != null && !existing.isEmpty()) {
            log.info("订单 {} 分销商 {} 结算记录已存在，跳过", orderNo, distributorId);
            return;
        }

        // 下周一为预计打款时间（每周一结算上周佣金）
        Date expectedTransferTime = nextMonday();

        DistributorSettlementRecord record = new DistributorSettlementRecord();
        record.setSettlementNo(generateSettlementNo());
        record.setDistributorId(distributorId);
        record.setMerchantId(merchantId);
        record.setOrderNo(orderNo);
        record.setAmount(commissionAmount);
        record.setRate(rate);
        record.setStatus("WAITING_SETTLEMENT");
        record.setExpectedTransferTime(expectedTransferTime);

        distributorSettlementMapper.insert(record);
        log.info("生成分销商结算记录: settlementNo={}, orderNo={}, distributor={}, amount={}",
                record.getSettlementNo(), orderNo, distributorId, commissionAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundReverse(String orderNo) {
        DistributorSettlementRecord query = new DistributorSettlementRecord();
        query.setOrderNo(orderNo);
        List<DistributorSettlementRecord> records = distributorSettlementMapper.selectList(query);
        for (DistributorSettlementRecord record : records) {
            if ("WAITING_SETTLEMENT".equals(record.getStatus())) {
                record.setStatus("CANCELLED");
                record.setFailReason("订单退款");
                distributorSettlementMapper.updateById(record);
            } else if ("TRANSFERRING".equals(record.getStatus())) {
                record.setStatus("REVERSED");
                record.setFailReason("订单退款-打款中");
                distributorSettlementMapper.updateById(record);
            } else if ("ARRIVED".equals(record.getStatus())) {
                // 生成负向记录
                DistributorSettlementRecord reverse = new DistributorSettlementRecord();
                reverse.setSettlementNo(generateSettlementNo());
                reverse.setDistributorId(record.getDistributorId());
                reverse.setMerchantId(record.getMerchantId());
                reverse.setOrderNo(record.getOrderNo());
                reverse.setAmount(record.getAmount().negate());
                reverse.setRate(record.getRate());
                reverse.setStatus("REVERSED");
                reverse.setReverseRecordId(record.getId());
                reverse.setExpectedTransferTime(nextMonday());
                distributorSettlementMapper.insert(reverse);
                log.info("生成负向分销商结算记录: {}", reverse.getSettlementNo());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkArrived(List<Long> ids) {
        for (Long id : ids) {
            DistributorSettlementRecord record = distributorSettlementMapper.selectById(id);
            if (record != null && !"ARRIVED".equals(record.getStatus()) && !"CANCELLED".equals(record.getStatus())) {
                record.setStatus("ARRIVED");
                record.setArriveTime(new Date());
                distributorSettlementMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long id, String failReason) {
        DistributorSettlementRecord record = distributorSettlementMapper.selectById(id);
        if (record != null) {
            record.setStatus("FAILED");
            record.setFailReason(failReason);
            distributorSettlementMapper.updateById(record);
        }
    }

    @Override
    public BigDecimal sumAmountByStatus(Long distributorId, String status) {
        return distributorSettlementMapper.sumAmountByStatus(distributorId, status);
    }

    @Override
    public BigDecimal sumAmountByDistributorId(Long distributorId) {
        return distributorSettlementMapper.sumAmountByDistributorId(distributorId);
    }

    private Date nextMonday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String generateSettlementNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "DST" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
