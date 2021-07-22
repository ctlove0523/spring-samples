package io.spring.ct.retry.resilience4j.async;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import io.ctlove0523.spring.retry.resilience4j.async.AsyncRetryService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: AsyncRetryServiceTest
 *
 * @author: chentong
 * Date:     2018/12/16 12:02
 */
public class AsyncRetryServiceTest {

    @Test
    public void test_asyncRetryOnException() throws ExecutionException, InterruptedException {
        AsyncRetryService asyncRetryService = new AsyncRetryService();
        Supplier<CompletionStage<String>> result = asyncRetryService.asyncRetryOnException();
        Assert.assertTrue(result.get().toCompletableFuture().get().contains("async retry"));
        Assert.assertEquals(3,asyncRetryService.getExecuteTimes());
    }

    @Test
    public void test_asyncNoRetryOnException() {
        try {
            AsyncRetryService asyncRetryService = new AsyncRetryService();
            Supplier<CompletionStage<String>> result = asyncRetryService.asyncNoRetryOnException();
            Assert.assertTrue(result.get().toCompletableFuture().get().contains("async retry"));
            Assert.assertEquals(1,asyncRetryService.getExecuteTimes());
        } catch (Exception e) {

        }
    }

    @Test
    public void test_asyncRetryOnResult() throws ExecutionException, InterruptedException {
        AsyncRetryService asyncRetryService = new AsyncRetryService();
        Supplier<CompletionStage<String>> result = asyncRetryService.asyncRetryOnResult();
        Assert.assertTrue(result.get().toCompletableFuture().get().contains("success"));
        Assert.assertEquals(3,asyncRetryService.getExecuteTimes());
    }
}
