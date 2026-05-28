package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.common.vo.WxPayOrderVo;
import com.ruoyi.mall.common.vo.WxPayParamVo;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.service.IMallOrderService;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/pay")
public class WxPayController {

    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;

    @Value("${wx.pay.stub-enabled:false}")
    private boolean stubEnabled;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IWxPayOrderService wxPayOrderService;

    @PostMapping("/order/create")
    public AjaxResult createPay(@RequestBody Map<String, String> body) {
        String orderNo = body != null ? body.get("orderNo") : null;
        if (orderNo == null || orderNo.isEmpty()) {
            return AjaxResult.error("订单号不能为空");
        }

        String userId = WxMiniUserContext.getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getUserId().toString().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }
        if (order.getStatus() != ORDER_STATUS_PENDING) {
            return AjaxResult.error("当前订单状态不可支付");
        }

        if (stubEnabled) {
            // Stub 模式：返回模拟支付参数
            Map<String, Object> result = new HashMap<>();
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", generateNonceStr());
            result.put("package", "prepay_id=wx_stub_" + orderNo);
            result.put("signType", "HMAC-SHA256");
            result.put("paySign", "stub_sign_" + orderNo);
            return AjaxResult.success(result);
        }

        // 真实微信支付模式
        try {
            WxPayOrderVo payVo = new WxPayOrderVo();
            payVo.setOrderNo(orderNo);

            // 将当前用户openid传入上下文（用于支付下单）
            java.util.HashMap<String, Object> contextMap = new java.util.HashMap<>();
            String openId = body.get("openId");
            if (openId == null || openId.isEmpty()) {
                // 尝试从用户信息获取
                openId = WxMiniUserContext.getCurrentUserId();
            }
            contextMap.put("openId", openId);

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

            // 注意：createPay 只返回支付参数，不能修改订单状态
            // 订单状态只能由微信支付回调验签成功后更新
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("支付创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/order/query")
    public AjaxResult queryPay(@RequestParam String outTradeNo) {
        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(outTradeNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("tradeState", order.getStatus() != null && order.getStatus() >= ORDER_STATUS_PAID ? "SUCCESS" : "NOTPAY");
        result.put("tradeType", "JSAPI");
        result.put("amount", order.getPayAmount() != null ? order.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0);
        result.put("successTime", order.getPayTime() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(order.getPayTime()) : null);
        return AjaxResult.success(result);
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
