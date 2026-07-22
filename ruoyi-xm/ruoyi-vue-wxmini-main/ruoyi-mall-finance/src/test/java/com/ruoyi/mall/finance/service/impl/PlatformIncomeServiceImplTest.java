package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.mapper.PlatformIncomeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformIncomeServiceImplTest {

    @InjectMocks
    private PlatformIncomeServiceImpl service;
    @Mock
    private PlatformIncomeMapper platformIncomeMapper;

    @Test
    void recordsActualCommissionRateInsteadOfConstantFraction() {
        when(platformIncomeMapper.selectPlatformIncomeList(any(PlatformIncome.class))).thenReturn(Collections.emptyList());

        service.createIncome("ORDER-100", 10L, new BigDecimal("100.00"), new BigDecimal("9.94"));

        ArgumentCaptor<PlatformIncome> captor = ArgumentCaptor.forClass(PlatformIncome.class);
        verify(platformIncomeMapper).insertPlatformIncome(captor.capture());
        assertEquals(new BigDecimal("9.94"), captor.getValue().getCommissionRate());
    }
}
