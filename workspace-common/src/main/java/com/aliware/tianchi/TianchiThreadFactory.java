package com.aliware.tianchi;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/12
 */
public class TianchiThreadFactory implements ThreadFactory {

    private ThreadGroup group = new ThreadGroup("tianchi-thread");

    private AtomicInteger threadNumber = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                "tainchi" + threadNumber.getAndIncrement(),
                0);
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }
}
