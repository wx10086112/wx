package com.ruoyi.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisModeProperties;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysCache;

/**
 * 缓存监控
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/cache")
public class CacheController
{
    private final RedisModeProperties redisModeProperties;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    private static final List<SysCache> caches = new ArrayList<SysCache>();
    {
        caches.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "用户信息"));
        caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
        caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
        caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
    }

    public CacheController(RedisModeProperties redisModeProperties)
    {
        this.redisModeProperties = redisModeProperties;
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping()
    public AjaxResult getInfo() throws Exception
    {
        if (!canUseRedis())
        {
            Map<String, Object> localInfo = new HashMap<>(4);
            localInfo.put("info", new Properties());
            localInfo.put("dbSize", 0);
            localInfo.put("commandStats", Collections.emptyList());
            localInfo.put("localMode", true);
            return AjaxResult.success("Redis 已禁用，本地模式下不提供 Redis 监控数据", localInfo);
        }

        try
        {
            Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info());
            Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
            Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.dbSize());

            Map<String, Object> result = new HashMap<>(4);
            result.put("info", info);
            result.put("dbSize", dbSize);
            result.put("localMode", false);

            List<Map<String, String>> pieList = new ArrayList<>();
            commandStats.stringPropertyNames().forEach(key -> {
                Map<String, String> data = new HashMap<>(2);
                String property = commandStats.getProperty(key);
                data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                pieList.add(data);
            });
            result.put("commandStats", pieList);
            return AjaxResult.success(result);
        }
        catch (RedisConnectionFailureException ex)
        {
            return localMonitorResponse();
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getNames")
    public AjaxResult cache()
    {
        return AjaxResult.success(caches);
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@PathVariable String cacheName)
    {
        if (!canUseRedis())
        {
            return AjaxResult.success("Redis 已禁用，本地模式下无 Redis 键列表", Collections.emptySet());
        }
        try
        {
            Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
            return AjaxResult.success(new TreeSet<>(cacheKeys));
        }
        catch (RedisConnectionFailureException ex)
        {
            return AjaxResult.success("Redis 当前不可用，无法读取键列表", Collections.emptySet());
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey)
    {
        if (!canUseRedis())
        {
            return AjaxResult.success("Redis 已禁用，本地模式下无 Redis 键值详情", new SysCache(cacheName, cacheKey, null));
        }
        try
        {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
            return AjaxResult.success(sysCache);
        }
        catch (RedisConnectionFailureException ex)
        {
            return AjaxResult.success("Redis 当前不可用，无法读取键值详情", new SysCache(cacheName, cacheKey, null));
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@PathVariable String cacheName)
    {
        if (!canUseRedis())
        {
            return AjaxResult.success("Redis 已禁用，无需清理 Redis 缓存");
        }
        try
        {
            Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
            redisTemplate.delete(cacheKeys);
            return AjaxResult.success();
        }
        catch (RedisConnectionFailureException ex)
        {
            return AjaxResult.success("Redis 当前不可用，无法执行缓存清理");
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheKey)
    {
        if (!canUseRedis())
        {
            return AjaxResult.success("Redis 已禁用，无需清理 Redis 缓存");
        }
        try
        {
            redisTemplate.delete(cacheKey);
            return AjaxResult.success();
        }
        catch (RedisConnectionFailureException ex)
        {
            return AjaxResult.success("Redis 当前不可用，无法执行缓存清理");
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll()
    {
        if (!canUseRedis())
        {
            return AjaxResult.success("Redis 已禁用，无需清理 Redis 缓存");
        }
        try
        {
            Collection<String> cacheKeys = redisTemplate.keys("*");
            redisTemplate.delete(cacheKeys);
            return AjaxResult.success();
        }
        catch (RedisConnectionFailureException ex)
        {
            return AjaxResult.success("Redis 当前不可用，无法执行缓存清理");
        }
    }

    private boolean canUseRedis()
    {
        return redisModeProperties.isEnabled() && redisTemplate != null;
    }

    private AjaxResult localMonitorResponse()
    {
        Map<String, Object> localInfo = new HashMap<>(4);
        localInfo.put("info", new Properties());
        localInfo.put("dbSize", 0);
        localInfo.put("commandStats", Collections.emptyList());
        localInfo.put("localMode", true);
        return AjaxResult.success("Redis 当前不可用，已切换为本地模式监控占位数据", localInfo);
    }
}
