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

    void createPayment(String orderNo, Long merchantId, Long userId, BigDecimal amount, String outTradeNo,
                       String spMchId, String subMchId, String subAppId, String payerOpenid);

    /**
     * 支付成功回调：更新 transactionId、payStatus、payTime
     */
    void markPaySuccess(String orderNo, String transactionId, String notifyResult);

    /**
     * 支付成功回调：带订单上下文补全支付记录，避免回调先到时产生缺字段记录。
     */
    void markPaySuccess(String orderNo, Long merchantId, Long userId, BigDecimal amount,
                        String transactionId, String notifyResult);

    void markPaySuccess(String orderNo, Long merchantId, Long userId, BigDecimal amount,
                        String transactionId, String notifyResult,
                        String spMchId, String subMchId, String subAppId, String payerOpenid);

    /**
     * 微信退款成功回调：将支付记录标记为已退款
     */
    void markRefunded(String orderNo, String notifyResult);
}
