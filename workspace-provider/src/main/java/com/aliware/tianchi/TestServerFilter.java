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
            Result result;
            ResponseTimeChecker responseTimeChecker = ServerChecker.getInstance().responseTimeChecker;
//            if (responseTimeChecker.receive) {
                long start = System.currentTimeMillis();
                result = invoker.invoke(invocation);
                long end = System.currentTimeMillis();
                responseTimeChecker.addSpecimen(end - start);
//            } else {
//                result = invoker.invoke(invocation);
//            }
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
