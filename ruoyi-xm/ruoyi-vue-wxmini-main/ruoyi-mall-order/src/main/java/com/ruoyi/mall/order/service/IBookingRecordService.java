package com.ruoyi.mall.order.service;

import com.ruoyi.mall.order.domain.BookingRecord;

import java.util.List;

public interface IBookingRecordService {

    BookingRecord selectBookingRecordById(Long id);

    BookingRecord selectBookingRecordByBookingNo(String bookingNo);

    List<BookingRecord> selectBookingRecordList(BookingRecord bookingRecord);

    int insertBookingRecord(BookingRecord bookingRecord);

    int updateBookingRecord(BookingRecord bookingRecord);

    boolean updateBookingStatus(String bookingNo, String status);

    int markExpiredPendingByUser(Long userId);

    int markExpiredPending();
}
