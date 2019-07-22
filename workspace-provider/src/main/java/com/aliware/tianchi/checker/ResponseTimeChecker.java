package com.aliware.tianchi.checker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/15
 */
public class ResponseTimeChecker {

    public volatile boolean receive = true;

    private final AtomicInteger TOTAL_TIME = new AtomicInteger();

    private final AtomicInteger NUM = new AtomicInteger();

    public void addSpecimen(long time) {
        TOTAL_TIME.addAndGet((int)time);
        NUM.incrementAndGet();
    }

    int getAvgRt() {
//        receive = false;
        try{
            int num = NUM.get();
            if (num == 0) {
                return 0;
            }
            return TOTAL_TIME.get() / num;
        } finally {
            clear();
        }
    }

    private void clear() {
        TOTAL_TIME.set(0);
        NUM.set(0);
    }

}
