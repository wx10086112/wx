package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.BookingService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BookingServiceMapper {

    BookingService selectBookingServiceById(Long id);

    BookingService selectBookingServiceByProductId(Long productId);

    List<BookingService> selectActiveBookingServiceList(@Param("merchantId") Long merchantId,
                                                        @Param("keyword") String keyword);

    Long selectBookingServiceVersion(@Param("merchantId") Long merchantId);
}
