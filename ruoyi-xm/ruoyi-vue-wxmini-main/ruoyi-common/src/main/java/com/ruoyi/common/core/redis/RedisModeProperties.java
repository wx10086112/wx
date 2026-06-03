package com.ruoyi.common.core.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 模式配置。
 * 本地联调默认关闭 Redis，避免未安装 Redis 时出现无意义告警；
 * 生产环境通过配置显式开启。
 */
@Component
@ConfigurationProperties(prefix = "ruoyi.redis")
public class RedisModeProperties
{
    /**
     * 是否启用 Redis 真正连接。
     */
    private boolean enabled;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
