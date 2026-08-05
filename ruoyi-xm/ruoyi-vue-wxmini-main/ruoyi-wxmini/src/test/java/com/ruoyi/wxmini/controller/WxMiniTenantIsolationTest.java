package com.ruoyi.wxmini.controller;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.mall.common.filter.WxMiniJwtFilter;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.order.domain.BookingRecord;
import com.ruoyi.mall.order.domain.MallOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WxMiniTenantIsolationTest {

    @AfterEach
    void clearContext() {
        WxMiniUserContext.clear();
    }

    @Test
    void publicBrowseRejectsRequestWithoutRegisteredMiniAppContext() throws Exception {
        WxMiniJwtFilter filter = new WxMiniJwtFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wxmini/groupon/list");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, filterChain);

        assertEquals(403, response.getStatus());
        assertEquals(null, filterChain.getRequest());
    }

    @Test
    void orderMustBelongToCurrentMerchant() {
        WxMiniUserContext.setCurrentUserContext(authContext(101L));
        MallOrder ownOrder = new MallOrder();
        ownOrder.setMerchantId(101L);
        MallOrder otherOrder = new MallOrder();
        otherOrder.setMerchantId(202L);

        WxOrderController controller = new WxOrderController();

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(controller, "isCurrentMerchantOrder", ownOrder));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(controller, "isCurrentMerchantOrder", otherOrder));
    }

    @Test
    void bookingMustBelongToCurrentMerchant() {
        WxMiniUserContext.setCurrentUserContext(authContext(101L));
        BookingRecord ownBooking = new BookingRecord();
        ownBooking.setMerchantId(101L);
        BookingRecord otherBooking = new BookingRecord();
        otherBooking.setMerchantId(202L);

        WxBookingController controller = new WxBookingController();

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(controller, "isCurrentMerchantBooking", ownBooking));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(controller, "isCurrentMerchantBooking", otherBooking));
    }

    private WxMiniAuthContext authContext(Long merchantId) {
        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId("test-user");
        authContext.setMerchantId(merchantId);
        return authContext;
    }
}
