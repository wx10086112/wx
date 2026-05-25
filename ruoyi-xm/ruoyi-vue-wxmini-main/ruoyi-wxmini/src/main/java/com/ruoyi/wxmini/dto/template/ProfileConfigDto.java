package com.ruoyi.wxmini.dto.template;

import lombok.Data;

import java.util.List;

@Data
public class ProfileConfigDto {

    private String loginTitle;

    private String loginDesc;

    private String orderSectionTitle;

    private String orderMoreText;

    private List<ProfileOrderEntryDto> orderEntries;

    private List<ProfileAssetEntryDto> assetEntries;

    private String benefitTitle;

    private String benefitDesc;

    private List<String> benefitTips;

    private List<ProfileMenuDto> serviceMenus;

    private String logoutText;
}
