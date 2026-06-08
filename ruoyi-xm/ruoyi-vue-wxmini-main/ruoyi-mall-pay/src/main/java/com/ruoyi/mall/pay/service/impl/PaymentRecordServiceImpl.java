package com.ruoyi.mall.pay.service.impl;

import com.ruoyi.mall.pay.domain.PaymentRecord;
import com.ruoyi.mall.pay.mapper.PaymentRecordMapper;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class PaymentRecordServiceImpl implements IPaymentRecordService {

    private static final Logger log = LoggerFactory.getLogger(PaymentRecordServiceImpl.class);

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CLOSED = 2;
    public static final int STATUS_REFUNDED = 3;

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public PaymentRecord selectById(Long id) {
        return paymentRecordMapper.selectById(id);
    }

    @Override
    public PaymentRecord selectByOrderNo(String orderNo) {
        return paymentRecordMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PaymentRecord selectByOutTradeNo(String outTradeNo) {
        return paymentRecordMapper.selectByOutTradeNo(outTradeNo);
    }

    @Override
    public List<PaymentRecord> selectList(PaymentRecord query) {
        return paymentRecordMapper.selectList(query);
    }

    @Override
    public void createPayment(String orderNo, Long merchantId, Long userId, BigDecimal amount, String outTradeNo) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderNo(orderNo);
        record.setMerchantId(merchantId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPayType("JSAPI");
        record.setOutTradeNo(outTradeNo != null ? outTradeNo : orderNo);
        record.setPayStatus(STATUS_PENDING);

        int affectedRows = paymentRecordMapper.insertIfAbsent(record);
        if (affectedRows > 0) {
            log.info("创建支付记录: orderNo={}, amount={}", orderNo, amount);
            return;
        }
        PaymentRecord existing = paymentRecordMapper.selectByOrderNo(orderNo);
        log.info("订单 {} 已存在支付记录 {}, 跳过创建", orderNo, existing != null ? existing.getId() : null);
    }

    @Override
    public void markPaySuccess(String orderNo, String transactionId, String notifyResult) {
        markPaySuccess(orderNo, null, null, null, transactionId, notifyResult);
    }

    @Override
    public void markPaySuccess(String orderNo, Long merchantId, Long userId, BigDecimal amount,
                               String transactionId, String notifyResult) {
        PaymentRecord record = paymentRecordMapper.selectByOrderNo(orderNo);
        if (record == null) {
            log.warn("支付成功回调: 订单 {} 无支付记录，创建带订单上下文的补录", orderNo);
            record = new PaymentRecord();
            record.setOrderNo(orderNo);
            record.setMerchantId(merchantId);
            record.setUserId(userId);
            record.setAmount(amount);
            record.setPayType("JSAPI");
            record.setOutTradeNo(orderNo);
            record.setPayStatus(STATUS_PAID);
            record.setTransactionId(transactionId);
            record.setPayTime(new Date());
            record.setNotifyResult(notifyResult);
            int affectedRows = paymentRecordMapper.insertIfAbsent(record);
            if (affectedRows == 0) {
                paymentRecordMapper.markPaySuccess(orderNo, transactionId, notifyResult, new Date());
            }
            return;
        }

        if (record.getPayStatus() != null
                && (record.getPayStatus() == STATUS_PAID || record.getPayStatus() == STATUS_REFUNDED)) {
            log.info("支付记录 {} 已处于终态，跳过重复更新", record.getId());
            return;
        }

        int affectedRows = paymentRecordMapper.markPaySuccess(orderNo, transactionId, notifyResult, new Date());
        if (affectedRows > 0) {
            log.info("支付记录 {} 更新为已支付: transactionId={}", record.getId(), transactionId);
        }
    }

    @Override
    public void markRefunded(String orderNo, String notifyResult) {
        PaymentRecord record = paymentRecordMapper.selectByOrderNo(orderNo);
        if (record == null) {
            log.warn("退款成功回调: 订单 {} 无支付记录，跳过支付记录更新", orderNo);
            return;
        }
        if (record.getPayStatus() != null && record.getPayStatus() == STATUS_REFUNDED) {
            log.info("支付记录 {} 已标记为已退款，跳过重复更新", record.getId());
            return;
        }
        int affectedRows = paymentRecordMapper.markRefunded(orderNo, notifyResult);
        if (affectedRows > 0) {
            log.info("支付记录 {} 更新为已退款", record.getId());
        }
    }
}
