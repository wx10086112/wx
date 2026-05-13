package com.ruoyi.wxmini.dto.template;

import lombok.Data;

import java.util.List;

@Data
public class HomeConfigDto {

    private String locationLabel;

    private String noticeTag;

    private List<TemplateStatCardDto> statsCards;

    private String merchantSectionTitle;

    private String merchantSectionSubtitle;

    private String productSectionTitle;

    private String productSectionSubtitle;

    private List<TemplateOptionDto> sortOptions;
}
