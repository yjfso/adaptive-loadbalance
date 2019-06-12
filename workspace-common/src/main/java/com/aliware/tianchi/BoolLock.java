package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yinjianfeng
 * @date 2019/6/11
 */
public class BoolLock {

    private final AtomicBoolean lock = new AtomicBoolean(true);

    public boolean tryLock() {
        if (lock.get()) {
            if (lock.compareAndSet(true, false)) {
                return true;
            }
        }
        return false;
    }

    public void unlock() {
        lock.set(true);
    }
}
