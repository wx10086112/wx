package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.redis.RedisModeProperties;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * 限流处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class RateLimiterAspect
{
    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final RedisModeProperties redisModeProperties;

    private final Map<String, LocalRateLimitWindow> localWindows = new ConcurrentHashMap<>();

    private RedisTemplate<Object, Object> redisTemplate;

    private RedisScript<Long> limitScript;

    public RateLimiterAspect(RedisModeProperties redisModeProperties)
    {
        this.redisModeProperties = redisModeProperties;
    }

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    @Autowired(required = false)
    public void setLimitScript(RedisScript<Long> limitScript)
    {
        this.limitScript = limitScript;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        String combineKey = getCombineKey(rateLimiter, point);

        if (shouldUseRedis())
        {
            List<Object> keys = Collections.singletonList(combineKey);
            try
            {
                Long number = redisTemplate.execute(limitScript, keys, count, time);
                if (StringUtils.isNull(number) || number.intValue() > count)
                {
                    throw new ServiceException("访问过于频繁，请稍候再试");
                }
                log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
                return;
            }
            catch (ServiceException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                log.warn("Redis 限流不可用，切换为本地限流模式：{}", e.getMessage());
            }
        }

        int current = acquireLocalCount(combineKey, time);
        if (current > count)
        {
            throw new ServiceException("访问过于频繁，请稍候再试");
        }
        log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, current, combineKey);
    }

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr()).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }

    private boolean shouldUseRedis()
    {
        return redisModeProperties.isEnabled() && redisTemplate != null && limitScript != null;
    }

    private int acquireLocalCount(String key, int timeSeconds)
    {
        long now = System.currentTimeMillis();
        long ttlMillis = TimeUnit.SECONDS.toMillis(timeSeconds);
        LocalRateLimitWindow window = localWindows.compute(key, (cacheKey, existing) -> {
            if (existing == null || existing.isExpired(now))
            {
                return new LocalRateLimitWindow(now + ttlMillis, 1);
            }
            existing.increment();
            return existing;
        });
        cleanupExpiredWindows(now);
        return window.getCount();
    }

    private void cleanupExpiredWindows(long now)
    {
        localWindows.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private static class LocalRateLimitWindow
    {
        private final long expireAt;

        private int count;

        LocalRateLimitWindow(long expireAt, int count)
        {
            this.expireAt = expireAt;
            this.count = count;
        }

        boolean isExpired(long now)
        {
            return now >= expireAt;
        }

        void increment()
        {
            count++;
        }

        int getCount()
        {
            return count;
        }
    }
}
