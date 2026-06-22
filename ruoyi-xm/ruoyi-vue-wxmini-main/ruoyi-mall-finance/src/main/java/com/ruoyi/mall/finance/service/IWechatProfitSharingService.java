package com.ruoyi.mall.finance.service;

public interface IWechatProfitSharingService {

    void processOrderProfitSharing(String orderNo);

    void finishOrderProfitSharing(String orderNo);
}
