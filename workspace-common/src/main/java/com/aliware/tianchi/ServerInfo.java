package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/9
 */
public class ServerInfo {

    private int serverPort;

    private volatile int validThreadNum = 800; // 初始都给高

    private volatile int avgResponseTime = 50;

    private final AtomicInteger totalRequest = new AtomicInteger(0);

    public final AtomicInteger activeThreadNum = new AtomicInteger(0);

    public ServerInfo(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean setServerInfo(int validThreadNum, int avgRt) {
        validThreadNum = (int) (validThreadNum * 0.9);
        if (validThreadNum == this.validThreadNum && (avgRt == 0 || this.avgResponseTime == avgRt)) {
            return false;
        }
        if (avgRt != 0) {
            this.avgResponseTime = avgRt;
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

    public boolean hasSurplusThreadNum() {
        return validThreadNum > activeThreadNum.get();
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getTotalRequest() {
        return totalRequest.get();
    }

    public int getAvgResponseTime() {
        return avgResponseTime;
    }

    @Override
    public String toString() {
        return serverPort + "|" + validThreadNum + "|" + avgResponseTime;
    }

    public void setValidThreadNum(int validThreadNum) {
        this.validThreadNum = validThreadNum;
    }
}
