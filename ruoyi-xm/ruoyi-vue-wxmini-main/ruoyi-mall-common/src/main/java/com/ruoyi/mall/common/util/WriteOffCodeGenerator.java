package com.ruoyi.mall.common.util;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 核销码生成器
 * 格式: LY20260605A7K9M2QX (前缀+日期8位+随机码8位)
 */
@Component
public class WriteOffCodeGenerator
{
    private static final String CODE_PREFIX = "LY";
    private static final String RANDOM_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 8;
    private static final Pattern CODE_PATTERN =
            Pattern.compile("^" + CODE_PREFIX + "\\d{8}[" + RANDOM_CHARS + "]{" + RANDOM_LENGTH + "}$");

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成核销码
     * @return 如 LY20260605A7K9M2QX
     */
    public String generate()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return CODE_PREFIX + dateStr + generateRandomSegment();
    }

    private String generateRandomSegment()
    {
        StringBuilder builder = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++)
        {
            int index = secureRandom.nextInt(RANDOM_CHARS.length());
            builder.append(RANDOM_CHARS.charAt(index));
        }
        return builder.toString();
    }

    /**
     * 校验核销码格式
     * @param code 核销码
     * @return 是否有效
     */
    public boolean isValid(String code)
    {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }
}
