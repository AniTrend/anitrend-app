package com.mxt.anitrend.base.custom.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by max on 2018/02/20.
 */

public class ThreadPool {

    private ExecutorService executorService;

    public ThreadPool createThreadPool() {
        executorService = Executors.newCachedThreadPool();
        return this;
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
