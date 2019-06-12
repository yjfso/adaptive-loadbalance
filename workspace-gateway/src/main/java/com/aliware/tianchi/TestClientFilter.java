package com.aliware.tianchi;

import com.aliware.tianchi.checker.ServerInfoHolder;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            if (ServerInfoHolder.invokeStart(invoker.getUrl().getPort())) {
                Result result = invoker.invoke(invocation);
                CompletableFuture<Integer> future = RpcContext.getContext().getCompletableFuture();
                if (future != null) {
                    future.whenComplete(
                            (actual, t)->
                                    ServerInfoHolder.invokeDone(invoker.getUrl().getPort())
                    );
                } else {
                    ServerInfoHolder.invokeDone(invoker.getUrl().getPort());
                }
                return result;
            } else {
                ServerInfoHolder.invokeDone(invoker.getUrl().getPort());
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }
}
