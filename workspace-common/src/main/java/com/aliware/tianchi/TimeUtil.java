package com.aliware.tianchi;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yinjianfeng
 * @date 2019/7/13
 */
public class TimeUtil {

    public static String getNow() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        return simpleDateFormat.format(new Date());
    }
}
