package io.spring.ct.retry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetryApplicationTests implements ApplicationContextAware {
    private ApplicationContext context;

    @Mock
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {

        String content = restTemplate.getForEntity("http://localhost:8080/api",String.class).getBody();
        System.out.println(content);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
