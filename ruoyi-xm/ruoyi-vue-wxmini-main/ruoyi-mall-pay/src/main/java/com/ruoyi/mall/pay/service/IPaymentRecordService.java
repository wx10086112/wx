package com.ruoyi.mall.pay.service;

import com.ruoyi.mall.pay.domain.PaymentRecord;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentRecordService {

    PaymentRecord selectById(Long id);

    PaymentRecord selectByOrderNo(String orderNo);

    PaymentRecord selectByOutTradeNo(String outTradeNo);

    List<PaymentRecord> selectList(PaymentRecord query);

    /**
     * 创建支付记录（发起支付时调用，幂等）
     */
    void createPayment(String orderNo, Long merchantId, Long userId, BigDecimal amount, String outTradeNo);

    /**
     * 支付成功回调：更新 transactionId、payStatus、payTime
     */
    void markPaySuccess(String orderNo, String transactionId, String notifyResult);

    /**
     * 微信退款成功回调：将支付记录标记为已退款
     */
    void markRefunded(String orderNo, String notifyResult);
}
