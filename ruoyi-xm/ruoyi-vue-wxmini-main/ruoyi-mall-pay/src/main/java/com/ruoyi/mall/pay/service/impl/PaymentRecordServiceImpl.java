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

    /** 支付状态: 0=待支付, 1=已支付, 2=已关闭, 3=已退款 */
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
        // 幂等：同一订单不重复创建
        PaymentRecord existing = paymentRecordMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("订单 {} 已存在支付记录 {}, 跳过创建", orderNo, existing.getId());
            return;
        }

        PaymentRecord record = new PaymentRecord();
        record.setOrderNo(orderNo);
        record.setMerchantId(merchantId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPayType("JSAPI");
        record.setOutTradeNo(outTradeNo != null ? outTradeNo : orderNo);
        record.setPayStatus(STATUS_PENDING);

        paymentRecordMapper.insert(record);
        log.info("创建支付记录: id={}, orderNo={}, amount={}", record.getId(), orderNo, amount);
    }

    @Override
    public void markPaySuccess(String orderNo, String transactionId, String notifyResult) {
        PaymentRecord record = paymentRecordMapper.selectByOrderNo(orderNo);
        if (record == null) {
            log.warn("支付成功回调: 订单 {} 无支付记录，创建补录", orderNo);
            record = new PaymentRecord();
            record.setOrderNo(orderNo);
            record.setPayType("JSAPI");
            record.setOutTradeNo(orderNo);
            record.setPayStatus(STATUS_PAID);
            record.setTransactionId(transactionId);
            record.setPayTime(new Date());
            record.setNotifyResult(notifyResult);
            paymentRecordMapper.insert(record);
            return;
        }

        // 幂等：已支付不重复处理
        if (record.getPayStatus() != null && record.getPayStatus() >= STATUS_PAID) {
            log.info("支付记录 {} 已支付，幂等跳过", record.getId());
            return;
        }

        record.setPayStatus(STATUS_PAID);
        record.setTransactionId(transactionId);
        record.setPayTime(new Date());
        record.setNotifyResult(notifyResult);
        paymentRecordMapper.updateById(record);
        log.info("支付记录 {} 更新为已支付: transactionId={}", record.getId(), transactionId);
    }
}
