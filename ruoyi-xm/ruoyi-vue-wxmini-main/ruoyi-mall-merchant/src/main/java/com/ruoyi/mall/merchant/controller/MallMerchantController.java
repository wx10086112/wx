package com.ruoyi.mall.merchant.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.common.config.WxMaProperties;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantController extends BaseController {

    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private WxMaProperties wxMaProperties;
    @Autowired
    private WxMaServiceManager wxMaServiceManager;

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

        List<WxMaProperties.Config> configs = wxMaProperties.getConfigs();
        if (configs == null || configs.isEmpty() || configs.get(0) == null) {
            return AjaxResult.error("统一小程序配置缺失");
        }

        String appId = configs.get(0).getAppid();
        if (StringUtils.isBlank(appId)) {
            return AjaxResult.error("统一小程序 AppID 未配置");
        }

        String scene = "merchantId=" + id;
        String loginPage = "/pages/merchant/login/login?merchantId=" + id;
        try {
            String relativePath = generateMerchantEntryCode(id, appId, scene, request);
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

        if ("******".equals(merchant.getCMiniAppSecret())) {
            merchant.setCMiniAppSecret(null);
        }
        if ("******".equals(merchant.getMMiniAppSecret())) {
            merchant.setMMiniAppSecret(null);
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
        if (rows > 0) {
            Merchant savedMerchant = merchantService.selectMerchantById(existing.getId());
            syncWxMiniServices(existing, savedMerchant);
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null) {
            for (Long id : ids) {
                Merchant merchant = merchantService.selectMerchantById(id);
                if (merchant != null && !effDistributorId.equals(merchant.getDistributorId())) {
                    return AjaxResult.error("无权限删除该商家");
                }
            }
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

        AjaxResult mMiniAppPairCheck = validateMiniAppPair("商家端", merchant.getMMiniAppId(), merchant.getMMiniAppSecret());
        if (mMiniAppPairCheck != null) {
            return mMiniAppPairCheck;
        }

        AjaxResult cMiniAppUniqueCheck = validateMiniAppUnique("C端 AppID", merchant.getCMiniAppId(), currentMerchantId);
        if (cMiniAppUniqueCheck != null) {
            return cMiniAppUniqueCheck;
        }

        AjaxResult mMiniAppUniqueCheck = validateMiniAppUnique("商家端 AppID", merchant.getMMiniAppId(), currentMerchantId);
        if (mMiniAppUniqueCheck != null) {
            return mMiniAppUniqueCheck;
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

        Merchant occupiedMerchant = merchantService.selectMerchantByAnyMiniAppId(appId);
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
        target.setMMiniAppId(merchant.getMMiniAppId());
        target.setMMiniAppSecret(resolveSecretForValidation(
                merchant.getMMiniAppId(),
                merchant.getMMiniAppSecret(),
                existing.getMMiniAppId(),
                existing.getMMiniAppSecret()
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
        syncWxMiniService(
                oldMerchant == null ? null : oldMerchant.getMMiniAppId(),
                newMerchant == null ? null : newMerchant.getMMiniAppId(),
                newMerchant == null ? null : newMerchant.getMMiniAppSecret()
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
        merchant.setMMiniAppId(trimToNull(merchant.getMMiniAppId()));
        merchant.setMMiniAppSecret(trimToNull(merchant.getMMiniAppSecret()));
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
        map.put("mMiniAppId", merchant.getMMiniAppId());
        map.put("mMiniAppSecretConfigured", StringUtils.isNotBlank(merchant.getMMiniAppSecret()));
        map.put("mMiniAppSecretMasked", "******");
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
        data.put("qrCodeUrl", buildFullUrl(request, relativePath));
        return data;
    }

    private String generateMerchantEntryCode(Long merchantId, String appId, String scene, HttpServletRequest request) throws IOException {
        try {
            if (shouldUseWxMiniCode(appId)) {
                wxMaService.switchoverTo(appId);
                File tempFile = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/merchant/login/login");
                return saveMerchantEntryCode(merchantId, tempFile);
            }
        } catch (WxErrorException e) {
            return saveMerchantEntryFallbackCode(merchantId, buildMerchantEntryLandingUrl(request, merchantId));
        }
        return saveMerchantEntryFallbackCode(merchantId, buildMerchantEntryLandingUrl(request, merchantId));
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

    private String saveMerchantEntryFallbackCode(Long merchantId, String entryUrl) throws IOException {
        String fileName = "merchant-entry-" + merchantId + ".png";
        Path targetDir = Paths.get(RuoYiConfig.getProfile(), "merchant_entry_codes");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(fileName);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = writer.encode(entryUrl, BarcodeFormat.QR_CODE, 430, 430, hints);
            writeBitMatrixToPng(bitMatrix, targetFile);
            return "/profile/merchant_entry_codes/" + fileName;
        } catch (WriterException e) {
            throw new IOException("本地二维码生成失败：" + e.getMessage(), e);
        }
    }

    private void writeBitMatrixToPng(BitMatrix bitMatrix, Path targetFile) throws IOException {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        ImageIO.write(image, "PNG", targetFile.toFile());
    }

    private String buildMerchantEntryLandingUrl(HttpServletRequest request, Long merchantId) {
        return buildFullUrl(request, "/wxmini/merchant-mini/entry/" + merchantId);
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
