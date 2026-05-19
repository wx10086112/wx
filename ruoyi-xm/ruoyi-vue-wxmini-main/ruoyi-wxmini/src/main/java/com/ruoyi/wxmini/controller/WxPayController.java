package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 微信支付接口（Stub）
 * 当前返回模拟数据，待微信商户号配置后替换为真实V3支付调用
 */
@RestController
@RequestMapping("/wxmini/pay")
public class WxPayController {

    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private MerchantMapper merchantMapper;

    @PostMapping("/order/create")
    public AjaxResult createPay(@RequestBody Map<String, String> body) {
        String orderNo = body != null ? body.get("orderNo") : null;
        if (orderNo == null || orderNo.isEmpty()) {
            return AjaxResult.error("订单号不能为空");
        }

        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }
        if (order.getStatus() != ORDER_STATUS_PENDING) {
            return AjaxResult.error("当前订单状态不可支付");
        }

        // 获取商家的C端小程序AppID（用于区分不同商家的支付配置）
        Merchant merchant = merchantMapper.selectMerchantById(order.getMerchantId());
        String cAppId = merchant != null ? merchant.getCMiniAppId() : null;

        // Stub: 返回模拟支付参数
        Map<String, Object> result = new HashMap<>();
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonceStr", generateNonceStr());
        result.put("package", "prepay_id=wx_stub_" + orderNo);
        result.put("signType", "RSA");
        result.put("paySign", "stub_sign_" + orderNo);

        // Stub模式下直接将订单标记为已支付
        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setStatus(ORDER_STATUS_PAID);
        update.setPayTime(new java.util.Date());
        mallOrderMapper.updateMallOrder(update);

        return AjaxResult.success(result);
    }

    @GetMapping("/order/query")
    public AjaxResult queryPay(@RequestParam String outTradeNo) {
        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(outTradeNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }

        // Stub: 返回模拟查询结果
        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("tradeState", order.getStatus() != null && order.getStatus() >= ORDER_STATUS_PAID ? "SUCCESS" : "NOTPAY");
        result.put("tradeType", "JSAPI");
        result.put("amount", order.getPayAmount() != null ? order.getPayAmount().longValue() : 0);
        result.put("successTime", order.getPayTime() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(order.getPayTime()) : null);
        return AjaxResult.success(result);
    }

    private String generateNonceStr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
