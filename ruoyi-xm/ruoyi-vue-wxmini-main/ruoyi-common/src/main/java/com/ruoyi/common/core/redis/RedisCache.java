package com.ruoyi.common.core.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * Redis 工具类。
 * 本地联调时如果 Redis 不可用，自动降级到进程内缓存，保证后台和小程序链路可继续验证。
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

    private final Map<String, LocalCacheEntry> localCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public RedisTemplate redisTemplate;

    private volatile boolean localModeLogged = false;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        executeVoid(() -> redisTemplate.opsForValue().set(key, value), () -> putLocalValue(key, value, null));
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        executeVoid(() -> redisTemplate.opsForValue().set(key, value, timeout, timeUnit),
                () -> putLocalValue(key, value, timeoutToMillis(timeout, timeUnit)));
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return execute(() -> redisTemplate.expire(key, timeout, unit), () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            if (entry == null)
            {
                return false;
            }
            entry.setExpireAt(timeoutToExpireAt(timeout, unit));
            return true;
        });
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key)
    {
        return execute(() -> redisTemplate.getExpire(key), () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            if (entry == null)
            {
                return -2L;
            }
            if (entry.getExpireAt() == null)
            {
                return -1L;
            }
            long remain = entry.getExpireAt() - System.currentTimeMillis();
            return remain <= 0 ? -2L : TimeUnit.MILLISECONDS.toSeconds(remain);
        });
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key)
    {
        return execute(() -> redisTemplate.hasKey(key), () -> getLocalEntry(key) != null);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        return execute(() -> {
            ValueOperations<String, T> operation = redisTemplate.opsForValue();
            return operation.get(key);
        }, () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            return entry == null ? null : (T) entry.getValue();
        });
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存键
     */
    public boolean deleteObject(final String key)
    {
        return execute(() -> redisTemplate.delete(key), () -> localCache.remove(key) != null);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return 删除结果
     */
    public boolean deleteObject(final Collection collection)
    {
        return execute(() -> redisTemplate.delete(collection) > 0, () -> {
            boolean removed = false;
            for (Object key : collection)
            {
                removed |= localCache.remove(String.valueOf(key)) != null;
            }
            return removed;
        });
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        return execute(() -> {
            Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
            return count == null ? 0L : count;
        }, () -> {
            putLocalValue(key, new ArrayList<>(dataList), null);
            return (long) dataList.size();
        });
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return execute(() -> redisTemplate.opsForList().range(key, 0, -1), () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            if (entry == null || entry.getValue() == null)
            {
                return Collections.emptyList();
            }
            return new ArrayList<>((List<T>) entry.getValue());
        });
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        return execute(() -> {
            BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
            Iterator<T> it = dataSet.iterator();
            while (it.hasNext())
            {
                setOperation.add(it.next());
            }
            return setOperation;
        }, () -> {
            putLocalValue(key, new LinkedHashSet<>(dataSet), null);
            return null;
        });
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存键
     * @return Set集合
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return execute(() -> redisTemplate.opsForSet().members(key), () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            if (entry == null || entry.getValue() == null)
            {
                return Collections.emptySet();
            }
            return new LinkedHashSet<>((Set<T>) entry.getValue());
        });
    }

    /**
     * 缓存Map
     *
     * @param key Redis键
     * @param dataMap Map数据
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        executeVoid(() -> {
            if (dataMap != null)
            {
                redisTemplate.opsForHash().putAll(key, dataMap);
            }
        }, () -> {
            if (dataMap != null)
            {
                putLocalValue(key, new LinkedHashMap<>(dataMap), null);
            }
        });
    }

    /**
     * 获得缓存的Map
     *
     * @param key Redis键
     * @return Map数据
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return execute(() -> redisTemplate.opsForHash().entries(key), () -> {
            LocalCacheEntry entry = getLocalEntry(key);
            if (entry == null || entry.getValue() == null)
            {
                return Collections.emptyMap();
            }
            return new LinkedHashMap<>((Map<String, T>) entry.getValue());
        });
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        executeVoid(() -> redisTemplate.opsForHash().put(key, hKey, value), () -> {
            Map<String, Object> map = getOrCreateLocalMap(key);
            map.put(hKey, value);
        });
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        return execute(() -> {
            HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
            return opsForHash.get(key, hKey);
        }, () -> {
            Map<String, Object> map = getLocalMap(key);
            return map == null ? null : (T) map.get(hKey);
        });
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return execute(() -> redisTemplate.opsForHash().multiGet(key, hKeys), () -> {
            Map<String, Object> map = getLocalMap(key);
            List<T> values = new ArrayList<>();
            if (map == null)
            {
                return values;
            }
            for (Object hKey : hKeys)
            {
                values.add((T) map.get(String.valueOf(hKey)));
            }
            return values;
        });
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return execute(() -> redisTemplate.opsForHash().delete(key, hKey) > 0, () -> {
            Map<String, Object> map = getLocalMap(key);
            return map != null && map.remove(hKey) != null;
        });
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return execute(() -> redisTemplate.keys(pattern), () -> {
            cleanupExpiredEntries();
            Pattern regex = Pattern.compile(pattern.replace(".", "\\.").replace("*", ".*"));
            List<String> matchedKeys = new ArrayList<>();
            for (String key : localCache.keySet())
            {
                if (regex.matcher(key).matches())
                {
                    matchedKeys.add(key);
                }
            }
            return matchedKeys;
        });
    }

    private <T> T execute(RedisSupplier<T> redisAction, LocalSupplier<T> localAction)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisAction.get();
            }
            catch (DataAccessException e)
            {
                logLocalMode(e);
            }
        }
        return localAction.get();
    }

    private void executeVoid(RedisRunnable redisAction, LocalRunnable localAction)
    {
        if (redisTemplate != null)
        {
            try
            {
                redisAction.run();
                return;
            }
            catch (DataAccessException e)
            {
                logLocalMode(e);
            }
        }
        localAction.run();
    }

    private void logLocalMode(Exception e)
    {
        if (!localModeLogged)
        {
            synchronized (this)
            {
                if (!localModeLogged)
                {
                    log.warn("Redis 不可用，已自动切换到本地内存缓存模式：{}", e.getMessage());
                    localModeLogged = true;
                }
            }
        }
    }

    private void putLocalValue(String key, Object value, Long ttlMillis)
    {
        localCache.put(key, new LocalCacheEntry(value, ttlMillis == null ? null : System.currentTimeMillis() + ttlMillis));
    }

    private LocalCacheEntry getLocalEntry(String key)
    {
        LocalCacheEntry entry = localCache.get(key);
        if (entry == null)
        {
            return null;
        }
        if (entry.isExpired())
        {
            localCache.remove(key);
            return null;
        }
        return entry;
    }

    private void cleanupExpiredEntries()
    {
        Iterator<Map.Entry<String, LocalCacheEntry>> iterator = localCache.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, LocalCacheEntry> entry = iterator.next();
            if (entry.getValue().isExpired())
            {
                iterator.remove();
            }
        }
    }

    private Long timeoutToMillis(Integer timeout, TimeUnit unit)
    {
        if (timeout == null || unit == null)
        {
            return null;
        }
        return unit.toMillis(timeout.longValue());
    }

    private Long timeoutToExpireAt(long timeout, TimeUnit unit)
    {
        return System.currentTimeMillis() + unit.toMillis(timeout);
    }

    private Map<String, Object> getLocalMap(String key)
    {
        LocalCacheEntry entry = getLocalEntry(key);
        if (entry == null || entry.getValue() == null)
        {
            return null;
        }
        return (Map<String, Object>) entry.getValue();
    }

    private Map<String, Object> getOrCreateLocalMap(String key)
    {
        LocalCacheEntry entry = getLocalEntry(key);
        if (entry != null && entry.getValue() instanceof Map)
        {
            return (Map<String, Object>) entry.getValue();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        putLocalValue(key, map, null);
        return map;
    }

    @FunctionalInterface
    private interface RedisSupplier<T>
    {
        T get();
    }

    @FunctionalInterface
    private interface LocalSupplier<T>
    {
        T get();
    }

    @FunctionalInterface
    private interface RedisRunnable
    {
        void run();
    }

    @FunctionalInterface
    private interface LocalRunnable
    {
        void run();
    }

    private static class LocalCacheEntry
    {
        private final Object value;

        private Long expireAt;

        LocalCacheEntry(Object value, Long expireAt)
        {
            this.value = value;
            this.expireAt = expireAt;
        }

        Object getValue()
        {
            return value;
        }

        Long getExpireAt()
        {
            return expireAt;
        }

        void setExpireAt(Long expireAt)
        {
            this.expireAt = expireAt;
        }

        boolean isExpired()
        {
            return expireAt != null && expireAt <= System.currentTimeMillis();
        }
    }
}
