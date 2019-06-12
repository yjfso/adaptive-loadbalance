package com.aliware.tianchi.checker;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yinjianfeng
 * @date 2019/6/12
 */
public class ThreadPoolUtil {

    static List<Thread> getThreadsFromPool(ThreadPoolExecutor executor) {
        try {
            Field field = ThreadPoolExecutor.class.getDeclaredField("workers");
            field.setAccessible(true);

            Class clazz = Class.forName("java.util.concurrent.ThreadPoolExecutor$Worker");
            Field threadField = clazz.getDeclaredField("thread");
            threadField.setAccessible(true);

            return (List<Thread>) ((HashSet) field.get(executor)).stream().map(
                    (worker) -> {
                        try {
                            return (Thread) threadField.get(worker);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
            ).filter(Objects::nonNull).collect(Collectors.toCollection(() -> new LinkedList<>()));
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
