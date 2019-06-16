package com.aliware.tianchi;

import com.aliware.tianchi.checker.ResponseTimeChecker;
import com.aliware.tianchi.checker.ServerChecker;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;


/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            long start = System.currentTimeMillis();
            Result result = invoker.invoke(invocation);
            if ("hash".equals(invocation.getMethodName())) {
                long end = System.currentTimeMillis();
                ServerChecker.getInstance().responseTimeChecker.addSpecimen(end - start);
            }
            return result;
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
