package com.ruoyi.mall.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import com.ruoyi.common.core.redis.RedisModeProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 核销码生成器
 * 格式: LY202605250001 (前缀+日期8位+序列号4位)
 */
@Component
public class WriteOffCodeGenerator
{
    private final RedisModeProperties redisModeProperties;

    private final Map<String, AtomicInteger> localSequences = new ConcurrentHashMap<>();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CODE_PREFIX = "LY";
    private static final String REDIS_KEY_PREFIX = "write_off:code:seq:";
    private static final int SEQ_LENGTH = 4;

    public WriteOffCodeGenerator(RedisModeProperties redisModeProperties)
    {
        this.redisModeProperties = redisModeProperties;
    }

    /**
     * 生成核销码
     * @return 如 LY202605250001
     */
    public String generate()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Long seq = generateByRedis(dateStr);
        if (seq == null)
        {
            seq = generateByLocal(dateStr);
        }
        String seqStr = String.format("%0" + SEQ_LENGTH + "d", seq);
        return CODE_PREFIX + dateStr + seqStr;
    }

    private Long generateByRedis(String dateStr)
    {
        if (!redisModeProperties.isEnabled() || stringRedisTemplate == null)
        {
            return null;
        }
        try
        {
            String redisKey = REDIS_KEY_PREFIX + dateStr;
            Long seq = stringRedisTemplate.opsForValue().increment(redisKey);
            if (seq != null && seq == 1L)
            {
                stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
            }
            return seq;
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    private Long generateByLocal(String dateStr)
    {
        localSequences.keySet().removeIf(key -> !key.equals(dateStr));
        return (long) localSequences.computeIfAbsent(dateStr, key -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * 校验核销码格式
     * @param code 核销码
     * @return 是否有效
     */
    public boolean isValid(String code)
    {
        if (code == null || code.length() != 14)
        {
            return false;
        }
        if (!code.startsWith(CODE_PREFIX))
        {
            return false;
        }
        String datePart = code.substring(2, 10);
        String seqPart = code.substring(10);
        return datePart.matches("\\d{8}") && seqPart.matches("\\d{4}");
    }
}
