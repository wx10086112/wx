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
}
