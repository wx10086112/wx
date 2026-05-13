package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniStaffPermissionRequestDto {

    private Long staffId;

    private String status;

    private List<String> permissions;

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
