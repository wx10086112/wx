package com.ruoyi.mall.pay.mapper;

import com.ruoyi.mall.pay.domain.PaymentRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentRecordMapper {

    PaymentRecord selectById(Long id);

    PaymentRecord selectByOrderNo(String orderNo);

    PaymentRecord selectByOutTradeNo(String outTradeNo);

    List<PaymentRecord> selectList(PaymentRecord query);

    int insert(PaymentRecord record);

    int insertIfAbsent(PaymentRecord record);

    int updateById(PaymentRecord record);

    int markPaySuccess(@Param("orderNo") String orderNo,
                       @Param("transactionId") String transactionId,
                       @Param("notifyResult") String notifyResult,
                       @Param("payTime") java.util.Date payTime,
                       @Param("spMchId") String spMchId,
                       @Param("subMchId") String subMchId,
                       @Param("subAppId") String subAppId,
                       @Param("payerOpenid") String payerOpenid);

    int markRefunded(@Param("orderNo") String orderNo, @Param("notifyResult") String notifyResult);

    int markClosed(@Param("orderNo") String orderNo, @Param("notifyResult") String notifyResult);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);
}
