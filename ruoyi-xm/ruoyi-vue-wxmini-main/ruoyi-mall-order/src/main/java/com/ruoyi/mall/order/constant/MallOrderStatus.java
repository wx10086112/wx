package com.ruoyi.mall.order.constant;

public final class MallOrderStatus {

    public static final int PENDING = 0;
    public static final int PAID = 1;
    public static final int USED = 2;
    public static final int COMPLETED = 3;
    public static final int REFUNDED = 4;
    public static final int CANCELLED = 5;

    private MallOrderStatus() {
    }

    public static boolean isPaidState(Integer status) {
        return status != null
                && (status == PAID || status == USED || status == COMPLETED || status == REFUNDED);
    }

    public static boolean isRefundable(Integer status) {
        return status != null
                && (status == PAID || status == USED || status == COMPLETED);
    }
}
