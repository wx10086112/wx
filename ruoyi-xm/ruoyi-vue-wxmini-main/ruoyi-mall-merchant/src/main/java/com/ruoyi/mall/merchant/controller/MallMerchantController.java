package com.ruoyi.mall.merchant.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantController extends BaseController {

    private static final String MINI_APP_ENTRY_PAGE = "pages/login/login";
    private static final String MERCHANT_LOGIN_PAGE = "pages/merchant/login/login";
    private static final int MERCHANT_ENTRY_CODE_WIDTH = 430;

    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private WxMaServiceManager wxMaServiceManager;
    @Value("${wx.miniapp.qrcode.env-version:release}")
    private String qrCodeEnvVersion;
    @Value("${wx.miniapp.qrcode.check-path:false}")
    private boolean qrCodeCheckPath;

    @DataScopeBiz(distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(Merchant merchant) {
        startPage();
        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(SecurityUtils.getDistributorId());
        }
        List<Merchant> list = merchantService.selectMerchantList(merchant);
        List<Map<String, Object>> safeList = new ArrayList<>();
        for (Merchant item : list) {
            safeList.add(toSafeMap(item));
        }
        return getDataTable(safeList);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Merchant merchant = merchantService.selectMerchantById(id);
        if (merchant == null) {
            return AjaxResult.error("商家不存在");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return AjaxResult.error("无权限查看该商家");
        }
        return AjaxResult.success(toSafeMap(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:query')")
    @GetMapping("/entry-qrcode/{id}")
    public AjaxResult getEntryQrCode(@PathVariable Long id, HttpServletRequest request) {
        Merchant merchant = merchantService.selectMerchantById(id);
        if (merchant == null) {
            return AjaxResult.error("商家不存在");
        }

        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return AjaxResult.error("无权限查看该商家");
        }
        if (merchant.getStatus() == Merchant.STATUS_STOPPED) {
            return AjaxResult.error("商家已停止合作，暂不能生成后台入口码");
        }
        if (merchant.getStatus() != Merchant.STATUS_NORMAL) {
            return AjaxResult.error("商家未审核通过，暂不能生成后台入口码");
        }

        String appId = trimToNull(merchant.getCMiniAppId());
        if (StringUtils.isBlank(appId)) {
            return AjaxResult.error("该商家尚未配置统一小程序 AppID");
        }
        if (StringUtils.isBlank(merchant.getCMiniAppSecret())) {
            return AjaxResult.error("该商家尚未配置统一小程序 Secret");
        }

        String scene = "merchantId=" + id;
        String loginPage = "/" + MERCHANT_LOGIN_PAGE + "?merchantId=" + id;
        try {
            String relativePath = generateMerchantEntryCode(id, appId, merchant.getCMiniAppSecret(), scene);
            Map<String, Object> data = buildEntryQrResponse(merchant, appId, scene, loginPage, request, relativePath);
            return AjaxResult.success(data);
        } catch (IOException e) {
            return AjaxResult.error("生成后台入口码失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:add')")
    @Log(title = "商户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Merchant merchant) {
        normalizeMiniAppConfig(merchant);
        AjaxResult miniAppCheck = validateMiniAppConfig(merchant, null);
        if (miniAppCheck != null) {
            return miniAppCheck;
        }

        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(SecurityUtils.getDistributorId());
        }

        int rows = merchantService.insertMerchant(merchant);
        if (rows > 0 && merchant.getId() != null) {
            Merchant savedMerchant = merchantService.selectMerchantById(merchant.getId());
            syncWxMiniServices(null, savedMerchant);
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Merchant merchant) {
        Merchant existing = merchantService.selectMerchantById(merchant.getId());
        if (existing == null) {
            return AjaxResult.error("商家不存在");
        }

        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(existing.getDistributorId())) {
            return AjaxResult.error("无权限修改该商家");
        }

        boolean miniAppSecretMasked = "******".equals(merchant.getCMiniAppSecret());
        boolean miniAppIdChanged = !StringUtils.equals(trimToNull(merchant.getCMiniAppId()), trimToNull(existing.getCMiniAppId()));
        if (miniAppSecretMasked && miniAppIdChanged) {
            return AjaxResult.error("修改商户小程序 AppID 时，请重新填写该小程序对应的 Secret");
        }
        if (miniAppSecretMasked) {
            merchant.setCMiniAppSecret(null);
        }
        if ("******".equals(merchant.getWxPayApiKey())) {
            merchant.setWxPayApiKey(null);
        }

        normalizeMiniAppConfig(merchant);
        AjaxResult miniAppCheck = validateMiniAppConfig(buildValidationTarget(merchant, existing), existing.getId());
        if (miniAppCheck != null) {
            return miniAppCheck;
        }

        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(existing.getDistributorId());
        }

        AjaxResult paymentCheck = validateProfitShareConfig(merchant);
        if (paymentCheck != null) {
            return paymentCheck;
        }

        int rows = merchantService.updateMerchant(merchant);
        if (rows <= 0) {
            return AjaxResult.error("保存失败");
        }
        Merchant savedMerchant = merchantService.selectMerchantById(existing.getId());
        syncWxMiniServices(existing, savedMerchant);
        return AjaxResult.success("保存成功", toSafeMap(savedMerchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null) {
            return AjaxResult.error("分销商不可删除商家，请联系平台管理员处理");
        }
        return toAjax(merchantService.deleteMerchantByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商户管理 - 停止合作", businessType = BusinessType.UPDATE)
    @PutMapping("/stop/{id}")
    public AjaxResult stopCooperation(@PathVariable Long id) {
        Merchant existing = merchantService.selectMerchantById(id);
        if (existing == null) {
            return AjaxResult.error("商家不存在");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(existing.getDistributorId())) {
            return AjaxResult.error("无权限操作该商家");
        }
        Merchant update = new Merchant();
        update.setId(id);
        update.setStatus(Merchant.STATUS_STOPPED);
        return toAjax(merchantService.updateMerchant(update));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商户管理 - 恢复合作", businessType = BusinessType.UPDATE)
    @PutMapping("/resume/{id}")
    public AjaxResult resumeCooperation(@PathVariable Long id) {
        Merchant existing = merchantService.selectMerchantById(id);
        if (existing == null) {
            return AjaxResult.error("商家不存在");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(existing.getDistributorId())) {
            return AjaxResult.error("无权限操作该商家");
        }
        Merchant update = new Merchant();
        update.setId(id);
        update.setStatus(Merchant.STATUS_NORMAL);
        return toAjax(merchantService.updateMerchant(update));
    }

    private AjaxResult validateMiniAppConfig(Merchant merchant, Long currentMerchantId) {
        AjaxResult cMiniAppPairCheck = validateMiniAppPair("C端", merchant.getCMiniAppId(), merchant.getCMiniAppSecret());
        if (cMiniAppPairCheck != null) {
            return cMiniAppPairCheck;
        }

        AjaxResult cMiniAppUniqueCheck = validateMiniAppUnique("商户小程序 AppID", merchant.getCMiniAppId(), currentMerchantId);
        if (cMiniAppUniqueCheck != null) {
            return cMiniAppUniqueCheck;
        }

        return null;
    }

    private AjaxResult validateMiniAppPair(String label, String appId, String secret) {
        boolean hasAppId = StringUtils.isNotBlank(appId);
        boolean hasSecret = StringUtils.isNotBlank(secret);
        if (hasAppId != hasSecret) {
            return AjaxResult.error(label + " AppID 和 Secret 需同时填写");
        }
        return null;
    }

    private AjaxResult validateMiniAppUnique(String label, String appId, Long currentMerchantId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }

        Merchant occupiedMerchant = merchantService.selectMerchantByCAppId(appId);
        if (occupiedMerchant == null) {
            return null;
        }
        if (currentMerchantId != null && currentMerchantId.equals(occupiedMerchant.getId())) {
            return null;
        }

        String merchantName = StringUtils.defaultIfBlank(occupiedMerchant.getName(), "未命名商家");
        return AjaxResult.error(label + " 已被商家【" + merchantName + "】占用");
    }

    private Merchant buildValidationTarget(Merchant merchant, Merchant existing) {
        if (existing == null) {
            return merchant;
        }

        Merchant target = new Merchant();
        target.setCMiniAppId(merchant.getCMiniAppId());
        target.setCMiniAppSecret(resolveSecretForValidation(
                merchant.getCMiniAppId(),
                merchant.getCMiniAppSecret(),
                existing.getCMiniAppId(),
                existing.getCMiniAppSecret()
        ));
        return target;
    }

    private String resolveSecretForValidation(String nextAppId, String nextSecret, String existingAppId, String existingSecret) {
        if (StringUtils.isNotBlank(nextSecret)) {
            return nextSecret;
        }
        if (StringUtils.isNotBlank(nextAppId) && StringUtils.equals(nextAppId, trimToNull(existingAppId))) {
            return existingSecret;
        }
        return nextSecret;
    }

    private void syncWxMiniServices(Merchant oldMerchant, Merchant newMerchant) {
        syncWxMiniService(
                oldMerchant == null ? null : oldMerchant.getCMiniAppId(),
                newMerchant == null ? null : newMerchant.getCMiniAppId(),
                newMerchant == null ? null : newMerchant.getCMiniAppSecret()
        );
    }

    private void syncWxMiniService(String oldAppId, String newAppId, String newSecret) {
        if (StringUtils.isNotBlank(oldAppId) && !StringUtils.equals(oldAppId, newAppId)) {
            wxMaServiceManager.remove(oldAppId);
        }
        if (StringUtils.isBlank(newAppId)) {
            return;
        }
        if (StringUtils.isBlank(newSecret)) {
            wxMaServiceManager.remove(newAppId);
            return;
        }
        wxMaServiceManager.registerOrRefresh(newAppId, newSecret);
    }

    private void normalizeMiniAppConfig(Merchant merchant) {
        merchant.setCMiniAppId(trimToNull(merchant.getCMiniAppId()));
        merchant.setCMiniAppSecret(trimToNull(merchant.getCMiniAppSecret()));
        merchant.setMMiniAppId(null);
        merchant.setMMiniAppSecret(null);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Object> toSafeMap(Merchant merchant) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", merchant.getId());
        map.put("distributorId", merchant.getDistributorId());
        map.put("distributorName", merchant.getDistributorName());
        map.put("name", merchant.getName());
        map.put("logo", merchant.getLogo());
        map.put("contact", merchant.getContact());

        String phone = merchant.getPhone();
        map.put("phone", phone != null && phone.length() >= 7
                ? phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4)
                : phone);
        map.put("commissionRate", merchant.getCommissionRate());
        map.put("status", merchant.getStatus());
        map.put("balance", merchant.getBalance());
        map.put("totalIncome", merchant.getTotalIncome());
        map.put("address", merchant.getAddress());
        map.put("avatar", merchant.getAvatar());
        map.put("description", merchant.getDescription());
        map.put("businessHours", merchant.getBusinessHours());
        map.put("productCount", merchant.getProductCount());
        map.put("storeCount", merchant.getStoreCount());
        map.put("createTime", merchant.getCreateTime());

        map.put("cMiniAppId", merchant.getCMiniAppId());
        map.put("cMiniAppSecretConfigured", StringUtils.isNotBlank(merchant.getCMiniAppSecret()));
        map.put("cMiniAppSecretMasked", "******");
        map.put("wxPayMchId", merchant.getWxPayMchId());
        map.put("wxPayApiKeyConfigured", StringUtils.isNotBlank(merchant.getWxPayApiKey()));
        map.put("wxPayApiKeyMasked", "******");

        map.put("mapClaimStatus", merchant.getMapClaimStatus());
        map.put("mapPoiId", merchant.getMapPoiId());
        map.put("mapClaimUrl", merchant.getMapClaimUrl());
        map.put("mapClaimTime", merchant.getMapClaimTime());
        map.put("mapClaimRemark", merchant.getMapClaimRemark());

        map.put("wxApplymentId", merchant.getWxApplymentId());
        map.put("wxApplymentState", merchant.getWxApplymentState());
        map.put("wxApplymentRejectReason", merchant.getWxApplymentRejectReason());
        map.put("wxApplymentTime", merchant.getWxApplymentTime());
        map.put("wxApplymentFinishTime", merchant.getWxApplymentFinishTime());
        map.put("wxPaymentAccessType", merchant.getWxPaymentAccessType());
        map.put("merchantWxMchId", merchant.getMerchantWxMchId());
        map.put("merchantWxMchName", merchant.getMerchantWxMchName());
        map.put("wxProfitSharingEnabled", merchant.getWxProfitSharingEnabled());
        map.put("platformReceiverMchId", merchant.getPlatformReceiverMchId());
        map.put("distributorReceiverMchId", merchant.getDistributorReceiverMchId());
        map.put("merchantShareRate", merchant.getMerchantShareRate());
        map.put("platformShareRate", merchant.getPlatformShareRate());
        map.put("distributorShareRate", merchant.getDistributorShareRate());
        map.put("settlementCycle", merchant.getSettlementCycle());
        map.put("effectiveMerchantWxMchId", merchant.getEffectiveMerchantWxMchId());
        map.put("canOperate", merchant.canOperate());
        map.put("operateBlockReason", merchant.getOperateBlockReason());
        return map;
    }

    private AjaxResult validateProfitShareConfig(Merchant merchant) {
        boolean touched = merchant.getMerchantShareRate() != null
                || merchant.getPlatformShareRate() != null
                || merchant.getDistributorShareRate() != null;
        if (!touched) {
            return null;
        }
        if (merchant.getMerchantShareRate() == null
                || merchant.getPlatformShareRate() == null
                || merchant.getDistributorShareRate() == null) {
            return AjaxResult.error("商家、平台、分销商三方分账比例必须同时填写");
        }
        BigDecimal sum = merchant.getMerchantShareRate()
                .add(merchant.getPlatformShareRate())
                .add(merchant.getDistributorShareRate());
        if (sum.compareTo(new BigDecimal("100")) != 0) {
            return AjaxResult.error("商家、平台、分销商三方分账比例合计必须等于100%");
        }
        return null;
    }

    private Map<String, Object> buildEntryQrResponse(Merchant merchant, String appId, String scene,
                                                     String loginPage, HttpServletRequest request, String relativePath) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("merchantId", merchant.getId());
        data.put("merchantName", merchant.getName());
        data.put("entryAppId", appId);
        data.put("scene", scene);
        data.put("loginPage", loginPage);
        data.put("qrCodeEnvVersion", normalizeQrCodeEnvVersion());
        data.put("qrCodeCheckPath", qrCodeCheckPath);
        data.put("qrCodeUrl", buildFullUrl(request, relativePath));
        return data;
    }

    private String generateMerchantEntryCode(Long merchantId, String appId, String secret, String scene) throws IOException {
        try {
            if (shouldUseWxMiniCode(appId)) {
                wxMaServiceManager.registerOrRefresh(appId, secret);
                WxMaService maService = wxMaServiceManager.getService(appId);
                if (maService == null) {
                    throw new IOException("无法加载该商户小程序配置");
                }
                File tempFile = maService.getQrcodeService().createWxaCodeUnlimit(
                        scene,
                        MINI_APP_ENTRY_PAGE,
                        qrCodeCheckPath,
                        normalizeQrCodeEnvVersion(),
                        MERCHANT_ENTRY_CODE_WIDTH,
                        false,
                        null,
                        false);
                return saveMerchantEntryCode(merchantId, tempFile);
            }
        } catch (WxErrorException e) {
            throw new IOException("生成微信小程序码失败：" + buildWxMiniCodeErrorMessage(e), e);
        } catch (RuntimeException e) {
            throw new IOException("生成微信小程序码失败：" + buildRuntimeMiniCodeErrorMessage(e), e);
        }
        throw new IOException("该商户小程序 AppID 无效");
    }

    private boolean shouldUseWxMiniCode(String appId) {
        if (appId == null) {
            return false;
        }
        String normalized = appId.trim();
        return !normalized.isEmpty() && !"test".equalsIgnoreCase(normalized);
    }

    private String saveMerchantEntryCode(Long merchantId, File sourceFile) throws IOException {
        String fileName = "merchant-entry-" + merchantId + ".png";
        Path targetDir = Paths.get(RuoYiConfig.getProfile(), "merchant_entry_codes");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(fileName);
        Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return "/profile/merchant_entry_codes/" + fileName;
    }

    private String normalizeQrCodeEnvVersion() {
        String envVersion = StringUtils.trimToEmpty(qrCodeEnvVersion).toLowerCase();
        if ("develop".equals(envVersion) || "trial".equals(envVersion) || "release".equals(envVersion)) {
            return envVersion;
        }
        return "release";
    }

    private String buildWxMiniCodeErrorMessage(WxErrorException e) {
        String message = e.getMessage();
        if (StringUtils.contains(message, "41030") || StringUtils.containsIgnoreCase(message, "invalid page")) {
            return message + "。请确认 AppID 对应的小程序版本已包含页面 /" + MINI_APP_ENTRY_PAGE
                    + "；上线前测试可配置 wx.miniapp.qrcode.env-version=trial，必要时临时配置 wx.miniapp.qrcode.check-path=false。";
        }
        return message;
    }

    private String buildRuntimeMiniCodeErrorMessage(RuntimeException e) {
        String message = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        if (hasCause(e, SSLHandshakeException.class)) {
            return "后端访问微信接口 HTTPS 握手失败，请检查服务器网络、代理/VPN、DNS 或防火墙，确保后端机器可直连 https://api.weixin.qq.com。原始错误：" + message;
        }
        if (hasCause(e, SocketTimeoutException.class) || hasCause(e, ConnectException.class)
                || hasCause(e, UnknownHostException.class)) {
            return "后端访问微信接口超时或无法连接，请检查服务器网络、代理/VPN、DNS 或防火墙，确保后端机器可访问 https://api.weixin.qq.com。原始错误：" + message;
        }
        return message;
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildFullUrl(HttpServletRequest request, String relativePath) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(request.getScheme()) && port == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && port == 443);
        if (!defaultPort) {
            url.append(":").append(port);
        }
        url.append(relativePath);
        return url.toString();
    }
}
