package io.ctlove0523.spring.retry.resilience4j.sync;

import io.ctlove0523.spring.retry.resilience4j.excpetion.RetryNeedException;
import io.ctlove0523.spring.retry.resilience4j.excpetion.RetryNoNeedException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: SyncRetryService
 *
 * @author: chentong
 * Date:     2018/12/15 23:57
 */
public class SyncRetryService {
    private static final long DEFAULT_WAIT_DURATION = 300L;
    private static RetryConfig config = RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofMillis(DEFAULT_WAIT_DURATION))
            .retryExceptions(RetryNeedException.class)
            .ignoreExceptions(RetryNoNeedException.class)
            .retryOnException(throwable -> throwable instanceof RuntimeException)
            .retryOnResult(resp -> resp.toString().contains("result cause retry"))
            .build();

    private Retry retry = Retry.of("sync retry", config);

    private int executeTimes = 0;

    public void retryOnException() {
        Retry.decorateRunnable(retry, new Runnable() {
            @Override
            public void run() {
                if (executeTimes++ < 3) {
                    throw RetryNeedException.defaultException();
                }
            }
        }).run();
    }

    public void noRetryOnException() {
        Retry.decorateRunnable(retry, new Runnable() {
            @Override
            public void run() {
                if (executeTimes++ < 3) {
                    throw RetryNoNeedException.defaultException();
                }
            }
        }).run();
    }

    public void resultNeedRetry() {
        try {
            Retry.decorateCallable(retry, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    if (executeTimes++ < 3) {
                        return "result cause retry";
                    }
                    return "success";
                }
            }).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getExecuteTimes() {
        return executeTimes;
    }
}
