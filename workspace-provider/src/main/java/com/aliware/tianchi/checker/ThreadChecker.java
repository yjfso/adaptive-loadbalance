package com.aliware.tianchi.checker;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yinjianfeng
 * @date 2019/6/14
 */
public class ThreadChecker {


    //服务器线程池
    final ThreadPoolExecutor threadPoolExecutor;

    //线程池中拿出来的线程列表
    volatile List<Thread> threads;

    //可用线程数
    final int workableThreadNum;

    final int activeNumBuffer;

    //可用线程预留量
    private final static int WORKABLE_THREAD_BUFFER = 10;

    ThreadChecker() {
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);

        threadPoolExecutor = (ThreadPoolExecutor) executors.values().iterator().next();
        workableThreadNum = threadPoolExecutor.getMaximumPoolSize() - WORKABLE_THREAD_BUFFER;
        activeNumBuffer = (int)(threadPoolExecutor.getMaximumPoolSize() * 0.03);
        refreshThread();
    }

    void refreshThread () {
        threadPoolExecutor.prestartAllCoreThreads();
        threads = ThreadPoolUtil.getThreadsFromPool(threadPoolExecutor);
    }
}
