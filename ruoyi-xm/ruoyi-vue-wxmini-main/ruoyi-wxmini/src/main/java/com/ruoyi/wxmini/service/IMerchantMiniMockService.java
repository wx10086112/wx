package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.dto.merchant.MerchantMiniFinanceOverviewDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniLoginResponseDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniOrderDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniOverviewDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffPermissionRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffUserDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStoreDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniUploadResultDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniVerifyRecordDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniWithdrawRecordDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMerchantMiniMockService {

    MerchantMiniLoginResponseDto login(String username, String password);

    MerchantMiniOverviewDto getWorkbenchOverview(String currentUserId);

    List<MerchantMiniOrderDto> listOrders(String status);

    MerchantMiniOrderDto getOrderDetail(String orderNo);

    MerchantMiniOrderDto writeOff(String code, String currentUserId);

    // 订单操作
    MerchantMiniOrderDto acceptOrder(String orderNo);

    MerchantMiniOrderDto rejectOrder(String orderNo, String reason);

    MerchantMiniOrderDto cancelOrder(String orderNo, String reason);

    MerchantMiniOrderDto approveRefund(String orderNo);

    MerchantMiniOrderDto rejectRefund(String orderNo, String reason);

    List<MerchantMiniVerifyRecordDto> listVerifyRecords(String status);

    List<MerchantMiniGoodsDto> listGoods(String status);

    MerchantMiniGoodsDto saveGoods(MerchantMiniGoodsDto goodsDto);

    MerchantMiniGoodsDto updateGoodsStatus(Long goodsId, String status);

    MerchantMiniUploadResultDto uploadGoodsImage(String fileName, Long size);

    MerchantMiniUploadResultDto uploadGoodsImage(MultipartFile file);

    int batchUpdateGoodsStatus(List<Long> goodsIds, String status);

    MerchantMiniStoreDto getStoreProfile();

    MerchantMiniStoreDto updateStoreProfile(MerchantMiniStoreDto storeDto);

    List<MerchantMiniStaffUserDto> listStaff();

    // 员工增删改
    MerchantMiniStaffUserDto addStaff(MerchantMiniStaffRequestDto requestDto);

    MerchantMiniStaffUserDto updateStaff(MerchantMiniStaffRequestDto requestDto);

    List<MerchantMiniStaffUserDto> updateStaffPermission(MerchantMiniStaffPermissionRequestDto requestDto);

    MerchantMiniFinanceOverviewDto getFinanceOverview();

    MerchantMiniWithdrawRecordDto applyWithdraw(Long amount);
}
