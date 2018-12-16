package io.ctlove0523.spring.retry.resilience4j.excpetion;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: RetryNoNeedException
 *
 * @author: chentong
 * Date:     2018/12/16 0:03
 */
public class RetryNoNeedException extends RuntimeException {
    private static final String DEFAULT_CONTENT = "No Need Retry";
    private String content;

    public RetryNoNeedException(String content) {
        this.content = content;
    }

    public static RetryNoNeedException defaultException() {
        return new RetryNoNeedException(DEFAULT_CONTENT);
    }
}
