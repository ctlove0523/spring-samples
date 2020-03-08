package io.ctlove0523.spring.interceptor.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author chentong
 */
@Configuration
public class AsyncTaskExecutorConfigure {

    @Bean
    public TaskExecutor createDefaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);

        return executor;
    }
}
