package com.ruoyi.wxmini.controller;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayTransferBatchesNotifyV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.finance.service.IPlatformTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

@RestController
@RequestMapping("/wxmini/transfer")
public class WxTransferNotifyController {

    private static final Logger log = LoggerFactory.getLogger(WxTransferNotifyController.class);

    @Resource
    private IPlatformTransferService platformTransferService;
    @Autowired(required = false)
    private WxPayService wxPayService;

    /**
     * 微信商家转账回调通知
     */
    @PostMapping("/notify")
    public WxTransferNotifyResp transferNotify(HttpServletRequest request) {
        try {
            if (wxPayService == null) {
                log.warn("WxPayService未配置，拒绝处理转账回调");
                return new WxTransferNotifyResp("FAIL", "微信支付未配置");
            }
            String notifyBody = readRequestBody(request);
            SignatureHeader header = new SignatureHeader();
            header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
            header.setNonce(request.getHeader("Wechatpay-Nonce"));
            header.setSignature(request.getHeader("Wechatpay-Signature"));
            header.setSerial(request.getHeader("Wechatpay-Serial"));

            WxPayTransferBatchesNotifyV3Result result =
                    wxPayService.parseTransferBatchesNotifyV3Result(notifyBody, header);
            platformTransferService.handleTransferNotify(result.getResult(), notifyBody);
            return new WxTransferNotifyResp("SUCCESS", "成功");
        } catch (Exception e) {
            log.error("处理微信转账回调异常: {}", e.getMessage(), e);
            return new WxTransferNotifyResp("FAIL", "处理失败");
        }
    }

    private String readRequestBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public static class WxTransferNotifyResp {
        private String code;
        private String message;

        public WxTransferNotifyResp(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
