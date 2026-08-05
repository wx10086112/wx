package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.system.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/template")
public class WxTemplateController {

    private static final String DEFAULT_OPERATOR_NAME = "本地生活";
    private static final String DEFAULT_RIGHTS_TIPS = "可通过小程序“联系客服”、订单详情商户联系方式或微信小程序主体公示联系方式提交个人信息权利请求。";

    @Resource
    private ISysConfigService configService;

    @Resource
    private IMerchantService merchantService;

    @GetMapping("/config")
    public AjaxResult config(@RequestHeader(value = "X-Wx-AppId", required = false) String wxAppId,
                             @RequestParam(value = "appid", required = false) String appid) {
        Map<String, Object> data = new HashMap<>();

        String requestAppId = StringUtils.defaultIfBlank(wxAppId, appid);
        Merchant appMerchant = resolveMerchantByCAppId(requestAppId);

        String configuredOperatorName = configValue("mall.privacy.operatorName", "");
        boolean operatorNameMissing = StringUtils.isBlank(valueFromMerchant(appMerchant, "name"))
                && StringUtils.isBlank(configuredOperatorName);
        String operatorName = firstNonBlank(valueFromMerchant(appMerchant, "name"),
                firstNonBlank(configuredOperatorName, DEFAULT_OPERATOR_NAME));
        String servicePhone = firstNonBlank(valueFromMerchant(appMerchant, "phone"),
                configValue("mall.privacy.servicePhone", ""));
        String contactEmail = configValue("mall.privacy.contactEmail", "");
        String contactAddress = firstNonBlank(valueFromMerchant(appMerchant, "address"),
                configValue("mall.privacy.contactAddress", ""));
        String businessHoursText = firstNonBlank(valueFromMerchant(appMerchant, "businessHours"),
                configValue("mall.privacy.businessHoursText", ""));
        String rightsRequestTips = configValue("mall.privacy.rightsRequestTips", DEFAULT_RIGHTS_TIPS);
        List<String> missingPrivacyFields = buildMissingPrivacyFields(operatorNameMissing, servicePhone, contactEmail,
                contactAddress, requestAppId, appMerchant);

        // templateMeta
        Map<String, Object> templateMeta = new HashMap<>();
        templateMeta.put("code", "merchant_o2o_v1");
        templateMeta.put("name", "门店服务");
        templateMeta.put("version", "1.1.0");
        templateMeta.put("configOwner", "merchant-mini-admin");
        templateMeta.put("description", "门店团购到店核销小程序模板");
        data.put("templateMeta", templateMeta);

        // brandInfo
        Map<String, Object> brandInfo = new HashMap<>();
        brandInfo.put("id", "brand_001");
        brandInfo.put("name", operatorName);
        brandInfo.put("logo", resolveMerchantImage(appMerchant));
        brandInfo.put("slogan", "");
        brandInfo.put("notice", "欢迎使用门店团购小程序");
        brandInfo.put("servicePhone", servicePhone);
        brandInfo.put("searchPlaceholder", "搜索套餐、服务项目");
        brandInfo.put("primaryColor", "#1677ff");
        data.put("brandInfo", brandInfo);

        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("operatorName", operatorName);
        contactInfo.put("servicePhone", servicePhone);
        contactInfo.put("contactEmail", contactEmail);
        contactInfo.put("contactAddress", contactAddress);
        contactInfo.put("businessHoursText", businessHoursText);
        contactInfo.put("rightsRequestTips", rightsRequestTips);
        contactInfo.put("configured", missingPrivacyFields.isEmpty());
        contactInfo.put("missingFields", missingPrivacyFields);
        contactInfo.put("configSource", appMerchant != null ? "merchant" : "system");
        contactInfo.put("matchedAppId", appMerchant != null);
        data.put("contactInfo", contactInfo);

        Map<String, Object> privacyInfo = new HashMap<>();
        privacyInfo.put("operatorName", operatorName);
        privacyInfo.put("processorText", "本小程序个人信息处理者为：" + operatorName);
        privacyInfo.put("servicePhone", servicePhone);
        privacyInfo.put("contactEmail", contactEmail);
        privacyInfo.put("contactAddress", contactAddress);
        privacyInfo.put("rightsRequestTips", rightsRequestTips);
        privacyInfo.put("wechatPrivacyFillReminder", "微信公众平台隐私保护指引需与小程序内展示的运营主体、电话、邮箱、地址保持一致。");
        privacyInfo.put("configured", missingPrivacyFields.isEmpty());
        privacyInfo.put("missingFields", missingPrivacyFields);
        privacyInfo.put("configSource", appMerchant != null ? "merchant" : "system");
        privacyInfo.put("matchedAppId", appMerchant != null);
        data.put("privacyInfo", privacyInfo);

        // home
        Map<String, Object> home = new HashMap<>();
        home.put("locationLabel", "距离本店");
        home.put("merchantSectionTitle", "本店信息");
        home.put("merchantSectionSubtitle", "查看门店详情");
        home.put("productSectionTitle", "本店服务项目");
        home.put("productSectionSubtitle", "精选团购套餐");
        home.put("sortOptions", new ArrayList<>());
        data.put("home", home);

        // profile
        Map<String, Object> profile = new HashMap<>();
        profile.put("loginTitle", "点击登录");
        profile.put("loginDesc", "登录后享受更多服务");
        profile.put("orderSectionTitle", "我的订单");
        profile.put("orderMoreText", "全部订单 >");

        List<Map<String, String>> orderEntries = new ArrayList<>();
        Map<String, String> e1 = new HashMap<>();
        e1.put("label", "待支付");
        e1.put("status", "PENDING_PAY");
        orderEntries.add(e1);
        Map<String, String> e2 = new HashMap<>();
        e2.put("label", "待使用");
        e2.put("status", "UNUSED");
        orderEntries.add(e2);
        Map<String, String> e3 = new HashMap<>();
        e3.put("label", "退款/售后");
        e3.put("status", "AFTER_SALE");
        orderEntries.add(e3);
        profile.put("orderEntries", orderEntries);

        List<Map<String, String>> assetEntries = new ArrayList<>();
        Map<String, String> a1 = new HashMap<>();
        a1.put("label", "优惠券/红包");
        a1.put("url", "/pages/coupon/coupon");
        a1.put("countField", "couponCount");
        assetEntries.add(a1);
        Map<String, String> a2 = new HashMap<>();
        a2.put("label", "我的收藏");
        a2.put("url", "/pages/favorite/favorite");
        a2.put("countField", "favoriteCount");
        assetEntries.add(a2);
        profile.put("assetEntries", assetEntries);

        profile.put("benefitTitle", "权益中心");
        profile.put("benefitDesc", "查看您的专属权益");
        profile.put("benefitTips", Arrays.asList("支持到店使用", "支持退款售后"));

        List<Map<String, String>> serviceMenus = new ArrayList<>();
        Map<String, String> s1 = new HashMap<>();
        s1.put("label", "我的优惠券 / 红包");
        s1.put("url", "/pages/coupon/coupon");
        serviceMenus.add(s1);
        Map<String, String> s2 = new HashMap<>();
        s2.put("label", "我的收藏");
        s2.put("url", "/pages/favorite/favorite");
        serviceMenus.add(s2);
        Map<String, String> s3 = new HashMap<>();
        s3.put("label", "联系客服");
        s3.put("url", "/pages/contact/contact");
        serviceMenus.add(s3);
        profile.put("serviceMenus", serviceMenus);

        profile.put("logoutText", "退出登录");
        data.put("profile", profile);

        // merchantDetail
        Map<String, Object> merchantDetail = new HashMap<>();
        merchantDetail.put("hotTag", "热门");
        merchantDetail.put("phoneActionText", "一键拨打");
        merchantDetail.put("mapActionText", "查看地图");
        merchantDetail.put("addressTitle", "门店地址");
        merchantDetail.put("productSectionTitle", "在售项目");
        merchantDetail.put("productSectionSubtitle", "该门店提供的团购套餐");
        merchantDetail.put("albumSectionTitle", "门店相册");
        merchantDetail.put("albumSectionSubtitle", "门店环境实拍");
        merchantDetail.put("collectText", "收藏门店");
        merchantDetail.put("collectedText", "已收藏");
        merchantDetail.put("contactButtonText", "联系门店");
        data.put("merchantDetail", merchantDetail);

        // productDetail
        Map<String, Object> productDetail = new HashMap<>();
        productDetail.put("decisionSectionTitle", "购买决策信息");
        productDetail.put("ruleSectionTitle", "使用规则");
        productDetail.put("merchantSectionTitle", "服务门店");
        productDetail.put("contentSectionTitle", "项目内容");
        productDetail.put("salesLabel", "已售");
        productDetail.put("stockLabel", "库存");
        productDetail.put("validDaysLabel", "有效期");
        productDetail.put("timeRangeRuleText", "购买后有效期内可使用");
        productDetail.put("bookingYesText", "需要预点单");
        productDetail.put("bookingNoText", "无需预点单");
        productDetail.put("collectText", "收藏");
        productDetail.put("collectedText", "已收藏");
        productDetail.put("shareText", "分享");
        productDetail.put("buyButtonText", "立即抢购");
        data.put("productDetail", productDetail);

        // checkout
        Map<String, Object> checkout = new HashMap<>();
        checkout.put("productSectionTitle", "确认商品");
        checkout.put("infoSectionTitle", "购买信息");
        checkout.put("priceSectionTitle", "价格明细");
        checkout.put("useRuleSectionTitle", "使用说明");
        checkout.put("quantityLabel", "购买数量");
        checkout.put("phoneLabel", "手机号");
        checkout.put("couponLabel", "优惠券");
        checkout.put("subtotalLabel", "商品金额");
        checkout.put("discountLabel", "优惠抵扣");
        checkout.put("totalLabel", "实付总金额");
        checkout.put("paySummaryLabel", "待支付");
        checkout.put("submitButtonText", "提交订单并支付");
        checkout.put("loginHintText", "请先登录后再进行支付");
        data.put("checkout", checkout);

        // featureToggle
        Map<String, Object> featureToggle = new HashMap<>();
        featureToggle.put("enableCoupon", true);
        featureToggle.put("enableFavorite", true);
        featureToggle.put("enableAddress", false);
        featureToggle.put("enableJoinApply", false);
        featureToggle.put("enableBookingRule", true);
        featureToggle.put("enableRefundRule", true);
        featureToggle.put("enableMerchantAlbum", true);
        data.put("featureToggle", featureToggle);

        return AjaxResult.success(data);
    }

    private String configValue(String key, String fallback) {
        String value = configService.selectConfigByKey(key);
        return StringUtils.isNotBlank(value) ? value.trim() : fallback;
    }

    private Merchant resolveMerchantByCAppId(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        return merchantService.selectMerchantByCAppId(appId.trim());
    }

    private String valueFromMerchant(Merchant merchant, String field) {
        if (merchant == null) {
            return "";
        }
        if ("name".equals(field)) {
            return merchant.getName();
        }
        if ("phone".equals(field)) {
            return merchant.getPhone();
        }
        if ("address".equals(field)) {
            return merchant.getAddress();
        }
        if ("businessHours".equals(field)) {
            return merchant.getBusinessHours();
        }
        return "";
    }

    private String firstNonBlank(String first, String fallback) {
        return StringUtils.isNotBlank(first) ? first.trim() : fallback;
    }

    private String resolveMerchantImage(Merchant merchant) {
        if (merchant == null) {
            return "";
        }
        return firstNonBlank(merchant.getLogo(), firstNonBlank(merchant.getAvatar(), ""));
    }

    private List<String> buildMissingPrivacyFields(boolean operatorNameMissing, String servicePhone,
                                                   String contactEmail, String contactAddress,
                                                   String requestAppId, Merchant appMerchant) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isNotBlank(requestAppId) && appMerchant == null) {
            missing.add("小程序AppID对应商户");
        }
        if (operatorNameMissing) {
            missing.add("运营主体名称");
        }
        if (StringUtils.isBlank(servicePhone)) {
            missing.add("客服电话");
        }
        if (StringUtils.isBlank(contactEmail)) {
            missing.add("联系邮箱");
        }
        if (StringUtils.isBlank(contactAddress)) {
            missing.add("注册地址或常用联系地址");
        }
        return missing;
    }
}
