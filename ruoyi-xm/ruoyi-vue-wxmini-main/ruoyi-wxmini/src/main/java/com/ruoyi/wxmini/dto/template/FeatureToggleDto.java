package com.ruoyi.wxmini.dto.template;

import lombok.Data;

@Data
public class FeatureToggleDto {

    private Boolean enableCoupon;

    private Boolean enableFavorite;

    private Boolean enableAddress;

    private Boolean enableReview;

    private Boolean enableJoinApply;

    private Boolean enableBookingRule;

    private Boolean enableRefundRule;

    private Boolean enableMerchantAlbum;
}
