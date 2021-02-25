package io.ctlove0523.spring.retry.resilience4j.async;

import io.ctlove0523.spring.retry.resilience4j.RetryThreadFactory;
import io.ctlove0523.spring.retry.resilience4j.excpetion.RetryNeedException;
import io.ctlove0523.spring.retry.resilience4j.excpetion.RetryNoNeedException;
import io.github.resilience4j.retry.AsyncRetry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: AsyncRetryService
 *
 * @author: chentong
 * Date:     2018/12/16 11:15
 */
public class AsyncRetryService {
    private static final long DEFAULT_WAIT_DURATION = 300L;
    private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new RetryThreadFactory());
    private static final RetryConfig config = RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofMillis(DEFAULT_WAIT_DURATION))
            .retryExceptions(RetryNeedException.class)
            .ignoreExceptions(RetryNoNeedException.class)
            .retryOnException(throwable -> throwable instanceof RuntimeException)
            .retryOnResult(resp -> resp.toString().contains("result cause retry"))
            .build();

    private final AsyncRetry asyncRetry = AsyncRetry.of("async retry", config);

    private final AtomicInteger executeTimes = new AtomicInteger(0);

    public Supplier<CompletionStage<String>> asyncRetryOnException() {
        return AsyncRetry.decorateCompletionStage(asyncRetry, executor, () -> exceptionCauseRetry());
    }

    public Supplier<CompletionStage<String>> asyncNoRetryOnException() {
        return AsyncRetry.decorateCompletionStage(asyncRetry, executor, () -> exceptionCauseNoRetry());
    }

    public Supplier<CompletionStage<String>> asyncRetryOnResult() {
        return AsyncRetry.decorateCompletionStage(asyncRetry, executor, () -> resultCauseRetry());
    }


    private CompletionStage<String> exceptionCauseRetry() {
        if (executeTimes.getAndIncrement() < 2) {
            throw new RetryNeedException("need retry");
        }
        return CompletableFuture.completedFuture("async retry");
    }

    private CompletionStage<String> exceptionCauseNoRetry() {
        if (executeTimes.getAndIncrement() < 2) {
            throw new RetryNoNeedException("need retry");
        }
        return CompletableFuture.completedFuture("async retry");
    }

    private CompletionStage<String> resultCauseRetry() {
        if (executeTimes.getAndIncrement() < 2) {
            return CompletableFuture.completedFuture("result cause retry");
        }
        return CompletableFuture.completedFuture("success");
    }


    public int getExecuteTimes() {
        return executeTimes.get();
    }
}
