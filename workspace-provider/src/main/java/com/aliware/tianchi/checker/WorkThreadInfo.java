package com.aliware.tianchi.checker;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yinjianfeng
 * @date 2019/6/14
 */
public class WorkThreadInfo {

    //正常工作的线程状态
    private final static Set<Thread.State> VALID_STATES =
            new HashSet<Thread.State>() {
                {
                    add(Thread.State.RUNNABLE);
                    add(Thread.State.TIMED_WAITING);
                    add(Thread.State.NEW);
                }
            };

    static int reckonValidThreadNum(ThreadChecker threadChecker) {
        int maxNowValidThread = 0;
        int active = threadChecker.threadPoolExecutor.getActiveCount();

        for (int i = 0; i < 5; i++) {
            int validThread = getNowValidThread(threadChecker);
            if (validThread > maxNowValidThread) {
                maxNowValidThread = validThread;
            }
        }
        if (active - threadChecker.activeNumBuffer > maxNowValidThread) {
            return maxNowValidThread;
        }
//        else if (maxNowValidThread > lastValidThreadNum) {
//            return maxNowValidThread;// todo 步进 添加
//        }
        else {
            return threadChecker.workableThreadNum;
        }
    }

    private static int getNowValidThread(ThreadChecker threadChecker) {
        return getNowValidThread(threadChecker, false);
    }

    private static int getNowValidThread(ThreadChecker threadChecker, boolean retry) {
        int validNum = 0;
        for (Thread thread : threadChecker.threads) {
            Thread.State state = thread.getState();
            if (state == Thread.State.TERMINATED) {
                threadChecker.refreshThread();
                if (retry) {
                    throw new RuntimeException();
                }
                return getNowValidThread(threadChecker, true);
            }
            if (VALID_STATES.contains(state)) {
                validNum ++;
            }
        }
        return validNum;
    }
}