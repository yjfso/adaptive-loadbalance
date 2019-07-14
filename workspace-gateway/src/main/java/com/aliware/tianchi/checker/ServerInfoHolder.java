package com.aliware.tianchi.checker;

import com.aliware.tianchi.ServerInfo;
import com.aliware.tianchi.TimeUtil;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinjianfeng
 * @date 2019/6/10
 */
public class ServerInfoHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInfoHolder.class);

    final static Map<Integer, ServerInfo> SERVER_INFO_MAP = new HashMap<>();

    final static List<ServerInfo> SERVER_INFOS = new ArrayList<>();

    private final static int REJECT_THRESHOLD = -10;

    public static void fromMsg(String msg) {
        LOGGER.info(TimeUtil.getNow() + " receive msg" + msg);
        String[] meta = msg.split("\\|");
        Integer port = Integer.valueOf(meta[0]);

        ServerInfo serverInfo = get(port);
        serverInfo.setServerInfo(Integer.valueOf(meta[1]), Integer.valueOf(meta[2]));
    }

    public static boolean invokeStart(Integer port) {
        ServerInfo serverInfo = get(port);
        int now = serverInfo.incrActiveThreadNum();
//        if (now > factor || serverInfo.getValidThreadNum() - now < 10) {
//            electPowerest();
//        }
        return now > REJECT_THRESHOLD;
    }

    public static void invokeDone(Integer port) {
        SERVER_INFO_MAP.get(port).descActiveThreadNum();
    }

    public static ServerInfo get(Integer port) {
        ServerInfo serverInfo = SERVER_INFO_MAP.get(port);
        if (serverInfo == null) {
            synchronized (ServerInfoHolder.class) {
                serverInfo = SERVER_INFO_MAP.get(port);
                if (serverInfo == null) {
                    serverInfo = new ServerInfo(port);
                    SERVER_INFO_MAP.put(port, serverInfo);
                    SERVER_INFOS.add(serverInfo);
                }
            }
        }
        return serverInfo;
    }

    public static void remove(Integer port) {
        SERVER_INFOS.remove(SERVER_INFO_MAP.get(port));
        SERVER_INFO_MAP.remove(port);
    }

}
