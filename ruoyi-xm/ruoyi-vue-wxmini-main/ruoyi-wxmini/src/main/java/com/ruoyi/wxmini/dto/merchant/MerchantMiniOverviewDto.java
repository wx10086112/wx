package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniOverviewDto {

    private MerchantMiniStaffUserDto staffUser;

    private MerchantMiniStoreDto storeInfo;

    private MerchantMiniWorkbenchStatsDto stats;

    private List<MerchantMiniOrderDto> pendingOrderList;

    public MerchantMiniStaffUserDto getStaffUser() {
        return staffUser;
    }

    public void setStaffUser(MerchantMiniStaffUserDto staffUser) {
        this.staffUser = staffUser;
    }

    public MerchantMiniStoreDto getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(MerchantMiniStoreDto storeInfo) {
        this.storeInfo = storeInfo;
    }

    public MerchantMiniWorkbenchStatsDto getStats() {
        return stats;
    }

    public void setStats(MerchantMiniWorkbenchStatsDto stats) {
        this.stats = stats;
    }

    public List<MerchantMiniOrderDto> getPendingOrderList() {
        return pendingOrderList;
    }

    public void setPendingOrderList(List<MerchantMiniOrderDto> pendingOrderList) {
        this.pendingOrderList = pendingOrderList;
    }
}
