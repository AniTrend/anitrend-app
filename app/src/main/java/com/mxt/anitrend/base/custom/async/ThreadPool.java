package com.mxt.anitrend.base.custom.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by max on 2018/02/20.
 */

public class ThreadPool {

    private ExecutorService executorService;

    private ThreadPool() {

    }

    public ExecutorService getExecutorService() {
        if(executorService == null)
            executorService = Executors.newCachedThreadPool();
        return executorService;
    }

    public void execute(Runnable runnable) {
        getExecutorService().execute(runnable);
    }

    public static class Builder {

        private ThreadPool threadPool;

        public Builder() {
            this.threadPool = new ThreadPool();
        }

        public ThreadPool build() {
            return threadPool;
        }
    }
}
