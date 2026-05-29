package com.ruoyi.wxmini.controller;

import com.ruoyi.mall.finance.service.IPlatformTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/wxmini/transfer")
public class WxTransferNotifyController {

    private static final Logger log = LoggerFactory.getLogger(WxTransferNotifyController.class);

    @Resource
    private IPlatformTransferService platformTransferService;

    /**
     * 微信商家转账回调通知
     */
    @PostMapping("/notify")
    public WxTransferNotifyResp transferNotify(@RequestBody String notifyBody) {
        log.info("收到微信转账回调: {}", notifyBody);
        try {
            platformTransferService.handleTransferNotify(notifyBody);
            return new WxTransferNotifyResp("SUCCESS", "成功");
        } catch (Exception e) {
            log.error("处理微信转账回调异常: {}", e.getMessage(), e);
            return new WxTransferNotifyResp("FAIL", "处理失败");
        }
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
