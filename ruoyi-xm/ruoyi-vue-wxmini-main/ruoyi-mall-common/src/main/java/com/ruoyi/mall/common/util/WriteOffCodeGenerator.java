package com.ruoyi.mall.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 核销码生成器
 * 格式: LY202605250001 (前缀+日期8位+序列号4位)
 */
@Component
public class WriteOffCodeGenerator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CODE_PREFIX = "LY";
    private static final String REDIS_KEY_PREFIX = "write_off:code:seq:";
    private static final int SEQ_LENGTH = 4;

    /**
     * 生成核销码
     * @return 如 LY202605250001
     */
    public String generate() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String redisKey = REDIS_KEY_PREFIX + dateStr;

        // Redis原子自增，当日首次从1开始
        Long seq = stringRedisTemplate.opsForValue().increment(redisKey);

        // 设置过期时间（2天后自动清理）
        if (seq != null && seq == 1) {
            stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        }

        // 格式化为4位数字，左补零
        String seqStr = String.format("%0" + SEQ_LENGTH + "d", seq != null ? seq : 1);

        return CODE_PREFIX + dateStr + seqStr;
    }

    /**
     * 校验核销码格式
     * @param code 核销码
     * @return 是否有效
     */
    public boolean isValid(String code) {
        if (code == null || code.length() != 14) {
            return false;
        }
        if (!code.startsWith(CODE_PREFIX)) {
            return false;
        }
        String datePart = code.substring(2, 10);
        String seqPart = code.substring(10);
        return datePart.matches("\\d{8}") && seqPart.matches("\\d{4}");
    }
}
