package io.github.ctlove0523.tls;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
public class TestController {
    public static void main(String[] args) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean result= antPathMatcher.match("/**", "/api/hello");

        System.out.println(result);
    }

    @RequestMapping(value = "/api/apps/{appId}", method = RequestMethod.GET)
    public ResponseEntity<App> showApp(@PathVariable(name = "appId") String appId) {
        App app = new App();
        app.setId(appId);
        app.setName("hello app");
        return ResponseEntity.ok(app);
    }

    @RequestMapping(value = "/api/health", method = RequestMethod.GET)
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("health");
    }

    @RequestMapping(value = "/api/users", method = RequestMethod.GET)
    public ResponseEntity<User> showUser() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("hello app");
        return ResponseEntity.ok(user);
    }
}

class User {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
class App {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}