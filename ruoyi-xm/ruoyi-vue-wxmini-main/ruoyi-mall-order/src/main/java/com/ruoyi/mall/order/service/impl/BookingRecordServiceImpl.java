package com.ruoyi.mall.order.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.order.domain.BookingRecord;
import com.ruoyi.mall.order.mapper.BookingRecordMapper;
import com.ruoyi.mall.order.service.IBookingRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class BookingRecordServiceImpl implements IBookingRecordService {

    @Resource
    private BookingRecordMapper bookingRecordMapper;

    @Override
    public BookingRecord selectBookingRecordById(Long id) {
        return bookingRecordMapper.selectBookingRecordById(id);
    }

    @Override
    public BookingRecord selectBookingRecordByBookingNo(String bookingNo) {
        return bookingRecordMapper.selectBookingRecordByBookingNo(bookingNo);
    }

    @Override
    public List<BookingRecord> selectBookingRecordList(BookingRecord bookingRecord) {
        return bookingRecordMapper.selectBookingRecordList(bookingRecord);
    }

    @Override
    public int insertBookingRecord(BookingRecord bookingRecord) {
        Date now = DateUtils.getNowDate();
        bookingRecord.setCreateTime(now);
        bookingRecord.setUpdateTime(now);
        return bookingRecordMapper.insertBookingRecord(bookingRecord);
    }

    @Override
    public int updateBookingRecord(BookingRecord bookingRecord) {
        bookingRecord.setUpdateTime(DateUtils.getNowDate());
        return bookingRecordMapper.updateBookingRecord(bookingRecord);
    }

    @Override
    public boolean updateBookingStatus(String bookingNo, String status) {
        return bookingRecordMapper.updateBookingStatus(bookingNo, status, DateUtils.getNowDate()) > 0;
    }

    @Override
    public int markExpiredPendingByUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        return bookingRecordMapper.markExpiredPendingByUser(userId, DateUtils.getNowDate());
    }

    @Override
    public int markExpiredPending() {
        return bookingRecordMapper.markExpiredPending(DateUtils.getNowDate());
    }
}
