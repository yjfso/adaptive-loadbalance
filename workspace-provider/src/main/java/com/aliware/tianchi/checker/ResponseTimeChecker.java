package com.aliware.tianchi.checker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/15
 */
public class ResponseTimeChecker {

    private final static AtomicInteger TOTAL_TIME = new AtomicInteger();

    private final static AtomicInteger NUM = new AtomicInteger();

    public void addSpecimen(long time) {
        TOTAL_TIME.addAndGet((int)time);
        NUM.incrementAndGet();
    }

    int getAvgRt() {
        try{
            int num = NUM.get();
            if (num == 0) {
                return 0;
            }
            return TOTAL_TIME.get() / NUM.get();
        } finally {
            clear();
        }
    }

    private void clear() {
        TOTAL_TIME.set(0);
        NUM.set(0);
    }

}
