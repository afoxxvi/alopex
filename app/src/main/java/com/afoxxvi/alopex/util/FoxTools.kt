package com.afoxxvi.alopex.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory

object FoxTools {
    fun getLocalDateTimeFromMills(mills: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(
            mills / 1000, (mills % 1000 * 1000000).toInt(),
            ZoneOffset.ofTotalSeconds(Calendar.getInstance()[Calendar.ZONE_OFFSET] / 1000)
        )
    }

    val singleExecutorService: ScheduledExecutorService
        get() = ScheduledThreadPoolExecutor(1, object : ThreadFactory {
            val factory = Executors.defaultThreadFactory()
            override fun newThread(r: Runnable): Thread {
                return factory.newThread(r)
            }
        })
}