package com.ruoyi.wxmini.dto.wx;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class WxOrderCreateRequestDto {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    private String phone;
    private Long couponId;
    private List<OrderItemInput> items;

    public static class OrderItemInput {
        private Long productId;
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public List<OrderItemInput> getItems() { return items; }
    public void setItems(List<OrderItemInput> items) { this.items = items; }
}
