package com.aliware.tianchi.checker;

import com.aliware.tianchi.BoolLock;
import com.aliware.tianchi.ServerInfo;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author yinjianfeng
 * @date 2019/6/10
 */
public class ServerInfoHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInfoHolder.class);

    private final static Map<Integer, ServerInfo> SERVER_INFO_MAP = new ConcurrentHashMap<>();

    private static volatile ServerInfo powerest;

    private static volatile int factor = 0;

    private final static Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final static BoolLock ELECT_LOCK = new BoolLock();

    private final static int REJECT_THRESHOLD = -10;

    static {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(() -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (ServerInfo value : SERVER_INFO_MAP.values()) {
                        stringBuilder.append(value.getServerPort()).append(":").append(value.getTotalRequest()).append(";");
                    }
                    System.out.println(stringBuilder.toString());
                }, 0, 1, TimeUnit.SECONDS);
    }

    public static void fromMsg(String msg) {
        LOGGER.info("receive msg" + msg);
        String[] meta = msg.split("\\|");
        Integer port = Integer.valueOf(meta[0]);
        SERVER_INFO_MAP.computeIfAbsent(port, ServerInfo::new);
        ServerInfo serverInfo = SERVER_INFO_MAP.get(port);
        serverInfo.setValidThreadNum(Integer.valueOf(meta[1]));
    }

    public static boolean invokeStart(Integer port) {
        ServerInfo serverInfo = get(port);
        int now = serverInfo.incrActiveThreadNum();
        if (now > factor || serverInfo.getValidThreadNum() - now < 10) {
            electPowerest();
        }
        return now > REJECT_THRESHOLD;
    }

    public static void invokeDone(Integer port) {
        SERVER_INFO_MAP.get(port).descActiveThreadNum();
    }

    public static void electPowerest() {
        if (!ELECT_LOCK.tryLock()) {
            return;
        }
        EXECUTOR.execute(
                () -> {
                    try {
                        if (SERVER_INFO_MAP.isEmpty()) {
                            return;
                        }
                        //todo 最小值调整
                        int max = Integer.MIN_VALUE;
                        int second = Integer.MIN_VALUE;
                        ServerInfo powerest = null;
                        StringBuilder weightInfo = new StringBuilder();
                        for (ServerInfo value : SERVER_INFO_MAP.values()) {
                            int weight = value.weight();
                            weightInfo.append(value.getServerPort()).append(":").append(weight).append(";");
                            if (max < weight) {
                                second = max;
                                max = weight;
                                powerest = value;
                            }
                        }
                        if (powerest != null) {
//                            LOGGER.info("elect powerest, the winner[" + powerest.getServerPort() + "]; weights:" + weightInfo.toString());
                            //触发选举因子 可适当减小
                            factor = powerest.getActiveThreadNum() + second;
                            ServerInfoHolder.powerest = powerest;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ELECT_LOCK.unlock();
                    }
                }
        );
    }

    public static ServerInfo loadPowerest() {
        if (powerest == null) {
            if (SERVER_INFO_MAP.isEmpty()) {
                return null;
            }
            powerest = SERVER_INFO_MAP.values().iterator().next();
        }
        return powerest;
    }

    public static ServerInfo get(Integer port) {
        ServerInfo serverInfo = SERVER_INFO_MAP.get(port);
        if (serverInfo == null) {
            serverInfo = new ServerInfo(port);
            SERVER_INFO_MAP.put(port, serverInfo);
        }
        return serverInfo;
    }

}
