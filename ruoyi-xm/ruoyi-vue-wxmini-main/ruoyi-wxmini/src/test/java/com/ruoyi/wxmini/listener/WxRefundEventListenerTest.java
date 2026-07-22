package com.ruoyi.wxmini.listener;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerRefundV3Request;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WxRefundEventListenerTest {

    @Test
    void schedulesRetryWhenWechatApiThrowsTransientError() throws Exception {
        WxRefundEventListener listener = new WxRefundEventListener();
        WxPayService wxPayService = mock(WxPayService.class);
        RefundRecordMapper refundRecordMapper = mock(RefundRecordMapper.class);
        MallOrderMapper mallOrderMapper = mock(MallOrderMapper.class);
        IMerchantService merchantService = mock(IMerchantService.class);

        ReflectionTestUtils.setField(listener, "wxPayService", wxPayService);
        ReflectionTestUtils.setField(listener, "refundRecordMapper", refundRecordMapper);
        ReflectionTestUtils.setField(listener, "mallOrderMapper", mallOrderMapper);
        ReflectionTestUtils.setField(listener, "merchantService", merchantService);
        ReflectionTestUtils.setField(listener, "paymentRecordService", mock(IPaymentRecordService.class));
        ReflectionTestUtils.setField(listener, "applicationContext", mock(ApplicationContext.class));
        ReflectionTestUtils.setField(listener, "refundNotifyUrl", "https://pay.lingdian.site/refund-notify");
        ReflectionTestUtils.setField(listener, "refundRetryMaxAttempts", 6);
        ReflectionTestUtils.setField(listener, "refundRetryBaseDelayMs", 1000L);
        ReflectionTestUtils.setField(listener, "refundRetryMaxDelayMs", 10000L);
        ReflectionTestUtils.setField(listener, "refundRetryAttemptLeaseMs", 10000L);

        RefundRecord refund = new RefundRecord();
        refund.setId(1L);
        refund.setOrderNo("ORDER-100");
        refund.setRefundNo("REFUND-100");
        refund.setStatus(RefundRecord.STATUS_APPROVED);
        refund.setRefundAmount(new BigDecimal("50.00"));
        when(refundRecordMapper.selectRefundRecordById(1L)).thenReturn(refund);
        when(refundRecordMapper.claimApprovedRefundAttempt(org.mockito.ArgumentMatchers.eq(1L), any(Date.class)))
                .thenReturn(1);

        MallOrder order = new MallOrder();
        order.setMerchantId(10L);
        order.setPayAmount(new BigDecimal("50.00"));
        when(mallOrderMapper.selectMallOrderByOrderNo("ORDER-100")).thenReturn(order);

        Merchant merchant = mock(Merchant.class);
        when(merchant.getEffectiveMerchantWxMchId()).thenReturn("1900000109");
        when(merchantService.selectMerchantById(10L)).thenReturn(merchant);
        when(wxPayService.refundV3(any(WxPayPartnerRefundV3Request.class)))
                .thenThrow(new RuntimeException("temporary network failure"));

        listener.onRefundApproved(new RefundApprovedEvent(this, "ORDER-100", 1L, "tester"));

        verify(refundRecordMapper).scheduleRefundRetry(
                org.mockito.ArgumentMatchers.eq(1L), any(Date.class), anyString(), anyInt());
        verify(refundRecordMapper, never()).markRefundAbnormalWithReason(anyLong(), anyString());
        verify(refundRecordMapper, never()).updateRefundNoForApproved(anyLong(), anyString());
    }

    @Test
    void marksRefundAbnormalWhenRetryLimitIsReached() throws Exception {
        WxRefundEventListener listener = new WxRefundEventListener();
        WxPayService wxPayService = mock(WxPayService.class);
        RefundRecordMapper refundRecordMapper = mock(RefundRecordMapper.class);
        MallOrderMapper mallOrderMapper = mock(MallOrderMapper.class);
        IMerchantService merchantService = mock(IMerchantService.class);

        ReflectionTestUtils.setField(listener, "wxPayService", wxPayService);
        ReflectionTestUtils.setField(listener, "refundRecordMapper", refundRecordMapper);
        ReflectionTestUtils.setField(listener, "mallOrderMapper", mallOrderMapper);
        ReflectionTestUtils.setField(listener, "merchantService", merchantService);
        ReflectionTestUtils.setField(listener, "paymentRecordService", mock(IPaymentRecordService.class));
        ReflectionTestUtils.setField(listener, "applicationContext", mock(ApplicationContext.class));
        ReflectionTestUtils.setField(listener, "refundNotifyUrl", "https://pay.lingdian.site/refund-notify");
        ReflectionTestUtils.setField(listener, "refundRetryMaxAttempts", 2);
        ReflectionTestUtils.setField(listener, "refundRetryBaseDelayMs", 1000L);
        ReflectionTestUtils.setField(listener, "refundRetryMaxDelayMs", 10000L);
        ReflectionTestUtils.setField(listener, "refundRetryAttemptLeaseMs", 10000L);

        RefundRecord refund = new RefundRecord();
        refund.setId(2L);
        refund.setOrderNo("ORDER-200");
        refund.setRefundNo("REFUND-200");
        refund.setStatus(RefundRecord.STATUS_APPROVED);
        refund.setRetryCount(1);
        refund.setRefundAmount(new BigDecimal("50.00"));
        when(refundRecordMapper.selectRefundRecordById(2L)).thenReturn(refund);
        when(refundRecordMapper.claimApprovedRefundAttempt(org.mockito.ArgumentMatchers.eq(2L), any(Date.class)))
                .thenReturn(1);

        MallOrder order = new MallOrder();
        order.setMerchantId(10L);
        order.setPayAmount(new BigDecimal("50.00"));
        when(mallOrderMapper.selectMallOrderByOrderNo("ORDER-200")).thenReturn(order);

        Merchant merchant = mock(Merchant.class);
        when(merchant.getEffectiveMerchantWxMchId()).thenReturn("1900000109");
        when(merchantService.selectMerchantById(10L)).thenReturn(merchant);
        when(wxPayService.refundV3(any(WxPayPartnerRefundV3Request.class)))
                .thenThrow(new RuntimeException("temporary network failure"));

        listener.onRefundApproved(new RefundApprovedEvent(this, "ORDER-200", 2L, "tester"));

        verify(refundRecordMapper).scheduleRefundRetry(
                org.mockito.ArgumentMatchers.eq(2L), org.mockito.ArgumentMatchers.isNull(), anyString(),
                org.mockito.ArgumentMatchers.eq(2));
    }

    @Test
    void marksRefundAbnormalWhenOriginalOrderIsMissing() {
        WxRefundEventListener listener = new WxRefundEventListener();
        RefundRecordMapper refundRecordMapper = mock(RefundRecordMapper.class);
        MallOrderMapper mallOrderMapper = mock(MallOrderMapper.class);

        ReflectionTestUtils.setField(listener, "wxPayService", mock(WxPayService.class));
        ReflectionTestUtils.setField(listener, "refundRecordMapper", refundRecordMapper);
        ReflectionTestUtils.setField(listener, "mallOrderMapper", mallOrderMapper);
        ReflectionTestUtils.setField(listener, "merchantService", mock(IMerchantService.class));
        ReflectionTestUtils.setField(listener, "paymentRecordService", mock(IPaymentRecordService.class));
        ReflectionTestUtils.setField(listener, "applicationContext", mock(ApplicationContext.class));

        RefundRecord refund = new RefundRecord();
        refund.setId(3L);
        refund.setOrderNo("ORDER-MISSING");
        refund.setStatus(RefundRecord.STATUS_APPROVED);
        when(refundRecordMapper.selectRefundRecordById(3L)).thenReturn(refund);
        when(refundRecordMapper.claimApprovedRefundAttempt(org.mockito.ArgumentMatchers.eq(3L), any(Date.class)))
                .thenReturn(1);
        when(mallOrderMapper.selectMallOrderByOrderNo("ORDER-MISSING")).thenReturn(null);

        listener.onRefundApproved(new RefundApprovedEvent(this, "ORDER-MISSING", 3L, "tester"));

        verify(refundRecordMapper).markRefundAbnormalWithReason(
                org.mockito.ArgumentMatchers.eq(3L), anyString());
    }

    @Test
    void skipsWechatRefundWhenAnotherWorkerOwnsTheAttemptLease() throws Exception {
        WxRefundEventListener listener = new WxRefundEventListener();
        WxPayService wxPayService = mock(WxPayService.class);
        RefundRecordMapper refundRecordMapper = mock(RefundRecordMapper.class);

        ReflectionTestUtils.setField(listener, "wxPayService", wxPayService);
        ReflectionTestUtils.setField(listener, "refundRecordMapper", refundRecordMapper);
        ReflectionTestUtils.setField(listener, "mallOrderMapper", mock(MallOrderMapper.class));
        ReflectionTestUtils.setField(listener, "merchantService", mock(IMerchantService.class));
        ReflectionTestUtils.setField(listener, "paymentRecordService", mock(IPaymentRecordService.class));
        ReflectionTestUtils.setField(listener, "applicationContext", mock(ApplicationContext.class));
        ReflectionTestUtils.setField(listener, "refundRetryAttemptLeaseMs", 10000L);

        RefundRecord refund = new RefundRecord();
        refund.setId(4L);
        refund.setOrderNo("ORDER-LEASED");
        refund.setStatus(RefundRecord.STATUS_APPROVED);
        when(refundRecordMapper.selectRefundRecordById(4L)).thenReturn(refund);
        when(refundRecordMapper.claimApprovedRefundAttempt(org.mockito.ArgumentMatchers.eq(4L), any(Date.class)))
                .thenReturn(0);

        listener.onRefundApproved(new RefundApprovedEvent(this, "ORDER-LEASED", 4L, "tester"));

        verify(wxPayService, never()).refundV3(any(WxPayPartnerRefundV3Request.class));
    }
}
