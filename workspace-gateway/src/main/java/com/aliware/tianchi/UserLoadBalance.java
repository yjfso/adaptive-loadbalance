package com.aliware.tianchi;

import com.aliware.tianchi.checker.Elector;
import com.aliware.tianchi.checker.ServerInfoHolder;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    private final BoolLock init = new BoolLock();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (!init.tryLock()) {
            invokers.forEach(
                    invoker ->{
                        ServerInfo serverInfo = ServerInfoHolder.get(invoker.getUrl().getPort());
                        serverInfo.setInvoker(invoker);
                    }
            );
        }
        ServerInfo serverInfo = Elector.loadPowerest();

        if (serverInfo != null) {
            Invoker invoker = serverInfo.getInvoker();
            if (!invoker.isAvailable()) {
                ServerInfoHolder.remove(serverInfo.getServerPort());
                init.unlock();
            }
            return invoker;
        }
        {
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }
    }
}
