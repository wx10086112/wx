package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.BookingRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface BookingRecordMapper {

    BookingRecord selectBookingRecordById(Long id);

    BookingRecord selectBookingRecordByBookingNo(String bookingNo);

    List<BookingRecord> selectBookingRecordList(BookingRecord bookingRecord);

    int insertBookingRecord(BookingRecord bookingRecord);

    int updateBookingRecord(BookingRecord bookingRecord);

    int updateBookingStatus(@Param("bookingNo") String bookingNo,
                            @Param("status") String status,
                            @Param("statusTime") Date statusTime);

    int markExpiredPendingByUser(@Param("userId") Long userId, @Param("expireTime") Date expireTime);

    int markExpiredPending(@Param("expireTime") Date expireTime);
}
