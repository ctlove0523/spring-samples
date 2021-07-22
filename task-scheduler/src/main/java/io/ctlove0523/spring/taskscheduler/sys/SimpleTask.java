package io.ctlove0523.spring.taskscheduler.sys;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: SchedulerTaskService
 *
 * @author: chentong
 * Date:     2018/12/13 23:16
 */
@Component
public class SimpleTask {
    private ApplicationContext context;
    private int counts = 0;

    @Autowired
    private ScheduledProcessor processor;

    @Scheduled(fixedRate = 1000L, initialDelay = 1000L)
    public void doSimpleTask() {
        counts++;
        if (counts == 6) {
            System.out.println("begin to cancel task ");
            processor.stopTask(this);
        }
        System.out.println(new Date().toString() + " do business thing");
    }

}
