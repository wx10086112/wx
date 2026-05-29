package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * C端小程序退款申请接口
 */
@RestController
@RequestMapping("/wxmini/refund")
public class WxRefundController {

    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_COMPLETED = 2;
    private static final int ORDER_STATUS_REFUNDED = 3;

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private RefundRecordMapper refundRecordMapper;

    /**
     * 提交退款申请
     * POST /wxmini/refund/apply
     */
    @PostMapping("/apply")
    public AjaxResult applyRefund(@RequestBody Map<String, String> body) {
        String orderNo = body != null ? body.get("orderNo") : null;
        String refundReason = body != null ? body.get("refundReason") : null;

        if (orderNo == null || orderNo.isEmpty()) {
            return AjaxResult.error("订单号不能为空");
        }

        String userId = WxMiniUserContext.getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error("请先登录");
        }

        // 校验订单
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getUserId().toString().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }
        if (order.getStatus() == null || (order.getStatus() != ORDER_STATUS_PAID && order.getStatus() != ORDER_STATUS_COMPLETED)) {
            return AjaxResult.error("当前订单状态不可退款");
        }
        if (order.getStatus() != null && order.getStatus() >= ORDER_STATUS_REFUNDED) {
            return AjaxResult.error("订单已退款");
        }

        // 幂等：检查是否已有待审核的退款记录
        RefundRecord existQuery = new RefundRecord();
        existQuery.setOrderNo(orderNo);
        List<RefundRecord> existing = refundRecordMapper.selectRefundRecordList(existQuery);
        if (existing != null) {
            for (RefundRecord r : existing) {
                if (r.getStatus() != null && (r.getStatus() == 1 || r.getStatus() == 2)) {
                    return AjaxResult.error("该订单已有退款申请在处理中");
                }
            }
        }

        // 创建退款记录
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setOrderNo(orderNo);
        refundRecord.setRefundNo(generateRefundNo());
        refundRecord.setMerchantId(order.getMerchantId());
        refundRecord.setUserId(order.getUserId());
        refundRecord.setRefundAmount(order.getPayAmount());
        refundRecord.setRefundReason(refundReason != null ? refundReason : "用户申请退款");
        refundRecord.setRefundType(1); // 1=全额退款
        refundRecord.setStatus(1); // 1=待审核

        refundRecordMapper.insertRefundRecord(refundRecord);

        return AjaxResult.success("退款申请已提交，请等待审核");
    }

    /**
     * 查询退款记录列表
     * GET /wxmini/refund/list
     */
    @GetMapping("/list")
    public AjaxResult listRefund() {
        String userId = WxMiniUserContext.getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error("请先登录");
        }

        RefundRecord query = new RefundRecord();
        query.setUserId(Long.valueOf(userId));
        List<RefundRecord> list = refundRecordMapper.selectRefundRecordList(query);

        return AjaxResult.success(list);
    }

    private String generateRefundNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "RF" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
