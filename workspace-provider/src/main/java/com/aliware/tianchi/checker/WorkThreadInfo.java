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

    private final static int ACTIVE_NUM_BUFFER = 5;

    static int reckonValidThreadNum(ServerThreadInfo serverThreadInfo) {
        int maxNowValidThread = 0;
        int active = serverThreadInfo.threadPoolExecutor.getActiveCount();

        for (int i = 0; i < 5; i++) {
            int validThread = getNowValidThread(serverThreadInfo);
            if (validThread > maxNowValidThread) {
                maxNowValidThread = validThread;
            }
        }
        if (active - ACTIVE_NUM_BUFFER > maxNowValidThread) {
            System.out.println(maxNowValidThread + "|---active:" + active + ";maxNowVaildThread:" + maxNowValidThread + ";workableThreadNum"+serverThreadInfo.workableThreadNum);
            return maxNowValidThread;
        } else {
            System.out.println(serverThreadInfo.workableThreadNum + "|---active:" + active + ";maxNowVaildThread:" + maxNowValidThread + ";workableThreadNum"+serverThreadInfo.workableThreadNum);
            return serverThreadInfo.workableThreadNum;
        }
    }

    private static int getNowValidThread(ServerThreadInfo serverThreadInfo) {
        return getNowValidThread(serverThreadInfo, false);
    }

    private static int getNowValidThread(ServerThreadInfo serverThreadInfo, boolean retry) {
        int validNum = 0;
        for (Thread thread : serverThreadInfo.threads) {
            Thread.State state = thread.getState();
            if (state == Thread.State.TERMINATED) {
                serverThreadInfo.refreshThread();
                if (retry) {
                    throw new RuntimeException();
                }
                return getNowValidThread(serverThreadInfo, true);
            }
            if (VALID_STATES.contains(state)) {
                validNum ++;
            }
        }
        return validNum;
    }
}
