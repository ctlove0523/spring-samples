package io.ctlove0523.spring.retry.resilience4j.sync.excpetion;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: RetryNeedException
 *
 * @author: chentong
 * Date:     2018/12/15 23:59
 */
public class RetryNeedException extends RuntimeException {
    private static final String DEFAULT_CONTENT = "Need Retry";
    private String content;

    public RetryNeedException(String content) {
        this.content = content;
    }

    public static RetryNeedException defaultException() {
        return new RetryNeedException(DEFAULT_CONTENT);
    }
}
