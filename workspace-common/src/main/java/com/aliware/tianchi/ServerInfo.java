package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/9
 */
public class ServerInfo {

    private int serverPort;

    private volatile int validThreadNum = 800; // 初始都给高

    private AtomicInteger totalRequest = new AtomicInteger(0);

    private AtomicInteger activeThreadNum = new AtomicInteger(0);

    public ServerInfo(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean setValidThreadNum(int validThreadNum) {
        if (validThreadNum == this.validThreadNum) {
            return false;
        }
        this.validThreadNum = validThreadNum;
        return true;
    }

    public int incrActiveThreadNum() {
        totalRequest.incrementAndGet();
        return activeThreadNum.incrementAndGet();
    }

    public int descActiveThreadNum() {
        return activeThreadNum.decrementAndGet();
    }

    public int getActiveThreadNum() {
        return activeThreadNum.get();
    }

    public int getValidThreadNum() {
        return validThreadNum;
    }

    public int weight() {
        return validThreadNum - activeThreadNum.get();
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getTotalRequest() {
        return totalRequest.get();
    }
    @Override
    public String toString() {
        return serverPort + "|" + validThreadNum;
    }
}
