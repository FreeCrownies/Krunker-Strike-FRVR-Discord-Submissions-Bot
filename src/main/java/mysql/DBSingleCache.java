package mysql;

import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DBSingleCache<T> extends DBCache {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(new CountingThreadFactory(() -> "Main", "SingleCache", false));
    private T o = null;
    private Instant nextUpdate = null;

    protected abstract T loadData() throws Exception;

    public synchronized T retrieve() {
        if (o == null) {
            try {
                o = loadData();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            setExpirationTimer();
        } else if (nextUpdate != null && Instant.now().isAfter(nextUpdate)) {
            setExpirationTimer();
            try {
                executorService.submit(() -> {
                    try {
                        o = loadData();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return o;
    }

    private void setExpirationTimer() {
        Integer expirationTimeMinutes = getExpirationTimeMinutes();
        if (expirationTimeMinutes != null) {
            nextUpdate = Instant.now().plus(expirationTimeMinutes, ChronoUnit.MINUTES);
        }
    }

    public boolean isCached() {
        return o != null;
    }

    public Integer getExpirationTimeMinutes() {
        return null;
    }

    @Override
    public void clear() {
        o = null;
    }

    @Override
    public void invalidateGuildId(long guildId) {
    }

}