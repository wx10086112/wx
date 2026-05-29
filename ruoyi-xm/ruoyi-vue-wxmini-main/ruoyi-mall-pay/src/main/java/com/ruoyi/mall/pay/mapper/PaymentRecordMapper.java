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

    int updateById(PaymentRecord record);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);
}
