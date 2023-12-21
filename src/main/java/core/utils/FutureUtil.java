package core.utils;

import core.GlobalThreadPool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FutureUtil {

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        GlobalThreadPool.getExecutorService().submit(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}