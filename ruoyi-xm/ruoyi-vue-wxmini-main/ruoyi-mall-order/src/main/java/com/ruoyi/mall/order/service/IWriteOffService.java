package com.ruoyi.mall.order.service;

import com.ruoyi.mall.order.vo.WriteOffResultVO;

public interface IWriteOffService {

    /**
     * 核销订单
     * @param code 核销码
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @return 核销结果
     */
    WriteOffResultVO writeOff(String code, Long merchantId, Long operatorId, String operatorName);

    /**
     * 核销订单（指定核销门店）
     * @param storeId 核销门店ID，为空时使用订单归属门店
     */
    WriteOffResultVO writeOff(String code, Long merchantId, Long operatorId, String operatorName, Long storeId);
}
