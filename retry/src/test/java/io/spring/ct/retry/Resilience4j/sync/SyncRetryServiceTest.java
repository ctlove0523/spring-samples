package io.spring.ct.retry.resilience4j.sync;

import io.ctlove0523.spring.retry.resilience4j.sync.SyncRetryService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: SyncRetryService
 *
 * @author: chentong
 * Date:     2018/12/15 23:57
 */
public class SyncRetryServiceTest {

    @Test
    public void test_retryOnException() throws Exception {
        SyncRetryService syncRetryService = new SyncRetryService();
        syncRetryService.retryOnException();
        Assert.assertEquals(4, syncRetryService.getExecuteTimes());
    }

    @Test
    public void test_noRetryOnException() {
        SyncRetryService syncRetryService = new SyncRetryService();
        try {
            syncRetryService.noRetryOnException();
        } catch (Exception e) {

        }
        Assert.assertEquals(1, syncRetryService.getExecuteTimes());
    }

    @Test
    public void test_resultNeedRetry() {
        SyncRetryService syncRetryService = new SyncRetryService();
        syncRetryService.resultNeedRetry();
        Assert.assertEquals(4, syncRetryService.getExecuteTimes());
    }

}
