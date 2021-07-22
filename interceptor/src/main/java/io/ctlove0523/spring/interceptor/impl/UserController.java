package io.ctlove0523.spring.interceptor.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author chentong
 */
@Controller
@Slf4j
public class UserController {

    @Autowired
    private TaskExecutor executor;

    @RequestMapping(value = "/user/login", method = RequestMethod.GET)
    public ResponseEntity<LoginToken> login() {
        LoginToken token = new LoginToken();
        token.setTokenCreateTime(System.currentTimeMillis());
        token.setToken("token");
        return new ResponseEntity<>(token, HttpStatus.ACCEPTED);
    }

    @GetMapping("/user/quotes")
    @ResponseBody
    public DeferredResult<String> quotes() {
        log.info("enter quotes");
        DeferredResult<String> deferredResult = new DeferredResult<String>();
        executor.execute(() -> deferredResult.setResult("Hello Async Request"));
        return deferredResult;
    }

}
