package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wxmini/refund")
public class WxRefundController {

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private IUserInfoService userInfoService;

    @PostMapping("/apply")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult applyRefund(@RequestBody Map<String, String> body) {
        String orderNo = body != null ? body.get("orderNo") : null;
        String refundReason = body != null ? body.get("refundReason") : null;

        if (StringUtils.isBlank(orderNo)) {
            return AjaxResult.error("订单号不能为空");
        }

        String userId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(userId)) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderMapper.selectMallOrderByOrderNoForUpdate(orderNo);
        UserInfo currentUser = resolveUserInfo(userId);
        if (order == null || !isCurrentUserOrder(order, currentUser)) {
            return AjaxResult.error("订单不存在");
        }
        AjaxResult tenantCheck = checkOrderTenant(order);
        if (tenantCheck != null) {
            return tenantCheck;
        }
        if (order.getStatus() != null && order.getStatus() == MallOrderStatus.REFUNDED) {
            return AjaxResult.error("订单已退款");
        }
        if (!MallOrderStatus.isRefundable(order.getStatus())) {
            return AjaxResult.error("当前订单状态不可退款");
        }

        if (refundRecordMapper.countActiveRefundByOrderNo(orderNo) > 0) {
            return AjaxResult.error("该订单已有退款申请在处理中");
        }

        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setOrderNo(orderNo);
        refundRecord.setRefundNo(generateRefundNo());
        refundRecord.setMerchantId(order.getMerchantId());
        refundRecord.setUserId(order.getUserId());
        refundRecord.setRefundAmount(order.getPayAmount());
        refundRecord.setRefundReason(normalizeRefundReason(refundReason));
        refundRecord.setRefundType(1);
        refundRecord.setStatus(RefundRecord.STATUS_PENDING);
        refundRecordMapper.insertRefundRecord(refundRecord);

        return AjaxResult.success("退款申请已提交，请等待审核");
    }

    @GetMapping("/list")
    public AjaxResult listRefund() {
        String userId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(userId)) {
            return AjaxResult.error("请先登录");
        }
        Long merchantId = WxMiniUserContext.getCurrentMerchantId();
        if (merchantId == null) {
            return AjaxResult.error("当前小程序登录态缺少商户信息");
        }

        RefundRecord query = new RefundRecord();
        UserInfo currentUser = resolveUserInfo(userId);
        if (currentUser == null || currentUser.getId() == null) {
            return AjaxResult.error("invalid user");
        }
        query.setUserId(currentUser.getId());
        query.setMerchantId(merchantId);
        List<RefundRecord> list = refundRecordMapper.selectRefundRecordList(query);
        return AjaxResult.success(list);
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

    private String normalizeRefundReason(String refundReason) {
        String normalized = StringUtils.isNotBlank(refundReason) ? refundReason.trim() : "用户申请退款";
        return normalized.length() > 200 ? normalized.substring(0, 200) : normalized;
    }

    private String generateRefundNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "RF" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
