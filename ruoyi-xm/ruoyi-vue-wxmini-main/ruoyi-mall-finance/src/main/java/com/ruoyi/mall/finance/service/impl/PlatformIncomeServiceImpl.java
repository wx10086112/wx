package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.mapper.PlatformIncomeMapper;
import com.ruoyi.mall.finance.service.IPlatformIncomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PlatformIncomeServiceImpl implements IPlatformIncomeService {

    private static final Logger log = LoggerFactory.getLogger(PlatformIncomeServiceImpl.class);

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.10");

    @Resource
    private PlatformIncomeMapper platformIncomeMapper;

    @Override
    public PlatformIncome selectById(Long id) {
        return platformIncomeMapper.selectPlatformIncomeById(id);
    }

    @Override
    public List<PlatformIncome> selectList(PlatformIncome query) {
        return platformIncomeMapper.selectPlatformIncomeList(query);
    }

    @Override
    public void createIncome(String orderNo, Long merchantId, BigDecimal orderAmount, BigDecimal commission) {
        // 幂等：同一订单不重复创建
        PlatformIncome query = new PlatformIncome();
        query.setOrderNo(orderNo);
        List<PlatformIncome> existing = platformIncomeMapper.selectPlatformIncomeList(query);
        if (existing != null && !existing.isEmpty()) {
            log.info("订单 {} 已存在平台收入记录，跳过", orderNo);
            return;
        }

        PlatformIncome income = new PlatformIncome();
        income.setMerchantId(merchantId);
        income.setOrderNo(orderNo);
        income.setOrderAmount(orderAmount);
        income.setCommissionRate(DEFAULT_COMMISSION_RATE);
        income.setCommission(commission.setScale(2, RoundingMode.DOWN));

        platformIncomeMapper.insertPlatformIncome(income);
        log.info("创建平台收入记录: orderNo={}, merchantId={}, commission={}", orderNo, merchantId, commission);
    }

    @Override
    public BigDecimal sumTotalCommission() {
        return platformIncomeMapper.sumTotalCommission();
    }
}
