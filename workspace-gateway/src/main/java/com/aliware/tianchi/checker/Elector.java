package com.aliware.tianchi.checker;

import com.aliware.tianchi.BoolLock;
import com.aliware.tianchi.ServerInfo;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yinjianfeng
 * @date 2019/6/16
 */
public class Elector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Elector.class);

    private static volatile ServerInfo powerest;

    private final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final static BoolLock ELECT_LOCK = new BoolLock();

    static {
//        EXECUTOR
//                .scheduleWithFixedDelay(() -> {
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (ServerInfo value : ServerInfoHolder.SERVER_INFO_MAP.values()) {
//                        stringBuilder.append(value.getServerPort()).append(":").append(value.getTotalRequest()).append(";");
//                    }
//                    System.out.println(stringBuilder.toString());
//                }, 0, 1, TimeUnit.SECONDS);
//        electPowerest();
    }

    public static void electPowerest() {
//        if (!ELECT_LOCK.tryLock()) {
//            return;
//        }
        EXECUTOR.scheduleWithFixedDelay(
                () -> {
                    try {
                        if (ServerInfoHolder.SERVER_INFO_MAP.isEmpty()) {
                            return;
                        }
                        int minAvgResponseTime = Integer.MAX_VALUE;
                        ServerInfo powerest = null;
                        ServerInfo minRt = null;
                        int size = ServerInfoHolder.SERVER_INFOS.size();
                        for (int i = 0; i < size; i++) {
                            ServerInfo value = ServerInfoHolder.SERVER_INFOS.get(i);
                            if (value.mandatory()) {
                                powerest = value;
                                break;
                            }
                            int rt = value.getAvgResponseTime();
                            if (powerest == null || minAvgResponseTime > rt) {
                                minAvgResponseTime = rt;
                                minRt = value;
                                if (value.hasSurplusThreadNum() && value.qualified()) {
                                    powerest = value;
                                }
                            }
                        }

                        if (powerest != null) {
//                            LOGGER.info("elect powerest, the winner[" + powerest.getServerPort() + "]; weights:" + weightInfo.toString());
                            //触发选举因子 可适当减小
//                            factor = powerest.getActiveThreadNum() + second;
                            Elector.powerest = powerest;
                        } else {
                            Elector.powerest = minRt;
                        }
                        for (int i = 0; i < size; i++) {
                            ServerInfo value = ServerInfoHolder.SERVER_INFOS.get(i);
                            if (value == Elector.powerest) {
                                value.elect();
                            } else {
                                value.unElect();
                            }
                        }

//                        LOGGER.info("choice:" + Elector.powerest.getServerPort() + "|" +weightInfo.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
//                        ELECT_LOCK.unlock();
                    }
                }, 0, 1, TimeUnit.MILLISECONDS
        );
    }

    public static void electPowerest1() {
        if (!ELECT_LOCK.tryLock()) {
            return;
        }
        int minAvgResponseTime = Integer.MAX_VALUE;
        ServerInfo powerest = null;
        ServerInfo minRt = null;
        int size = ServerInfoHolder.SERVER_INFOS.size();
        for (int i = 0; i < size; i++) {
            ServerInfo value = ServerInfoHolder.SERVER_INFOS.get(i);
            int rt = value.getAvgResponseTime();
            if (minAvgResponseTime > rt) {
                minAvgResponseTime = rt;
                minRt = value;
            }
            if (powerest == null || minAvgResponseTime > rt) {


                if (value.hasSurplusThreadNum()) {
                    powerest = value;
                }
            }
        }

        if (powerest != null) {
            Elector.powerest = powerest;
        } else {
            Elector.powerest = minRt;
        }
        ELECT_LOCK.unlock();
    }

    public static ServerInfo loadPowerest() {
        if (powerest == null) {
            electPowerest1();
            if (ServerInfoHolder.SERVER_INFO_MAP.isEmpty()) {
                return null;
            }
            powerest = ServerInfoHolder.SERVER_INFO_MAP.values().iterator().next();
        }
        return powerest;
    }

}
