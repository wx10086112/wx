package com.ruoyi.mall.finance.service.impl;

import com.github.binarywang.wxpay.bean.profitsharing.request.ProfitSharingV3Request;
import com.github.binarywang.wxpay.bean.profitsharing.request.ProfitSharingReceiverV3Request;
import com.github.binarywang.wxpay.bean.profitsharing.result.ProfitSharingV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.mapper.OrderProfitLedgerMapper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.pay.domain.PaymentRecord;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WechatProfitSharingServiceImplTest {

    @Test
    void usesServiceProviderRelationForServiceProviderReceiver() throws Exception {
        WechatProfitSharingServiceImpl service = new WechatProfitSharingServiceImpl();
        Object context = newContext("1111295663");
        OrderProfitLedger ledger = new OrderProfitLedger();
        ledger.setPlatformAmount(new BigDecimal("0.15"));
        ledger.setDistributorAmount(BigDecimal.ZERO);

        Method buildReceivers = WechatProfitSharingServiceImpl.class.getDeclaredMethod(
                "buildReceivers", OrderProfitLedger.class, context.getClass());
        buildReceivers.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ProfitSharingV3Request.Receiver> receivers =
                (List<ProfitSharingV3Request.Receiver>) buildReceivers.invoke(service, ledger, context);

        assertEquals(1, receivers.size());
        assertEquals("MERCHANT_ID", receivers.get(0).getType());
        assertEquals("1111295663", receivers.get(0).getAccount());
        assertEquals("SERVICE_PROVIDER", receivers.get(0).getRelationType());
        assertEquals("Platform Co., Ltd.", receivers.get(0).getName());
        assertEquals(15, receivers.get(0).getAmount());
    }

    @Test
    void submitsPartnerReceiverAndStoresWechatOrderId() throws Exception {
        OrderProfitLedgerMapper ledgerMapper = mock(OrderProfitLedgerMapper.class);
        WxPayService wxPayService = mock(WxPayService.class, RETURNS_DEEP_STUBS);
        Merchant merchant = enabledMerchant();
        PaymentRecord payment = paidPayment();
        OrderProfitLedger ledger = ledger("WAITING_SETTLEMENT");
        ProfitSharingV3Result result = new ProfitSharingV3Result();
        result.setState("FINISHED");
        result.setOrderId("WXPS-100");

        when(ledgerMapper.selectByOrderNo("ORDER-100")).thenReturn(ledger);
        when(ledgerMapper.claimProfitSharingAttempt(eq(100L), eq(5), any())).thenReturn(1);
        when(wxPayService.getConfig().getAppId()).thenReturn("wx-service-provider");
        when(wxPayService.getConfig().getMchId()).thenReturn("1111295663");
        when(wxPayService.getProfitSharingService().profitSharingV3(any(ProfitSharingV3Request.class)))
                .thenReturn(result);

        WechatProfitSharingServiceImpl service = service(ledgerMapper, wxPayService, merchant, payment);
        service.processOrderProfitSharing("ORDER-100");

        ArgumentCaptor<ProfitSharingReceiverV3Request> receiverCaptor =
                ArgumentCaptor.forClass(ProfitSharingReceiverV3Request.class);
        verify(wxPayService.getProfitSharingService()).addReceiverV3(receiverCaptor.capture());
        assertEquals("SERVICE_PROVIDER", receiverCaptor.getValue().getRelationType());
        assertEquals("1111295663", receiverCaptor.getValue().getAccount());
        assertEquals("Platform Co., Ltd.", receiverCaptor.getValue().getName());
        verify(ledgerMapper).updateProfitSharingRequest(100L, "PSORDER-100");
        verify(ledgerMapper).updateProfitSharingState(eq(100L), eq("WECHAT_PROFIT_SHARING_SUCCESS"),
                anyString(), eq("WXPS-100"), eq(null), eq(true));
    }

    @Test
    void queriesProcessingOrderWithoutSubmittingAnotherSplit() throws Exception {
        OrderProfitLedgerMapper ledgerMapper = mock(OrderProfitLedgerMapper.class);
        WxPayService wxPayService = mock(WxPayService.class, RETURNS_DEEP_STUBS);
        Merchant merchant = enabledMerchant();
        PaymentRecord payment = paidPayment();
        OrderProfitLedger ledger = ledger("WECHAT_PROFIT_SHARING_PROCESSING");
        ledger.setProfitSharingOutOrderNo("PSORDER-100");
        ProfitSharingV3Result result = new ProfitSharingV3Result();
        result.setState("FINISHED");
        result.setOrderId("WXPS-100");

        when(ledgerMapper.selectByOrderNo("ORDER-100")).thenReturn(ledger);
        when(wxPayService.getConfig().getAppId()).thenReturn("wx-service-provider");
        when(wxPayService.getConfig().getMchId()).thenReturn("1111295663");
        when(wxPayService.getProfitSharingService().profitSharingQueryV3(
                "PSORDER-100", "42000000000000000000000000000000", "1113814461"))
                .thenReturn(result);

        WechatProfitSharingServiceImpl service = service(ledgerMapper, wxPayService, merchant, payment);
        service.queryOrderProfitSharing("ORDER-100");

        verify(ledgerMapper).updateProfitSharingState(eq(100L), eq("WECHAT_PROFIT_SHARING_SUCCESS"),
                anyString(), eq("WXPS-100"), eq(null), eq(true));
        verify(wxPayService.getProfitSharingService()).profitSharingQueryV3(
                "PSORDER-100", "42000000000000000000000000000000", "1113814461");
    }

    private Object newContext(String platformReceiverMchId) throws Exception {
        Class<?> contextType = Class.forName(
                "com.ruoyi.mall.finance.service.impl.WechatProfitSharingServiceImpl$ProfitSharingContext");
        Constructor<?> constructor = contextType.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object context = constructor.newInstance();
        Field platformReceiverField = contextType.getDeclaredField("platformReceiverMchId");
        platformReceiverField.setAccessible(true);
        platformReceiverField.set(context, platformReceiverMchId);
        Field platformReceiverNameField = contextType.getDeclaredField("platformReceiverName");
        platformReceiverNameField.setAccessible(true);
        platformReceiverNameField.set(context, "Platform Co., Ltd.");
        return context;
    }

    private WechatProfitSharingServiceImpl service(OrderProfitLedgerMapper ledgerMapper,
                                                    WxPayService wxPayService,
                                                    Merchant merchant,
                                                    PaymentRecord payment) {
        WechatProfitSharingServiceImpl service = new WechatProfitSharingServiceImpl();
        IMerchantService merchantService = mock(IMerchantService.class);
        IPaymentRecordService paymentRecordService = mock(IPaymentRecordService.class);
        when(merchantService.selectMerchantById(1L)).thenReturn(merchant);
        when(paymentRecordService.selectByOrderNo("ORDER-100")).thenReturn(payment);
        ReflectionTestUtils.setField(service, "ledgerMapper", ledgerMapper);
        ReflectionTestUtils.setField(service, "wxPayService", wxPayService);
        ReflectionTestUtils.setField(service, "merchantService", merchantService);
        ReflectionTestUtils.setField(service, "paymentRecordService", paymentRecordService);
        ReflectionTestUtils.setField(service, "profitSharingEnabled", true);
        ReflectionTestUtils.setField(service, "autoSubmitEnabled", true);
        ReflectionTestUtils.setField(service, "addReceiverEnabled", true);
        ReflectionTestUtils.setField(service, "platformReceiverName", "Platform Co., Ltd.");
        ReflectionTestUtils.setField(service, "retryMaxAttempts", 5);
        ReflectionTestUtils.setField(service, "retryDelayMs", 300000L);
        return service;
    }

    private Merchant enabledMerchant() {
        Merchant merchant = new Merchant();
        merchant.setWxProfitSharingEnabled(1);
        merchant.setMerchantWxMchId("1113814461");
        return merchant;
    }

    private PaymentRecord paidPayment() {
        PaymentRecord payment = new PaymentRecord();
        payment.setPayStatus(1);
        payment.setTransactionId("42000000000000000000000000000000");
        payment.setSubMchId("1113814461");
        payment.setSubAppId("wx-sub-merchant");
        return payment;
    }

    private OrderProfitLedger ledger(String status) {
        OrderProfitLedger ledger = new OrderProfitLedger();
        ledger.setId(100L);
        ledger.setOrderNo("ORDER-100");
        ledger.setMerchantId(1L);
        ledger.setStatus(status);
        ledger.setPlatformAmount(new BigDecimal("0.15"));
        ledger.setDistributorAmount(BigDecimal.ZERO);
        return ledger;
    }
}
