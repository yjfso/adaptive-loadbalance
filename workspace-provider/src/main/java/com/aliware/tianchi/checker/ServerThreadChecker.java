package com.aliware.tianchi.checker;

import com.aliware.tianchi.CallbackServiceImpl;
import com.aliware.tianchi.ServerInfo;
import com.aliware.tianchi.TianchiThreadFactory;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yinjianfeng
 * @date 2019/6/8
 */
//todo 仅做单服务检测
public class ServerThreadChecker {

    //正常工作的线程状态
    private final static Set<Thread.State> VALID_STATES =
            new HashSet<Thread.State>() {
                {
                    add(Thread.State.RUNNABLE);
                    add(Thread.State.TIMED_WAITING);
                    add(Thread.State.NEW);
                }
            };

    //可用线程预留量
    private final static int WORKABLE_THREAD_BUFFER_NUM = 10;

    //维护serverInfo周期
    private final static int MAINTAIN_INTO_INTERVAL = 50;

    private final static ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(new TianchiThreadFactory());

    //可用线程数
    private final int workableThreadNum;

    //服务器线程池
    private final ThreadPoolExecutor threadPoolExecutor;

    //线程池中拿出来的线程列表
    private volatile List<Thread> threads;

    private ServerInfo serverInfo;

    private CallbackServiceImpl callbackServiceImpl;

    public static class Singleton {

        public final static ServerThreadChecker INSTANCE = new ServerThreadChecker();

    }

    public ServerThreadChecker getInstance() {
        return Singleton.INSTANCE;
    }

    private ServerThreadChecker() {
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);

        threadPoolExecutor = (ThreadPoolExecutor) executors.values().iterator().next();
        workableThreadNum = threadPoolExecutor.getMaximumPoolSize() - WORKABLE_THREAD_BUFFER_NUM;
        refreshThread();
    }

    public void initServerInfo(CallbackServiceImpl callbackServiceImpl) {
        this.callbackServiceImpl = callbackServiceImpl;
        serverInfo = new ServerInfo(RpcContext.getContext().getLocalPort());
        serverInfo.setValidThreadNum(workableThreadNum);

        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                maintainInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, MAINTAIN_INTO_INTERVAL, TimeUnit.MILLISECONDS);

    }

    private void maintainInfo() {
        int activeNum = threadPoolExecutor.getActiveCount();

        int finalValidNum = 0;

        if (workableThreadNum > activeNum) {
            finalValidNum = workableThreadNum;
        } else {
            for (int i = 0; i < 3; i++) {
                int validNum = 0;
                for (Thread thread : threads) {
                    Thread.State state = thread.getState();
                    if (state == Thread.State.TERMINATED) {
                        refreshThread();
                        return;
                    }
                    if (VALID_STATES.contains(state)) {
                        validNum ++;
                    }
                }
                if (validNum > finalValidNum) {
                    finalValidNum = validNum;
                }
            }
        }
        if (serverInfo.setValidThreadNum(finalValidNum)) {
            callbackServiceImpl.sendMsg(serverInfo.toString());
        }
    }

    private void refreshThread () {
        threadPoolExecutor.prestartAllCoreThreads();
        threads = ThreadPoolUtil.getThreadsFromPool(threadPoolExecutor);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean acceptable() {
        return workableThreadNum > threadPoolExecutor.getActiveCount();
    }

}
