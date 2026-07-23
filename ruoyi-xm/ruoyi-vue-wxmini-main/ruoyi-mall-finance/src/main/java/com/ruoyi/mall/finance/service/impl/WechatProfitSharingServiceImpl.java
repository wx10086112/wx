package com.ruoyi.mall.finance.service.impl;

import com.github.binarywang.wxpay.bean.profitsharing.request.ProfitSharingReceiverV3Request;
import com.github.binarywang.wxpay.bean.profitsharing.request.ProfitSharingUnfreezeV3Request;
import com.github.binarywang.wxpay.bean.profitsharing.request.ProfitSharingV3Request;
import com.github.binarywang.wxpay.bean.profitsharing.result.ProfitSharingUnfreezeV3Result;
import com.github.binarywang.wxpay.bean.profitsharing.result.ProfitSharingV3Result;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.mapper.OrderProfitLedgerMapper;
import com.ruoyi.mall.finance.service.IWechatProfitSharingService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.pay.domain.PaymentRecord;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WechatProfitSharingServiceImpl implements IWechatProfitSharingService {

    private static final Logger log = LoggerFactory.getLogger(WechatProfitSharingServiceImpl.class);

    private static final String RECEIVER_TYPE_MERCHANT_ID = "MERCHANT_ID";
    private static final String RELATION_TYPE_SERVICE_PROVIDER = "SERVICE_PROVIDER";
    private static final String RELATION_TYPE_DISTRIBUTOR = "DISTRIBUTOR";
    private static final String STATUS_PROCESSING = "WECHAT_PROFIT_SHARING_PROCESSING";
    private static final String STATUS_SUCCESS = "WECHAT_PROFIT_SHARING_SUCCESS";
    private static final String STATUS_FAILED = "WECHAT_PROFIT_SHARING_FAILED";
    private static final String STATUS_SKIPPED = "WECHAT_PROFIT_SHARING_SKIPPED";
    private static final String STATUS_WAITING = "WAITING_SETTLEMENT";

    @Value("${wx.pay.profit-sharing-enabled:true}")
    private boolean profitSharingEnabled;

    @Value("${wx.pay.profit-sharing-auto-submit-enabled:true}")
    private boolean autoSubmitEnabled;

    @Value("${wx.pay.profit-sharing-add-receiver-enabled:true}")
    private boolean addReceiverEnabled;

    @Value("${wx.pay.profit-sharing-platform-mch-id:}")
    private String platformReceiverMchId;
    @Value("${wx.pay.profit-sharing-platform-receiver-name:}")
    private String platformReceiverName;
    @Value("${wx.pay.profit-sharing-retry-max-attempts:5}")
    private int retryMaxAttempts;
    @Value("${wx.pay.profit-sharing-retry-delay-ms:300000}")
    private long retryDelayMs;

    @Resource
    private WxPayService wxPayService;
    @Resource
    private OrderProfitLedgerMapper ledgerMapper;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private IDistributorService distributorService;

    @Override
    public void processOrderProfitSharing(String orderNo) {
        if (!profitSharingEnabled || !autoSubmitEnabled) {
            return;
        }

        OrderProfitLedger ledger = ledgerMapper.selectByOrderNo(orderNo);
        if (!shouldProcess(ledger) || !claimAttempt(ledger)) {
            return;
        }

        try {
            ProfitSharingContext context = buildContext(ledger);
            List<ProfitSharingV3Request.Receiver> receivers = buildReceivers(ledger, context);
            if (receivers.isEmpty()) {
                markLedger(ledger, STATUS_SKIPPED, "no receiver amount", null, null, true);
                return;
            }

            String outOrderNo = firstNotBlank(ledger.getProfitSharingOutOrderNo(),
                    buildOutOrderNo(orderNo, "PS"));
            ledgerMapper.updateProfitSharingRequest(ledger.getId(), outOrderNo);

            if (addReceiverEnabled) {
                for (ProfitSharingV3Request.Receiver receiver : receivers) {
                    addReceiver(context, receiver);
                }
            }

            ProfitSharingV3Request request = new ProfitSharingV3Request();
            if (StringUtils.isNotBlank(context.spAppId)) {
                request.setAppid(context.spAppId);
            }
            request.setSubAppid(context.subAppId);
            request.setSubMchId(context.subMchId);
            request.setTransactionId(context.transactionId);
            request.setOutOrderNo(outOrderNo);
            request.setReceivers(receivers);
            request.setUnfreezeUnsplit(true);

            ProfitSharingV3Result result = wxPayService.getProfitSharingService().profitSharingV3(request);
            String state = result != null ? result.getState() : null;
            String status = isSuccessState(state) ? STATUS_SUCCESS : STATUS_PROCESSING;
            String remark = "wxProfitSharing outOrderNo=" + request.getOutOrderNo()
                    + ", orderId=" + (result != null ? StringUtils.defaultString(result.getOrderId()) : "")
                    + ", state=" + StringUtils.defaultString(state);
            markLedger(ledger, status, remark, result != null ? result.getOrderId() : null, null, true);
            log.info("wechat profit sharing submitted: orderNo={}, outOrderNo={}, state={}",
                    orderNo, request.getOutOrderNo(), state);
        } catch (ProfitSharingSkippedException e) {
            markLedger(ledger, STATUS_SKIPPED, e.getMessage(), null, null, true);
            log.info("wechat profit sharing skipped: orderNo={}, reason={}", orderNo, e.getMessage());
        } catch (Exception e) {
            String error = describeException(e);
            markLedger(ledger, STATUS_FAILED, truncate("wechat profit sharing failed: " + error),
                    null, nextRetryTime(), false);
            log.error("wechat profit sharing failed: orderNo={}, error={}", orderNo, error, e);
        }
    }

    @Override
    public void queryOrderProfitSharing(String orderNo) {
        if (!profitSharingEnabled) {
            return;
        }
        OrderProfitLedger ledger = ledgerMapper.selectByOrderNo(orderNo);
        if (ledger == null || !STATUS_PROCESSING.equals(ledger.getStatus())) {
            return;
        }

        String outOrderNo = firstNotBlank(ledger.getProfitSharingOutOrderNo(),
                buildOutOrderNo(orderNo, "PS"));
        try {
            ProfitSharingContext context = buildContext(ledger);
            ProfitSharingV3Result result = wxPayService.getProfitSharingService()
                    .profitSharingQueryV3(outOrderNo, context.transactionId, context.subMchId);
            String state = result != null ? result.getState() : null;
            String remark = "wxProfitSharing query outOrderNo=" + outOrderNo
                    + ", orderId=" + (result != null ? StringUtils.defaultString(result.getOrderId()) : "")
                    + ", state=" + StringUtils.defaultString(state);
            if (isSuccessState(state)) {
                markLedger(ledger, STATUS_SUCCESS, remark,
                        result != null ? result.getOrderId() : null, null, true);
            } else if (isFailedState(state)) {
                markLedger(ledger, STATUS_FAILED, remark,
                        result != null ? result.getOrderId() : null, nextRetryTime(), false);
            } else {
                log.info("wechat profit sharing remains processing: orderNo={}, outOrderNo={}, state={}",
                        orderNo, outOrderNo, state);
            }
        } catch (Exception e) {
            log.error("wechat profit sharing query failed: orderNo={}, error={}", orderNo, describeException(e), e);
        }
    }

    @Override
    public void finishOrderProfitSharing(String orderNo) {
        if (!profitSharingEnabled) {
            return;
        }

        OrderProfitLedger ledger = ledgerMapper.selectByOrderNo(orderNo);
        if (ledger == null) {
            return;
        }
        try {
            ProfitSharingContext context = buildContext(ledger);
            ProfitSharingUnfreezeV3Request request = new ProfitSharingUnfreezeV3Request();
            request.setSubMchId(context.subMchId);
            request.setTransactionId(context.transactionId);
            request.setOutOrderNo(buildOutOrderNo(orderNo, "PF"));
            request.setDescription("order profit sharing finish");
            ProfitSharingUnfreezeV3Result result = wxPayService.getProfitSharingService().profitSharingUnfreeze(request);
            markLedger(ledger, STATUS_SUCCESS, "wxProfitSharing finish outOrderNo=" + request.getOutOrderNo()
                    + ", orderId=" + (result != null ? StringUtils.defaultString(result.getOrderId()) : "")
                    + ", state=" + (result != null ? StringUtils.defaultString(result.getState()) : ""),
                    result != null ? result.getOrderId() : null, null, true);
        } catch (Exception e) {
            String error = describeException(e);
            markLedger(ledger, STATUS_FAILED, truncate("wechat profit sharing finish failed: " + error),
                    null, nextRetryTime(), false);
            log.error("wechat profit sharing finish failed: orderNo={}, error={}", orderNo, error, e);
        }
    }

    private boolean shouldProcess(OrderProfitLedger ledger) {
        if (ledger == null || StringUtils.isBlank(ledger.getOrderNo())) {
            return false;
        }
        String status = ledger.getStatus();
        return STATUS_WAITING.equals(status) || STATUS_FAILED.equals(status);
    }

    private boolean claimAttempt(OrderProfitLedger ledger) {
        if (ledger == null || ledger.getId() == null) {
            return false;
        }
        return ledgerMapper.claimProfitSharingAttempt(ledger.getId(), retryMaxAttempts, nextRetryTime()) > 0;
    }

    private ProfitSharingContext buildContext(OrderProfitLedger ledger) {
        Merchant merchant = merchantService.selectMerchantById(ledger.getMerchantId());
        if (merchant == null) {
            throw new IllegalStateException("merchant not found: " + ledger.getMerchantId());
        }
        if (!Integer.valueOf(1).equals(merchant.getWxProfitSharingEnabled())) {
            throw new ProfitSharingSkippedException("merchant wx profit sharing disabled");
        }

        PaymentRecord paymentRecord = paymentRecordService.selectByOrderNo(ledger.getOrderNo());
        if (paymentRecord == null) {
            throw new IllegalStateException("payment record not found");
        }
        if (paymentRecord.getPayStatus() == null || paymentRecord.getPayStatus() != 1) {
            throw new IllegalStateException("payment record is not paid");
        }
        if (StringUtils.isBlank(paymentRecord.getTransactionId())) {
            throw new IllegalStateException("wechat transaction_id is required");
        }

        ProfitSharingContext context = new ProfitSharingContext();
        context.spAppId = wxPayService.getConfig().getAppId();
        context.spMchId = wxPayService.getConfig().getMchId();
        context.subMchId = firstNotBlank(paymentRecord.getSubMchId(), merchant.getEffectiveMerchantWxMchId());
        context.subAppId = firstNotBlank(paymentRecord.getSubAppId(), merchant.getCMiniAppId());
        context.transactionId = paymentRecord.getTransactionId();
        context.platformReceiverMchId = firstNotBlank(merchant.getPlatformReceiverMchId(), platformReceiverMchId, context.spMchId);
        context.platformReceiverName = platformReceiverName;
        context.distributorReceiverMchId = merchant.getDistributorReceiverMchId();
        context.distributorReceiverName = resolveDistributorReceiverName(merchant);

        if (StringUtils.isBlank(context.spMchId)) {
            throw new IllegalStateException("wechat service provider pay config incomplete");
        }
        if (StringUtils.isBlank(context.subMchId)) {
            throw new IllegalStateException("sub_mchid is required");
        }
        if (StringUtils.isBlank(context.subAppId)) {
            throw new IllegalStateException("sub_appid is required");
        }
        return context;
    }

    private List<ProfitSharingV3Request.Receiver> buildReceivers(OrderProfitLedger ledger, ProfitSharingContext context) {
        List<ProfitSharingV3Request.Receiver> receivers = new ArrayList<>();
        Integer platformAmount = toFen(ledger.getPlatformAmount());
        if (platformAmount != null && platformAmount > 0) {
            if (StringUtils.isBlank(context.platformReceiverMchId)) {
                throw new IllegalStateException("platform receiver mch_id is required");
            }
            if (StringUtils.isBlank(context.platformReceiverName)) {
                throw new IllegalStateException("platform receiver merchant name is required");
            }
            receivers.add(buildReceiver(context.platformReceiverMchId, context.platformReceiverName, platformAmount,
                    RELATION_TYPE_SERVICE_PROVIDER, "platform share"));
        }

        Integer distributorAmount = toFen(ledger.getDistributorAmount());
        if (distributorAmount != null && distributorAmount > 0) {
            if (StringUtils.isBlank(context.distributorReceiverMchId)) {
                throw new IllegalStateException("distributor receiver mch_id is required");
            }
            if (StringUtils.isBlank(context.distributorReceiverName)) {
                throw new IllegalStateException("distributor receiver merchant name is required");
            }
            receivers.add(buildReceiver(context.distributorReceiverMchId, context.distributorReceiverName, distributorAmount,
                    RELATION_TYPE_DISTRIBUTOR, "distributor share"));
        }
        return receivers;
    }

    private ProfitSharingV3Request.Receiver buildReceiver(String account, String name, Integer amount,
                                                          String relationType, String description) {
        ProfitSharingV3Request.Receiver receiver = new ProfitSharingV3Request.Receiver();
        receiver.setType(RECEIVER_TYPE_MERCHANT_ID);
        receiver.setAccount(account);
        receiver.setName(name);
        receiver.setAmount(amount);
        receiver.setRelationType(relationType);
        receiver.setDescription(description);
        return receiver;
    }

    private void addReceiver(ProfitSharingContext context, ProfitSharingV3Request.Receiver receiver) throws WxPayException {
        ProfitSharingReceiverV3Request request = new ProfitSharingReceiverV3Request();
        if (StringUtils.isNotBlank(context.spAppId)) {
            request.setAppid(context.spAppId);
        }
        request.setSubAppid(context.subAppId);
        request.setSubMchId(context.subMchId);
        request.setType(receiver.getType());
        request.setAccount(receiver.getAccount());
        request.setName(receiver.getName());
        request.setRelationType(receiver.getRelationType());
        try {
            wxPayService.getProfitSharingService().addReceiverV3(request);
        } catch (WxPayException e) {
            if (isReceiverAlreadyExists(e)) {
                log.info("wechat profit sharing receiver already exists: subMchId={}, account={}",
                        context.subMchId, receiver.getAccount());
                return;
            }
            throw e;
        }
    }

    private boolean isReceiverAlreadyExists(WxPayException e) {
        String message = StringUtils.defaultString(e.getErrCode()) + " "
                + StringUtils.defaultString(e.getErrCodeDes()) + " "
                + StringUtils.defaultString(e.getMessage());
        return message.contains("RECEIVER_ALREADY_EXISTS")
                || message.contains("already exists")
                || message.contains("\u5df2\u5b58\u5728")
                || message.contains("\u91cd\u590d");
    }

    private Integer toFen(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.DOWN).intValueExact();
    }

    private boolean isSuccessState(String state) {
        return "FINISHED".equals(state) || "SUCCESS".equals(state);
    }

    private boolean isFailedState(String state) {
        return "CLOSED".equals(state) || "FAILED".equals(state);
    }

    private String buildOutOrderNo(String orderNo, String prefix) {
        String cleanOrderNo = orderNo.replaceAll("[^A-Za-z0-9_-]", "");
        String value = prefix + cleanOrderNo;
        return value.length() <= 64 ? value : value.substring(0, 64);
    }

    private void markLedger(OrderProfitLedger ledger, String status, String remark,
                            String orderId, Date nextRetryTime, boolean clearNextRetry) {
        if (ledger == null || ledger.getId() == null) {
            return;
        }
        ledger.setStatus(status);
        ledger.setRemark(truncate(remark));
        ledgerMapper.updateProfitSharingState(ledger.getId(), status, ledger.getRemark(), orderId,
                nextRetryTime, clearNextRetry);
    }

    private Date nextRetryTime() {
        return new Date(System.currentTimeMillis() + Math.max(1L, retryDelayMs));
    }

    private String describeException(Exception e) {
        if (e instanceof WxPayException) {
            WxPayException wxPayException = (WxPayException) e;
            String code = StringUtils.defaultString(wxPayException.getErrCode());
            String description = StringUtils.defaultString(wxPayException.getErrCodeDes());
            String message = StringUtils.defaultString(wxPayException.getMessage());
            return "code=" + code + ", description=" + description + ", message=" + message;
        }
        return StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String resolveDistributorReceiverName(Merchant merchant) {
        if (merchant == null || merchant.getDistributorId() == null) {
            return null;
        }
        Distributor distributor = distributorService.selectDistributorById(merchant.getDistributorId());
        return distributor != null ? StringUtils.trimToNull(distributor.getName()) : null;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 500) {
            return value;
        }
        return value.substring(0, 500);
    }

    private static class ProfitSharingContext {
        private String spAppId;
        private String spMchId;
        private String subMchId;
        private String subAppId;
        private String transactionId;
        private String platformReceiverMchId;
        private String platformReceiverName;
        private String distributorReceiverMchId;
        private String distributorReceiverName;
    }

    private static class ProfitSharingSkippedException extends RuntimeException {
        private ProfitSharingSkippedException(String message) {
            super(message);
        }
    }
}
