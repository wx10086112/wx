package com.ruoyi.mall.finance.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Keeps real-money WeChat transfers behind an explicit second confirmation.
 */
@Component
public class WechatTransferSafetyGuard {

    static final String LIVE_TRANSFER_CONFIRMATION = "REAL_WECHAT_TRANSFER_CONFIRMED";

    @Value("${wx.pay.stub-enabled:false}")
    private boolean stubEnabled;
    @Value("${wx.pay.transfer-enabled:false}")
    private boolean transferEnabled;
    @Value("${wx.pay.transfer-task-enabled:false}")
    private boolean transferTaskEnabled;
    @Value("${wx.pay.transfer-live-confirmation:}")
    private String transferLiveConfirmation;

    @PostConstruct
    public void validateAtStartup() {
        validateStartupConfiguration();
    }

    public void validateStartupConfiguration() {
        if (transferTaskEnabled && !transferEnabled) {
            throw new IllegalStateException("wx.pay.transfer-task-enabled requires wx.pay.transfer-enabled=true");
        }
        if (transferEnabled && !stubEnabled && !isLiveTransferConfirmed()) {
            throw new IllegalStateException("Real WeChat transfer requires wx.pay.transfer-live-confirmation="
                    + LIVE_TRANSFER_CONFIRMATION);
        }
    }

    public void ensureTransferAllowed() {
        if (!transferEnabled) {
            throw new IllegalStateException("WeChat transfer is disabled");
        }
        if (!stubEnabled && !isLiveTransferConfirmed()) {
            throw new IllegalStateException("Real WeChat transfer confirmation is missing");
        }
    }

    private boolean isLiveTransferConfirmed() {
        return StringUtils.equals(LIVE_TRANSFER_CONFIRMATION, StringUtils.trim(transferLiveConfirmation));
    }
}
