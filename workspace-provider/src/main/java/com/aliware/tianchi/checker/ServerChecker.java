package com.aliware.tianchi.checker;

import com.aliware.tianchi.CallbackServiceImpl;
import com.aliware.tianchi.ServerInfo;
import com.aliware.tianchi.TianchiThreadFactory;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.RpcContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yinjianfeng
 * @date 2019/6/15
 */
public class ServerChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChecker.class);

    private static class Singleton {

        private final static ServerChecker INSTANCE = new ServerChecker();
    }

    public static ServerChecker getInstance() {
        return ServerChecker.Singleton.INSTANCE;
    }

    public final ThreadChecker threadChecker = new ThreadChecker();

    public final ResponseTimeChecker responseTimeChecker = new ResponseTimeChecker();

    //维护serverInfo周期
    private final static int MAINTAIN_INTO_INTERVAL = 100;

    private final static ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(new TianchiThreadFactory());

    private ServerInfo serverInfo;

    private CallbackServiceImpl callbackServiceImpl;

    public void initServerInfo(CallbackServiceImpl callbackServiceImpl) {
        this.callbackServiceImpl = callbackServiceImpl;
        serverInfo = new ServerInfo(RpcContext.getContext().getLocalPort());
        serverInfo.setValidThreadNum(threadChecker.workableThreadNum);

        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                maintainInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, MAINTAIN_INTO_INTERVAL, TimeUnit.MILLISECONDS);

        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                responseTimeChecker.receive = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2 * MAINTAIN_INTO_INTERVAL / 4 , MAINTAIN_INTO_INTERVAL, TimeUnit.MILLISECONDS);

    }

    private void maintainInfo() {
        int reckonValidThreadNum = WorkThreadInfo.reckonValidThreadNum(threadChecker, serverInfo.getValidThreadNum());
        int avgRt = responseTimeChecker.getAvgRt();
        if (serverInfo.setServerInfo(reckonValidThreadNum, avgRt)) {
//            LOGGER.info("reckonValidThreadNum:" + reckonValidThreadNum + "|avgRt:" + avgRt);
            callbackServiceImpl.sendMsg(serverInfo.toString());
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean acceptable() {
        return threadChecker.workableThreadNum > threadChecker.threadPoolExecutor.getActiveCount();
    }
}
