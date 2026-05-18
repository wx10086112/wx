package com.ruoyi.mall.pay.controller;

import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wxmini/pay")
public class WxPayController extends BaseController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 创建支付订单
     */
    @PostMapping("/order/create")
    public AjaxResult createOrder(@RequestBody com.ruoyi.mall.common.bo.WxPayCreateOrderParam param) {
        String userId = WxMiniUserContext.getCurrentUserId();
        // TODO: 调用 wxPayService 创建统一下单并返回支付参数
        return AjaxResult.success();
    }

    /**
     * 查询支付订单
     */
    @GetMapping("/order/query")
    public AjaxResult queryOrder(@RequestParam String orderNo) {
        String userId = WxMiniUserContext.getCurrentUserId();
        // TODO: 调用 wxPayService 查询订单状态
        return AjaxResult.success();
    }

    /**
     * 微信支付回调通知
     */
    @PostMapping("/notify")
    public String notify(@RequestBody String xmlData) {
        // TODO: 解析微信回调通知，更新订单状态
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }
}
