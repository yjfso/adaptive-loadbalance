package com.aliware.tianchi;

import com.aliware.tianchi.checker.ServerThreadChecker;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

    private CallbackListener listener;

    @Override
    public void addListener(String key, CallbackListener listener) {
        this.listener = listener;
        ServerThreadChecker serverThreadChecker = ServerThreadChecker.Singleton.INSTANCE;
        serverThreadChecker.initServerInfo(this);
        sendMsg(serverThreadChecker.getServerInfo().toString());
    }

    public void sendMsg(String msg) {
        listener.receiveServerMsg(msg);
    }
}
