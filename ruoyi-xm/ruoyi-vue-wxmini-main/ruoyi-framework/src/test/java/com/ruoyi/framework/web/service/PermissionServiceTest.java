package com.ruoyi.framework.web.service;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest
{
    private final PermissionService permissionService = new PermissionService();

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void builtInSuperAdministratorIsAccepted()
    {
        authenticate(1L);

        assertTrue(permissionService.isAdmin());
    }

    @Test
    void otherUsersAreRejected()
    {
        authenticate(2L);

        assertFalse(permissionService.isAdmin());
    }

    @Test
    void missingAuthenticationIsRejected()
    {
        assertFalse(permissionService.isAdmin());
    }

    private void authenticate(Long userId)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("test-user");
        LoginUser loginUser = new LoginUser(userId, null, user, Collections.emptySet());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
