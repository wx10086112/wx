package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.mapper.OrderProfitLedgerMapper;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class OrderProfitLedgerServiceImpl implements IOrderProfitLedgerService {

    private static final Logger log = LoggerFactory.getLogger(OrderProfitLedgerServiceImpl.class);

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** 有分销商时的比例（可从 application.yml 配置） */
    @Value("${mall.split-rate.merchant-with-distributor:85}")
    private int merchantRateWithDist;
    @Value("${mall.split-rate.platform-with-distributor:10}")
    private int platformRateWithDist;
    @Value("${mall.split-rate.distributor:5}")
    private int distributorRateVal;

    /** 无分销商时的比例 */
    @Value("${mall.split-rate.merchant-no-distributor:90}")
    private int merchantRateNoDist;
    @Value("${mall.split-rate.platform-no-distributor:10}")
    private int platformRateNoDist;

    @Resource
    private OrderProfitLedgerMapper ledgerMapper;

    @Override
    public OrderProfitLedger selectById(Long id) {
        return ledgerMapper.selectById(id);
    }

    @Override
    public OrderProfitLedger selectByOrderNo(String orderNo) {
        return ledgerMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<OrderProfitLedger> selectList(OrderProfitLedger query) {
        return ledgerMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLedger(String orderNo, Long merchantId, Long distributorId, BigDecimal payAmount) {
        // 幂等
        OrderProfitLedger existing = ledgerMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("订单 {} 分账流水已存在，跳过", orderNo);
            return;
        }

        OrderProfitLedger ledger = new OrderProfitLedger();
        ledger.setOrderNo(orderNo);
        ledger.setMerchantId(merchantId);
        ledger.setDistributorId(distributorId);
        ledger.setPayAmount(payAmount);

        if (distributorId != null) {
            // 有分销商：按配置比例
            BigDecimal mRate = new BigDecimal(merchantRateWithDist);
            BigDecimal pRate = new BigDecimal(platformRateWithDist);
            BigDecimal dRate = new BigDecimal(distributorRateVal);
            BigDecimal merchantAmt = payAmount.multiply(mRate).divide(HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal platformAmt = payAmount.multiply(pRate).divide(HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal distributorAmt = payAmount.subtract(merchantAmt).subtract(platformAmt);

            ledger.setMerchantAmount(merchantAmt);
            ledger.setPlatformAmount(platformAmt);
            ledger.setDistributorAmount(distributorAmt);
            ledger.setMerchantRate(mRate);
            ledger.setPlatformRate(pRate);
            ledger.setDistributorRate(dRate);
        } else {
            // 无分销商：按配置比例
            BigDecimal mRate = new BigDecimal(merchantRateNoDist);
            BigDecimal pRate = new BigDecimal(platformRateNoDist);
            BigDecimal merchantAmt = payAmount.multiply(mRate).divide(HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal platformAmt = payAmount.subtract(merchantAmt);

            ledger.setMerchantAmount(merchantAmt);
            ledger.setPlatformAmount(platformAmt);
            ledger.setDistributorAmount(BigDecimal.ZERO);
            ledger.setMerchantRate(mRate);
            ledger.setPlatformRate(pRate);
            ledger.setDistributorRate(BigDecimal.ZERO);
        }

        ledger.setStatus("WAITING_SETTLEMENT");
        ledger.setFinishTime(new Date());
        ledgerMapper.insert(ledger);
        log.info("生成分账流水: orderNo={}, merchant={}, platform={}, distributor={}",
                orderNo, ledger.getMerchantAmount(), ledger.getPlatformAmount(), ledger.getDistributorAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundReverse(String orderNo) {
        OrderProfitLedger ledger = ledgerMapper.selectByOrderNo(orderNo);
        if (ledger == null) return;
        ledger.setStatus("REFUND_REVERSED");
        ledgerMapper.updateById(ledger);
        log.info("分账流水 {} 已标记为退款冲回", orderNo);
    }

    @Override
    public BigDecimal sumMerchantAmountByMerchantId(Long merchantId) {
        return ledgerMapper.sumMerchantAmountByMerchantId(merchantId);
    }

    @Override
    public BigDecimal sumPlatformAmount() {
        return ledgerMapper.sumPlatformAmount();
    }

    @Override
    public BigDecimal sumDistributorAmountByDistributorId(Long distributorId) {
        return ledgerMapper.sumDistributorAmountByDistributorId(distributorId);
    }

    @Override
    public Integer countByMerchantId(Long merchantId) {
        return ledgerMapper.countByMerchantId(merchantId);
    }

    @Override
    public List<OrderProfitLedger> selectByDistributorId(Long distributorId) {
        return ledgerMapper.selectByDistributorId(distributorId);
    }
}
