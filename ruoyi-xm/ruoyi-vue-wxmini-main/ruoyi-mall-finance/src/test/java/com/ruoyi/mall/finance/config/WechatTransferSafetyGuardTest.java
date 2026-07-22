package com.ruoyi.mall.finance.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WechatTransferSafetyGuardTest {

    @Test
    void rejectsEnabledTaskWhenTransferIsDisabled() {
        WechatTransferSafetyGuard guard = guard(false, false, true, null);

        assertThrows(IllegalStateException.class, guard::validateStartupConfiguration);
    }

    @Test
    void rejectsLiveTransferWithoutConfirmation() {
        WechatTransferSafetyGuard guard = guard(false, true, false, null);

        assertThrows(IllegalStateException.class, guard::validateStartupConfiguration);
        assertThrows(IllegalStateException.class, guard::ensureTransferAllowed);
    }

    @Test
    void allowsStubTransferWithoutLiveConfirmation() {
        WechatTransferSafetyGuard guard = guard(true, true, true, null);

        assertDoesNotThrow(guard::validateStartupConfiguration);
        assertDoesNotThrow(guard::ensureTransferAllowed);
    }

    @Test
    void allowsLiveTransferWithExactConfirmation() {
        WechatTransferSafetyGuard guard = guard(false, true, true,
                WechatTransferSafetyGuard.LIVE_TRANSFER_CONFIRMATION);

        assertDoesNotThrow(guard::validateStartupConfiguration);
        assertDoesNotThrow(guard::ensureTransferAllowed);
    }

    private WechatTransferSafetyGuard guard(boolean stubEnabled, boolean transferEnabled,
                                             boolean transferTaskEnabled, String confirmation) {
        WechatTransferSafetyGuard guard = new WechatTransferSafetyGuard();
        ReflectionTestUtils.setField(guard, "stubEnabled", stubEnabled);
        ReflectionTestUtils.setField(guard, "transferEnabled", transferEnabled);
        ReflectionTestUtils.setField(guard, "transferTaskEnabled", transferTaskEnabled);
        ReflectionTestUtils.setField(guard, "transferLiveConfirmation", confirmation);
        return guard;
    }
}
