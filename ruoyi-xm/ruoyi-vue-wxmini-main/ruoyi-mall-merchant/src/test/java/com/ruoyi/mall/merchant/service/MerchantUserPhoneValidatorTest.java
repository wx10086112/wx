package com.ruoyi.mall.merchant.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantUserPhoneValidatorTest {

    @Mock
    private MerchantUserMapper merchantUserMapper;

    @Test
    void blankPhoneShouldSkipCheck() {
        assertDoesNotThrow(() -> MerchantUserPhoneValidator.check(merchantUserMapper, 1L, "  ", null));
        verify(merchantUserMapper, never()).selectMerchantUserList(any());
    }

    @Test
    void duplicatePhoneShouldThrow() {
        MerchantUser existing = new MerchantUser();
        existing.setId(10L);
        existing.setMerchantId(1L);
        existing.setPhone("13800000000");
        when(merchantUserMapper.selectMerchantUserList(any())).thenReturn(Collections.singletonList(existing));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> MerchantUserPhoneValidator.check(merchantUserMapper, 1L, "13800000000", null));
        assertEquals("当前商家已存在使用该手机号的账号", ex.getMessage());
    }

    @Test
    void sameUserShouldBeExcluded() {
        MerchantUser existing = new MerchantUser();
        existing.setId(10L);
        existing.setMerchantId(1L);
        existing.setPhone("13800000000");
        when(merchantUserMapper.selectMerchantUserList(any())).thenReturn(Collections.singletonList(existing));

        assertDoesNotThrow(() -> MerchantUserPhoneValidator.check(merchantUserMapper, 1L, "13800000000", 10L));
    }

    @Test
    void phoneWithoutMerchantShouldThrow() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> MerchantUserPhoneValidator.check(merchantUserMapper, null, "13800000000", null));
        assertEquals("商家ID不能为空", ex.getMessage());
    }
}
