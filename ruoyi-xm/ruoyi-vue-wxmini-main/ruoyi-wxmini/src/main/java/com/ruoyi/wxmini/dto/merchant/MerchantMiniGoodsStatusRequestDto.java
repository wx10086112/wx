package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniGoodsStatusRequestDto {

    private Long goodsId;

    private String status;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
