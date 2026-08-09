package com.ruoyi.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.domain.entity.SysUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SysUserJsonTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordCanBeReadFromRequestButIsNotWrittenToResponse() throws Exception
    {
        SysUser user = objectMapper.readValue("{\"userName\":\"merchant\",\"password\":\"secret123\"}", SysUser.class);

        assertEquals("secret123", user.getPassword());
        assertFalse(objectMapper.writeValueAsString(user).contains("password"));
    }
}
