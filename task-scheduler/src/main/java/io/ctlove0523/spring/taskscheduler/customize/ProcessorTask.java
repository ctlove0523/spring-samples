package io.ctlove0523.spring.taskscheduler.customize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: TaskConfigure
 *
 * @author: chentong
 * Date:     2018/12/14 22:01
 */
@Service
public class ProcessorTask {
    private static final int MAX_COUNTS = 5;
    private int counts = 0;

    @Autowired
    private CustomTaskScheduler scheduler;

    /**
     * This task will be execute file times and then cancel.
     */
    @Scheduled(fixedRate = 500L)
    public void doProcessorTask() {
        if (counts == MAX_COUNTS) {
            System.out.println("processor task success,begin to cancel");
            scheduler.cancelTask(this);
        }
        System.out.println("this " + counts++ + " times do processor task ");
    }

}
