package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.mapper.OrderProfitLedgerMapper;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
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

    @Value("${mall.split-rate.merchant-with-distributor:85}")
    private int merchantRateWithDist;
    @Value("${mall.split-rate.platform-with-distributor:10}")
    private int platformRateWithDist;
    @Value("${mall.split-rate.distributor:5}")
    private int distributorRateVal;

    @Value("${mall.split-rate.merchant-no-distributor:90}")
    private int merchantRateNoDist;
    @Value("${mall.split-rate.platform-no-distributor:10}")
    private int platformRateNoDist;
    @Value("${mall.split-rate.wechat-fee-rate:0.6}")
    private BigDecimal wechatFeeRate;
    @Value("${mall.split-rate.use-net-after-wechat-fee:true}")
    private boolean useNetAfterWechatFee;
    @Value("${mall.split-rate.merchant-override-enabled:false}")
    private boolean merchantOverrideEnabled;

    @Resource
    private OrderProfitLedgerMapper ledgerMapper;
    @Resource
    private IMerchantService merchantService;

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
    public List<OrderProfitLedger> selectProfitSharingRetryCandidates(int limit, int maxAttempts) {
        return ledgerMapper.selectProfitSharingRetryCandidates(limit, maxAttempts);
    }

    @Override
    public List<OrderProfitLedger> selectProcessingProfitSharing(int limit) {
        return ledgerMapper.selectProcessingProfitSharing(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLedger(String orderNo, Long merchantId, Long distributorId, BigDecimal payAmount) {
        OrderProfitLedger existing = ledgerMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("order profit ledger already exists, skip: orderNo={}", orderNo);
            return;
        }

        BigDecimal orderAmount = safeAmount(payAmount);
        BigDecimal wechatFeeAmount = calcWechatFee(orderAmount);
        BigDecimal splitBaseAmount = orderAmount.subtract(wechatFeeAmount);
        if (splitBaseAmount.compareTo(BigDecimal.ZERO) < 0) {
            splitBaseAmount = BigDecimal.ZERO;
        }

        RateConfig rates = resolveRates(merchantId, distributorId);
        BigDecimal merchantAmount = splitBaseAmount.multiply(rates.merchantRate).divide(HUNDRED, 2, RoundingMode.DOWN);
        BigDecimal distributorAmount = splitBaseAmount.multiply(rates.distributorRate).divide(HUNDRED, 2, RoundingMode.DOWN);
        BigDecimal platformAmount = splitBaseAmount.subtract(merchantAmount).subtract(distributorAmount);

        OrderProfitLedger ledger = new OrderProfitLedger();
        ledger.setOrderNo(orderNo);
        ledger.setMerchantId(merchantId);
        ledger.setDistributorId(distributorId);
        ledger.setPayAmount(orderAmount);
        ledger.setMerchantAmount(merchantAmount);
        ledger.setPlatformAmount(platformAmount);
        ledger.setDistributorAmount(distributorAmount);
        ledger.setMerchantRate(rates.merchantRate);
        ledger.setPlatformRate(rates.platformRate);
        ledger.setDistributorRate(rates.distributorRate);
        ledger.setStatus("WAITING_SETTLEMENT");
        ledger.setFinishTime(new Date());
        ledgerMapper.insert(ledger);
        log.info("created order profit ledger: orderNo={}, orderAmount={}, wechatFee={}, splitBase={}, merchant={}, platform={}, distributor={}",
                orderNo, orderAmount, wechatFeeAmount, splitBaseAmount,
                ledger.getMerchantAmount(), ledger.getPlatformAmount(), ledger.getDistributorAmount());
    }

    private RateConfig resolveRates(Long merchantId, Long distributorId) {
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchantOverrideEnabled && merchant != null && hasMerchantRates(merchant)) {
            BigDecimal merchantRate = merchant.getMerchantShareRate();
            BigDecimal platformRate = merchant.getPlatformShareRate();
            BigDecimal distributorRate = merchant.getDistributorShareRate();
            if (distributorId == null) {
                if (positive(distributorRate)) {
                    throw new IllegalStateException("direct platform merchant distributor share rate must be 0");
                }
                distributorRate = BigDecimal.ZERO;
            }
            validateRateSum(merchantRate, platformRate, distributorRate);
            return new RateConfig(merchantRate, platformRate, distributorRate);
        }
        if (distributorId != null) {
            return new RateConfig(new BigDecimal(merchantRateWithDist),
                    new BigDecimal(platformRateWithDist), new BigDecimal(distributorRateVal));
        }
        return new RateConfig(new BigDecimal(merchantRateNoDist),
                new BigDecimal(platformRateNoDist), BigDecimal.ZERO);
    }

    private BigDecimal calcWechatFee(BigDecimal orderAmount) {
        if (!useNetAfterWechatFee || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        }
        BigDecimal rate = wechatFeeRate != null ? wechatFeeRate : BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        }
        return orderAmount.multiply(rate).divide(HUNDRED, 2, RoundingMode.UP);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.DOWN);
    }

    private boolean hasMerchantRates(Merchant merchant) {
        return merchant.getMerchantShareRate() != null
                && merchant.getPlatformShareRate() != null
                && merchant.getDistributorShareRate() != null;
    }

    private void validateRateSum(BigDecimal merchantRate, BigDecimal platformRate, BigDecimal distributorRate) {
        BigDecimal sum = merchantRate.add(platformRate).add(distributorRate);
        if (sum.compareTo(HUNDRED) != 0) {
            throw new IllegalStateException("merchant/platform/distributor share rates must sum to 100");
        }
    }

    private boolean positive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static class RateConfig {
        private final BigDecimal merchantRate;
        private final BigDecimal platformRate;
        private final BigDecimal distributorRate;

        private RateConfig(BigDecimal merchantRate, BigDecimal platformRate, BigDecimal distributorRate) {
            this.merchantRate = merchantRate;
            this.platformRate = platformRate;
            this.distributorRate = distributorRate;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundReverse(String orderNo) {
        OrderProfitLedger ledger = ledgerMapper.selectByOrderNo(orderNo);
        if (ledger == null) {
            return;
        }
        ledger.setStatus("REFUND_REVERSED");
        ledgerMapper.updateById(ledger);
        log.info("order profit ledger reversed by refund: orderNo={}", orderNo);
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
