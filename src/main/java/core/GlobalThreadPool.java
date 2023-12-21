package core;

import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalThreadPool {

	private static final ExecutorService executorService = Executors.newCachedThreadPool(new CountingThreadFactory(() -> "Main", "ThreadPool", false));

	public static ExecutorService getExecutorService() {
		return executorService;
	}

}