package com.afoxxvi.alopex.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class FoxTools {
    public static LocalDateTime getLocalDateTimeFromMills(long mills) {
        return LocalDateTime.ofEpochSecond(mills / 1000, (int) (mills % 1000 * 1000000),
                ZoneOffset.ofTotalSeconds(Calendar.getInstance().get(Calendar.ZONE_OFFSET) / 1000));
    }

    public static ScheduledExecutorService getSingleExecutorService() {
        return new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            final ThreadFactory factory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                return factory.newThread(r);
            }
        });
    }
}
