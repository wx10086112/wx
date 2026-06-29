package com.ruoyi.mall.order.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.common.util.WriteOffCodeGenerator;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.WriteOffRecord;
import com.ruoyi.mall.order.event.OrderCompletedEvent;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import com.ruoyi.mall.order.mapper.WriteOffRecordMapper;
import com.ruoyi.mall.order.vo.WriteOffResultVO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class WriteOffServiceImpl implements IWriteOffService {

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private WriteOffRecordMapper writeOffRecordMapper;
    @Resource
    private WriteOffCodeGenerator writeOffCodeGenerator;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private IMallOrderService mallOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WriteOffResultVO writeOff(String code, Long merchantId, Long operatorId, String operatorName) {
        String normalizedCode = writeOffCodeGenerator.normalize(code);
        if (!writeOffCodeGenerator.isValid(normalizedCode)) {
            throw new ServiceException("核销码格式不正确");
        }

        MallOrder order = mallOrderMapper.selectOrderByWriteOffCode(normalizedCode);
        if (order == null) {
            throw new ServiceException("核销码不存在");
        }

        if (!order.getMerchantId().equals(merchantId)) {
            throw new ServiceException("该订单不属于当前商家");
        }

        if (order.getStatus() == null || order.getStatus() != MallOrderStatus.PAID) {
            throw new ServiceException("订单状态异常，无法核销");
        }

        if (order.getWriteOffStatus() != null && order.getWriteOffStatus() == 1) {
            throw new ServiceException("该订单已核销，请勿重复操作");
        }

        if (order.getPayTime() != null && order.getValidDays() != null && order.getValidDays() > 0) {
            long expireMillis = order.getPayTime().getTime() + ((long) order.getValidDays()) * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() > expireMillis) {
                throw new ServiceException("该订单已过期，无法核销");
            }
        }

        Date now = new Date();
        int affectedRows = mallOrderMapper.markOrderWriteOffCompleted(order.getId(), merchantId, operatorId, now);
        if (affectedRows == 0) {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }

        mallOrderService.recordOrderStatusHistory(order, order.getStatus(), MallOrderStatus.COMPLETED,
                "WRITE_OFF_COMPLETE", "MERCHANT_MINI", operatorId, operatorName, null);

        WriteOffRecord record = new WriteOffRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setWriteOffCode(normalizedCode);
        record.setMerchantId(merchantId);
        record.setStoreId(order.getStoreId());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setWriteOffType(1);
        record.setWriteOffTime(now);
        record.setProductAmount(order.getPayAmount());
        record.setStatus(1);
        writeOffRecordMapper.insertWriteOffRecord(record);

        String title = null;
        List<OrderItem> items = orderItemMapper.selectOrderItemByOrderNo(order.getOrderNo());
        if (items != null && !items.isEmpty() && items.get(0).getProductName() != null) {
            title = items.get(0).getProductName();
        }
        eventPublisher.publishEvent(new OrderCompletedEvent(this,
                order.getOrderNo(), merchantId, null, order.getStoreId(), order.getPayAmount(), title));

        WriteOffResultVO vo = new WriteOffResultVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setPayAmount(order.getPayAmount());
        vo.setWriteOffTime(now);
        vo.setOperatorName(operatorName);
        return vo;
    }
}
