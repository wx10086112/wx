package com.ruoyi.wxmini.task;

import com.ruoyi.mall.order.service.IBookingRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BookingAutoCancelTask {
    private static final Logger log = LoggerFactory.getLogger(BookingAutoCancelTask.class);

    @Resource
    private IBookingRecordService bookingRecordService;

    @Scheduled(fixedDelayString = "${wxmini.booking-auto-cancel.fixed-delay-ms:300000}",
            initialDelayString = "${wxmini.booking-auto-cancel.initial-delay-ms:60000}")
    public void cancelOverdueBookings() {
        int count = bookingRecordService.markExpiredPending();
        if (count > 0) {
            log.info("自动取消超时预约 {} 条", count);
        }
    }
}
