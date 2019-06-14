package com.aliware.tianchi.checker;

import com.aliware.tianchi.CallbackServiceImpl;
import com.aliware.tianchi.ServerInfo;
import com.aliware.tianchi.TianchiThreadFactory;
import org.apache.dubbo.rpc.RpcContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yinjianfeng
 * @date 2019/6/8
 */
//todo 仅做单服务检测
public class ServerThreadChecker {

    //维护serverInfo周期
    private final static int MAINTAIN_INTO_INTERVAL = 1000;

    private final static ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(new TianchiThreadFactory());

    private ServerInfo serverInfo;

    private final ServerThreadInfo serverThreadInfo;

    private CallbackServiceImpl callbackServiceImpl;

    private static class Singleton {

        private final static ServerThreadChecker INSTANCE = new ServerThreadChecker();

    }

    public static ServerThreadChecker getInstance() {
        return Singleton.INSTANCE;
    }

    private ServerThreadChecker() {
        serverThreadInfo = new ServerThreadInfo();
    }

    public void initServerInfo(CallbackServiceImpl callbackServiceImpl) {
        this.callbackServiceImpl = callbackServiceImpl;
        serverInfo = new ServerInfo(RpcContext.getContext().getLocalPort());

        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                maintainInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, MAINTAIN_INTO_INTERVAL, TimeUnit.MILLISECONDS);

    }

    private void maintainInfo() {
        int reckon = WorkThreadInfo.reckonValidThreadNum(serverThreadInfo);
        if (serverInfo.setValidThreadNum(reckon)) {
            callbackServiceImpl.sendMsg(serverInfo.toString());
        }
    }


    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean acceptable() {
        return serverThreadInfo.workableThreadNum > serverThreadInfo.threadPoolExecutor.getActiveCount();
    }

}
