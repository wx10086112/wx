package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniLoginResponseDto {

    private String token;

    private MerchantMiniStaffUserDto staffUser;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MerchantMiniStaffUserDto getStaffUser() {
        return staffUser;
    }

    public void setStaffUser(MerchantMiniStaffUserDto staffUser) {
        this.staffUser = staffUser;
    }
}
