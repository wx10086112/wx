package com.ruoyi.wxmini.controller;

import com.ruoyi.mall.order.domain.BookingService;
import com.ruoyi.mall.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WxBookingControllerPriceTest {

    @Test
    void linkedBookingServiceUsesCurrentProductPriceForNewBooking() {
        BookingService service = new BookingService();
        service.setProductId(101L);
        service.setServicePrice(new BigDecimal("9.90"));
        Product product = new Product();
        product.setPrice(new BigDecimal("12.80"));

        BigDecimal price = ReflectionTestUtils.invokeMethod(new WxBookingController(),
                "resolveBookingServicePrice", service, product);

        assertEquals(new BigDecimal("12.80"), price);
    }

    @Test
    void independentBookingServiceKeepsItsOwnConfiguredPrice() {
        BookingService service = new BookingService();
        service.setServicePrice(new BigDecimal("9.90"));

        BigDecimal price = ReflectionTestUtils.invokeMethod(new WxBookingController(),
                "resolveBookingServicePrice", service, null);

        assertEquals(new BigDecimal("9.90"), price);
    }
}
