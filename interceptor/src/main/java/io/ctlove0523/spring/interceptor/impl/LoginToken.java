package io.ctlove0523.spring.interceptor.impl;

import lombok.Data;

/**
 * @author chentong
 */
@Data
public class LoginToken {
    private long tokenCreateTime;
    private String token;
}
