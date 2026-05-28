package com.ruoyi.mall.order.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.common.util.WriteOffCodeGenerator;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WriteOffResultVO writeOff(String code, Long merchantId, Long operatorId, String operatorName) {
        // 1. 校验核销码格式
        if (!writeOffCodeGenerator.isValid(code)) {
            throw new ServiceException("核销码格式不正确");
        }

        // 2. 查询订单
        MallOrder order = mallOrderMapper.selectOrderByWriteOffCode(code);
        if (order == null) {
            throw new ServiceException("核销码不存在");
        }

        // 3. 校验商家归属
        if (!order.getMerchantId().equals(merchantId)) {
            throw new ServiceException("该订单不属于当前商家");
        }

        // 4. 校验订单状态（必须已支付）
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new ServiceException("订单状态异常，无法核销");
        }

        // 5. 校验是否已核销
        if (order.getWriteOffStatus() != null && order.getWriteOffStatus() == 1) {
            throw new ServiceException("该订单已核销，请勿重复操作");
        }

        // 6. 校验是否过期（商品有效天数）
        if (order.getPayTime() != null && order.getValidDays() != null && order.getValidDays() > 0) {
            long expireMillis = order.getPayTime().getTime() + ((long) order.getValidDays()) * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() > expireMillis) {
                throw new ServiceException("该订单已过期，无法核销");
            }
        }

        // 7. 更新订单状态
        Date now = new Date();
        order.setWriteOffStatus(1);
        order.setWriteOffTime(now);
        order.setWriteOffUserId(operatorId);
        order.setStatus(3); // 已完成
        order.setUseTime(now);
        mallOrderMapper.updateMallOrder(order);

        // 8. 写入核销记录
        WriteOffRecord record = new WriteOffRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setWriteOffCode(code);
        record.setMerchantId(merchantId);
        record.setStoreId(order.getStoreId());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setWriteOffType(1); // 扫码核销
        record.setWriteOffTime(now);
        record.setProductAmount(order.getPayAmount());
        record.setStatus(1);
        writeOffRecordMapper.insertWriteOffRecord(record);

        // 9. 发布订单完成事件，触发结算记录生成
        String title = null;
        List<OrderItem> items = orderItemMapper.selectOrderItemByOrderNo(order.getOrderNo());
        if (items != null && !items.isEmpty() && items.get(0).getProductName() != null) {
            title = items.get(0).getProductName();
        }
        eventPublisher.publishEvent(new OrderCompletedEvent(this,
                order.getOrderNo(), merchantId, null, order.getStoreId(), order.getPayAmount(), title));

        // 10. 返回结果
        WriteOffResultVO vo = new WriteOffResultVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setPayAmount(order.getPayAmount());
        vo.setWriteOffTime(now);
        vo.setOperatorName(operatorName);
        return vo;
    }
}
