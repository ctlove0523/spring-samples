package io.ctlove0523.spring.filter.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * @author chentong
 */
@Component
public class UserService {
    /**
     * database store user info
     */
    private Map<String, String> userInfo = new HashMap<>();

    private Map<String, Integer> viewTimes = new ConcurrentHashMap<>();

    /**
     * add user
     */
    @PostConstruct
    public void init() {
        userInfo.put("spring boot", "1234");
        userInfo.put("filter", "123456");
    }

    public boolean userMatch(String userName, String password) {
        return userInfo.get(userName) != null && userInfo.get(userName).equals(password);
    }

    public void view(String userName) {
        int times = viewTimes.getOrDefault(userName, 0);
        viewTimes.put(userName, times + 1);
    }
}
