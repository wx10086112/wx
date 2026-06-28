package com.ruoyi.wxmini.controller;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayPartnerOrderQueryV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.common.vo.WxPayOrderVo;
import com.ruoyi.mall.common.vo.WxPayParamVo;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/pay")
public class WxPayController {

    private static final Logger log = LoggerFactory.getLogger(WxPayController.class);

    @Value("${wx.pay.stub-enabled:false}")
    private boolean stubEnabled;
    @Value("${wx.pay.private-key-path:}")
    private String privateKeyPath;
    @Value("${wx.pay.private-cert-path:}")
    private String privateCertPath;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IWxPayOrderService wxPayOrderService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private IUserInfoService userInfoService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;

    @PostMapping("/order/create")
    public AjaxResult createPay(@RequestBody Map<String, String> body) {
        String orderNo = body != null ? body.get("orderNo") : null;
        if (StringUtils.isBlank(orderNo)) {
            return AjaxResult.error("订单号不能为空");
        }

        String userId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(userId)) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        UserInfo currentUser = resolveUserInfo(userId);
        if (order == null || !isCurrentUserOrder(order, currentUser)) {
            return AjaxResult.error("订单不存在");
        }
        AjaxResult tenantCheck = checkOrderTenant(order);
        if (tenantCheck != null) {
            return tenantCheck;
        }
        if (order.getStatus() == null || order.getStatus() != MallOrderStatus.PENDING) {
            return AjaxResult.error("当前订单状态不可支付");
        }
        AjaxResult payConfigCheck = checkMerchantPayReady(order.getMerchantId());
        if (payConfigCheck != null) {
            return payConfigCheck;
        }

        if (stubEnabled) {
            Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
            paymentRecordService.createPayment(orderNo, order.getMerchantId(), currentUser.getId(), order.getPayAmount(), orderNo,
                    wxPayService != null && wxPayService.getConfig() != null ? wxPayService.getConfig().getMchId() : null,
                    merchant != null ? merchant.getEffectiveMerchantWxMchId() : null,
                    merchant != null ? merchant.getCMiniAppId() : null,
                    currentUser.getOpenId());

            Map<String, Object> result = new HashMap<>();
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", generateNonceStr());
            result.put("package", "prepay_id=wx_stub_" + orderNo);
            result.put("signType", "HMAC-SHA256");
            result.put("paySign", "stub_sign_" + orderNo);
            return AjaxResult.success(result);
        }

        try {
            WxPayOrderVo payVo = new WxPayOrderVo();
            payVo.setOrderNo(orderNo);
            payVo.setOpenId(resolveOpenId(userId));

            if (StringUtils.isBlank(payVo.getOpenId())) {
                return AjaxResult.error("用户openId不存在，请重新登录后再试");
            }

            WxPayParamVo payParam = wxPayOrderService.createOrder(userId, payVo);
            if (payParam == null) {
                return AjaxResult.error("创建支付订单失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("timeStamp", payParam.getPayParam().getTimeStamp());
            result.put("nonceStr", payParam.getPayParam().getNonceStr());
            result.put("package", payParam.getPayParam().getPackageValue());
            result.put("signType", payParam.getPayParam().getSignType());
            result.put("paySign", payParam.getPayParam().getPaySign());
            result.put("orderNo", payParam.getOrderNo());
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("微信支付创建失败: orderNo={}, userId={}", orderNo, userId, e);
            return AjaxResult.error("支付创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/order/query")
    public AjaxResult queryPay(@RequestParam String outTradeNo) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderService.selectMallOrderByOrderNo(outTradeNo);
        if (order == null || !isCurrentUserOrder(order, resolveUserInfo(currentUserId))) {
            return AjaxResult.error("订单不存在");
        }
        AjaxResult tenantCheck = checkOrderTenant(order);
        if (tenantCheck != null) {
            return tenantCheck;
        }

        boolean isLocalPaid = MallOrderStatus.isPaidState(order.getStatus());
        if (!isLocalPaid && !stubEnabled && wxPayService != null) {
            try {
                Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
                if (merchant == null || StringUtils.isBlank(merchant.getEffectiveMerchantWxMchId())) {
                    return AjaxResult.error("商户微信支付子商户号未配置");
                }
                WxPayPartnerOrderQueryV3Request queryReq = new WxPayPartnerOrderQueryV3Request()
                        .setOutTradeNo(outTradeNo)
                        .setSpMchId(wxPayService.getConfig().getMchId())
                        .setSubMchId(merchant != null ? merchant.getEffectiveMerchantWxMchId() : null);
                WxPayPartnerOrderQueryV3Result wxResult = wxPayService.queryPartnerOrderV3(queryReq);
                if ("SUCCESS".equals(wxResult.getTradeState())
                        && isSamePartnerPayment(merchant, wxResult)
                        && isSameAmount(order, wxResult)) {
                    java.util.Date payTime = new java.util.Date();
                    mallOrderService.markOrderPaid(outTradeNo, payTime);
                    paymentRecordService.markPaySuccess(outTradeNo, order.getMerchantId(), order.getUserId(),
                            order.getPayAmount(), wxResult.getTransactionId(), "query-sync",
                            wxResult.getSpMchId(), wxResult.getSubMchId(), wxResult.getSubAppid(),
                            wxResult.getPayer() != null ? wxResult.getPayer().getSubOpenid() : null);
                    order.setPayTime(payTime);
                    isLocalPaid = true;
                } else if ("SUCCESS".equals(wxResult.getTradeState())) {
                    log.warn("微信支付查单归属或金额不匹配，拒绝同步本地订单状态: orderNo={}, spMchId={}, subMchId={}, subAppId={}, localAmount={}, wxAmount={}",
                            outTradeNo, wxResult.getSpMchId(), wxResult.getSubMchId(), wxResult.getSubAppid(),
                            toFen(order.getPayAmount()),
                            wxResult.getAmount() != null ? wxResult.getAmount().getTotal() : null);
                }
            } catch (Exception e) {
                log.warn("微信支付查单同步失败: orderNo={}, error={}", outTradeNo, e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("tradeState", isLocalPaid ? "SUCCESS" : "NOTPAY");
        result.put("tradeType", "JSAPI");
        result.put("amount", toFen(order.getPayAmount()));
        result.put("successTime", order.getPayTime() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(order.getPayTime()) : null);
        return AjaxResult.success(result);
    }

    private boolean isSamePartnerPayment(Merchant merchant, WxPayPartnerOrderQueryV3Result wxResult) {
        return merchant != null
                && wxPayService != null
                && wxPayService.getConfig() != null
                && StringUtils.equals(wxResult.getSpMchId(), wxPayService.getConfig().getMchId())
                && StringUtils.equals(wxResult.getSubMchId(), merchant.getEffectiveMerchantWxMchId())
                && StringUtils.equals(wxResult.getSubAppid(), merchant.getCMiniAppId());
    }

    private boolean isSameAmount(MallOrder order, WxPayPartnerOrderQueryV3Result wxResult) {
        return order != null
                && wxResult != null
                && wxResult.getAmount() != null
                && wxResult.getAmount().getTotal() != null
                && wxResult.getAmount().getTotal() == toFen(order.getPayAmount());
    }

    private AjaxResult checkMerchantPayReady(Long merchantId) {
        AjaxResult platformPayCheck = checkPlatformPayReady();
        if (platformPayCheck != null) {
            return platformPayCheck;
        }
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant == null) {
            return AjaxResult.error("商户不存在");
        }
        String blockReason = merchant.getOperateBlockReason();
        if (StringUtils.isNotBlank(blockReason)) {
            return AjaxResult.error("商户支付配置不完整: " + blockReason);
        }
        return null;
    }

    private AjaxResult checkPlatformPayReady() {
        if (stubEnabled) {
            return null;
        }
        if (wxPayService == null || wxPayService.getConfig() == null
                || StringUtils.isBlank(wxPayService.getConfig().getMchId())) {
            log.warn("微信支付服务商配置未完成: wxPayService/config/mchId missing");
            return AjaxResult.error("平台微信支付配置未完成，请联系平台处理");
        }
        if (!isReadablePayFile(privateKeyPath) || !isReadablePayFile(privateCertPath)) {
            log.warn("微信支付服务商证书文件缺失或为空: privateKeyPath={}, privateCertPath={}",
                    maskPath(privateKeyPath), maskPath(privateCertPath));
            return AjaxResult.error("平台微信支付证书未配置，请联系平台处理");
        }
        return null;
    }

    private boolean isReadablePayFile(String path) {
        if (StringUtils.isBlank(path) || path.startsWith("classpath:")) {
            return false;
        }
        try {
            return Files.isRegularFile(Paths.get(path)) && Files.size(Paths.get(path)) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String maskPath(String path) {
        if (StringUtils.isBlank(path)) {
            return "<empty>";
        }
        int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slashIndex >= 0 ? "..." + path.substring(slashIndex) : path;
    }

    private AjaxResult checkOrderTenant(MallOrder order) {
        Long tokenMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (tokenMerchantId == null || !tokenMerchantId.equals(order.getMerchantId())) {
            return AjaxResult.error("订单商户与当前小程序登录态不匹配");
        }
        Long appIdMerchantId = WxMiniUserContext.getAppIdMerchantId();
        if (appIdMerchantId != null && !appIdMerchantId.equals(order.getMerchantId())) {
            return AjaxResult.error("订单商户与当前小程序AppID不匹配");
        }
        return null;
    }

    private String resolveOpenId(String userId) {
        UserInfo userInfo = resolveUserInfo(userId);
        return userInfo != null ? userInfo.getOpenId() : null;
    }

    private boolean isCurrentUserOrder(MallOrder order, UserInfo userInfo) {
        return userInfo != null && userInfo.getId() != null
                && order.getUserId() != null && order.getUserId().equals(userInfo.getId());
    }

    private UserInfo resolveUserInfo(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return userInfoService.selectUserInfoByUserId(userId);
    }

    private long toFen(BigDecimal amount) {
        return amount != null ? amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact() : 0L;
    }

    private String generateNonceStr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
