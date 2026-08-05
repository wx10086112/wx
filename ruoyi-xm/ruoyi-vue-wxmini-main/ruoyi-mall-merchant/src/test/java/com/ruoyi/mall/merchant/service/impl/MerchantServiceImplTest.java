package com.ruoyi.mall.merchant.service.impl;

import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantStore;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.merchant.mapper.MerchantStoreMapper;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private MerchantUserMapper merchantUserMapper;
    @Mock
    private MerchantStoreMapper merchantStoreMapper;

    private MerchantServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MerchantServiceImpl();
        ReflectionTestUtils.setField(service, "merchantMapper", merchantMapper);
        ReflectionTestUtils.setField(service, "merchantUserMapper", merchantUserMapper);
        ReflectionTestUtils.setField(service, "merchantStoreMapper", merchantStoreMapper);
    }

    @Test
    void newMerchantCreatesPrimaryStoreInSameServiceFlow() {
        Merchant merchant = merchant(8L);
        MerchantUser owner = new MerchantUser();
        owner.setRole("owner");
        when(merchantMapper.insertMerchant(merchant)).thenReturn(1);
        when(merchantUserMapper.selectMerchantUserByMerchantId(8L))
                .thenReturn(Collections.singletonList(owner));
        when(merchantStoreMapper.insertMerchantStore(any(MerchantStore.class))).thenAnswer(invocation -> {
            MerchantStore store = invocation.getArgument(0);
            store.setId(81L);
            return 1;
        });
        when(merchantStoreMapper.countMerchantStoreByMerchantId(8L)).thenReturn(1);

        assertEquals(1, service.insertMerchant(merchant));

        ArgumentCaptor<MerchantStore> storeCaptor = ArgumentCaptor.forClass(MerchantStore.class);
        verify(merchantStoreMapper).insertMerchantStore(storeCaptor.capture());
        MerchantStore store = storeCaptor.getValue();
        assertEquals(8L, store.getMerchantId());
        assertEquals("测试餐厅", store.getName());
        assertEquals(1, store.getStatus());
        assertEquals(1, store.getIsMain());
        verifyStoreCountUpdate(8L, 1);
    }

    @Test
    void existingMerchantWithoutStoreCanCreatePrimaryStore() {
        Merchant merchant = merchant(2L);
        MerchantStore input = new MerchantStore();
        input.setName("秦月楼泡馍小炒烧烤");
        input.setLongitude(new BigDecimal("108.9530980"));
        input.setLatitude(new BigDecimal("34.2778000"));
        when(merchantMapper.selectMerchantIdForUpdate(2L)).thenReturn(2L);
        when(merchantMapper.selectMerchantById(2L)).thenReturn(merchant);
        when(merchantStoreMapper.countMerchantStoreByMerchantId(2L)).thenReturn(0, 1);
        when(merchantStoreMapper.insertMerchantStore(input)).thenAnswer(invocation -> {
            input.setId(21L);
            return 1;
        });
        when(merchantStoreMapper.selectMerchantStoreById(21L)).thenReturn(input);

        MerchantStore saved = service.createPrimaryStore(2L, input);

        assertEquals(21L, saved.getId());
        assertEquals(2L, saved.getMerchantId());
        assertEquals(1, saved.getIsMain());
        verifyStoreCountUpdate(2L, 1);
    }

    @Test
    void secondStoreIsRejectedDuringFirstPhase() {
        when(merchantMapper.selectMerchantIdForUpdate(1L)).thenReturn(1L);
        when(merchantMapper.selectMerchantById(1L)).thenReturn(merchant(1L));
        when(merchantStoreMapper.countMerchantStoreByMerchantId(1L)).thenReturn(1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createPrimaryStore(1L, new MerchantStore()));

        assertTrue(error.getMessage().contains("只支持一家主门店"));
        verify(merchantStoreMapper, never()).insertMerchantStore(any(MerchantStore.class));
    }

    @Test
    void primaryStoreUpdateKeepsIdentifiersAndAllowsClearingCoordinates() {
        MerchantStore existing = new MerchantStore();
        existing.setId(11L);
        existing.setMerchantId(1L);
        existing.setName("旧门店");
        MerchantStore input = new MerchantStore();
        input.setName("新门店");
        input.setStatus(1);
        when(merchantMapper.selectMerchantById(1L)).thenReturn(merchant(1L));
        when(merchantStoreMapper.selectMerchantStoreByMerchantId(1L))
                .thenReturn(Collections.singletonList(existing));
        when(merchantStoreMapper.updatePrimaryMerchantStore(input)).thenReturn(1);
        when(merchantStoreMapper.countMerchantStoreByMerchantId(1L)).thenReturn(1);
        when(merchantStoreMapper.selectMerchantStoreById(11L)).thenReturn(input);

        MerchantStore saved = service.updatePrimaryStore(1L, input);

        assertEquals(11L, saved.getId());
        assertEquals(1L, saved.getMerchantId());
        assertEquals(1, saved.getIsMain());
        verify(merchantStoreMapper).updatePrimaryMerchantStore(input);
    }

    private Merchant merchant(Long id) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setName("测试餐厅");
        merchant.setContact("测试联系人");
        merchant.setPhone("13800000000");
        merchant.setAddress("测试地址");
        merchant.setBusinessHours("10:00-22:00");
        return merchant;
    }

    private void verifyStoreCountUpdate(Long merchantId, int expectedCount) {
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantMapper).updateMerchant(merchantCaptor.capture());
        assertEquals(merchantId, merchantCaptor.getValue().getId());
        assertEquals(expectedCount, merchantCaptor.getValue().getStoreCount());
    }
}
