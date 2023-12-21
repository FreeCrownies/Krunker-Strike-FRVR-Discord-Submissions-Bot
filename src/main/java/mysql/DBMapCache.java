package mysql;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public abstract class DBMapCache<T, U> extends DBCache {

    private final LoadingCache<T, U> cache;

    protected DBMapCache() {
        cache = getCacheBuilder().build(
                new CacheLoader<>() {
                    @Override
                    public U load(T t) throws Exception {
                        return process(t);
                    }
                }
        );
    }

    protected CacheBuilder<Object, Object> getCacheBuilder() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(getExpirationTimeMinutes() == null ? 10 : getExpirationTimeMinutes()));
    }

    protected abstract U load(T t) throws Exception;

    protected U process(T t) throws Exception {
        return DBMapCache.this.load(t);
    }

    public U retrieve (T t) {
        try {
            return cache.get(t);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected LoadingCache<T, U> getCache() {
        return cache;
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public void invalidateGuildId(long guildId) {
        if (cache.asMap().keySet().stream().findFirst().map(key -> key instanceof Long).orElse(false)) {
            cache.invalidate(guildId);
        }
    }

    public Integer getExpirationTimeMinutes() {
        return null;
    }

}