package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invoker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinjianfeng
 * @date 2019/6/9
 */
public class ServerInfo {

    private int serverPort;

    private volatile int validThreadNum = 800; // 初始都给高

    private volatile int avgResponseTime = 50;

    public volatile boolean full = false;

    private final AtomicInteger totalRequest = new AtomicInteger(0);

    public final AtomicInteger activeThreadNum = new AtomicInteger(0);

    private Invoker invoker;

    public ServerInfo(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean setValidThreadNum (int validThreadNum) {
        if (validThreadNum == 0) {
            return false;
        }
//        validThreadNum = (int) (validThreadNum * 0.99);
        if (validThreadNum == this.validThreadNum) {
            return false;
        }
        this.validThreadNum = validThreadNum;
        return true;
    }

    private boolean setAvgRt(int avgRt) {
        if (avgRt == 0) {
            return false;
        }
        int diff = avgRt - this.avgResponseTime;
        if (diff >= 0 && diff < 3) {
            return false;
        }
        this.avgResponseTime = avgRt;
        return true;
    }

    public boolean setServerInfo(int validThreadNum, int avgRt) {
        return setValidThreadNum(validThreadNum) || setAvgRt(avgRt);
    }

//    public boolean setServerInfo(int validThreadNum, int avgRt) {
//        validThreadNum = (int) (validThreadNum * 0.9);
//        if (validThreadNum == this.validThreadNum && (avgRt == 0 || this.avgResponseTime == avgRt)) {
//            return false;
//        }
//        if (avgRt != 0) {
//            this.avgResponseTime = avgRt;
//        }
//        this.validThreadNum = validThreadNum;
//        return true;
//    }

    public int incrActiveThreadNum() {
//        totalRequest.incrementAndGet();
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
        return validThreadNum - activeThreadNum.get() > 5;
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

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    //    public void setValidThreadNum(int validThreadNum) {
//        this.validThreadNum = validThreadNum;
//    }
}
