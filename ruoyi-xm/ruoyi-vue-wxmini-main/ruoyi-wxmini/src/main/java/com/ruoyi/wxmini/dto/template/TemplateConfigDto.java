package com.ruoyi.wxmini.dto.template;

import lombok.Data;

@Data
public class TemplateConfigDto {

    private TemplateMetaDto templateMeta;

    private BrandConfigDto brandInfo;

    private HomeConfigDto home;

    private ProfileConfigDto profile;

    private MerchantDetailConfigDto merchantDetail;

    private ProductDetailConfigDto productDetail;

    private CheckoutConfigDto checkout;

    private FeatureToggleDto featureToggle;
}
