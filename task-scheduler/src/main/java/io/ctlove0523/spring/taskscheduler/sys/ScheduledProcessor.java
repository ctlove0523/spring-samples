package io.ctlove0523.spring.taskscheduler.sys;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: ScheduledProcessor
 *
 * @author: chentong
 * Date:     2018/12/13 23:43
 */
@Service
public class ScheduledProcessor implements ApplicationContextAware {
    private ApplicationContext context;
    private ScheduledAnnotationBeanPostProcessor processor;

    @PostConstruct
    public void initProcessor() {
        processor = (ScheduledAnnotationBeanPostProcessor) context.getBean("org.springframework.context.annotation.internalScheduledAnnotationProcessor");
    }

    public void stopTask(Object bean) {
        processor.postProcessBeforeDestruction(bean, null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
