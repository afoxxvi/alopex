package com.afoxxvi.alopex

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.afoxxvi.alopex.util.FoxFiles
import org.apache.commons.io.input.ReversedLinesFileReader
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Alopex : BaseObservable() {
    const val TAG = "AlopexView"
    private const val LOG_MAX_DISPLAY = 40
    private val LOG_ARRAY = arrayOfNulls<String>(LOG_MAX_DISPLAY)
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val MONTH_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
    const val CHANNEL_ID = "ALOPEX_CHANNEL_1"

    var active = false
    var handlerNotification: Handler? = null
    private var lastConnect: LocalDateTime? = null
    private var lastDisconnect: LocalDateTime? = null
    private var logIndex = 0
    private var logStream: PrintStream? = null
    var properties: SharedPreferences? = null

    private var toast: Toast? = null

    //Bindable attributes
    @get:Bindable
    val logStrings: String
        get() {
            val builder = StringBuilder()
            for (i in 0 until LOG_MAX_DISPLAY) {
                val next = LOG_ARRAY[(logIndex - 1 - i + LOG_MAX_DISPLAY) % LOG_MAX_DISPLAY]
                    ?: break
                if (i > 0) {
                    builder.append('\n')
                }
                builder.append(next)
            }
            return builder.toString()
        }

    @get:Bindable
    val lastConnectStr: String
        get() = if (lastConnect == null) "No connect event" else DATE_TIME_FORMATTER.format(lastConnect)

    @get:Bindable
    val lastDisconnectStr: String
        get() = if (lastDisconnect == null) "No disconnect event" else DATE_TIME_FORMATTER.format(
            lastDisconnect
        )

    fun init(context: Context) {
        val logFile = File(context.filesDir, "logs.log")
        try {
            val reader = ReversedLinesFileReader(logFile, StandardCharsets.UTF_8)
            val lines = reader.readLines(LOG_MAX_DISPLAY)
            for (i in lines.indices) {
                LOG_ARRAY[i] = lines[lines.size - 1 - i]
            }
            logIndex = lines.size % LOG_MAX_DISPLAY
        } catch (e: IOException) {
            e.printStackTrace()
        }
        logStream = FoxFiles.getOutputStream(context, "logs.log")
        properties = context.getSharedPreferences("properties", Context.MODE_PRIVATE)
        lastConnect = LocalDateTime.parse(
            properties?.getString("lastConnect", "2022-01-01 12:00:00"),
            DATE_TIME_FORMATTER
        )
        lastDisconnect = LocalDateTime.parse(
            properties?.getString("lastDisconnect", "2022-01-01 12:00:00"),
            DATE_TIME_FORMATTER
        )
    }

    fun showToast(context: Context?, text: CharSequence?, duration: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, text, duration)
        toast?.show()
    }

    fun showToastShort(context: Context?, text: CharSequence?) {
        showToast(context, text, Toast.LENGTH_SHORT)
    }

    fun showToastLong(context: Context?, text: CharSequence?) {
        showToast(context, text, Toast.LENGTH_LONG)
    }

    fun log(content: String) {
        val str = "[" + MONTH_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + content
        LOG_ARRAY[logIndex] = str
        logIndex = (logIndex + 1) % LOG_MAX_DISPLAY
        logStream?.println(str)
    }

    fun notifyConnect() {
        log("connect")
        lastConnect = LocalDateTime.now()
        properties?.edit()?.putString("lastConnect", DATE_TIME_FORMATTER.format(lastConnect))?.apply()
        notifyChange()
    }

    fun notifyDisconnect() {
        log("disconnect")
        lastDisconnect = LocalDateTime.now()
        properties?.edit()?.putString("lastDisconnect", DATE_TIME_FORMATTER.format(lastDisconnect))?.apply()
        notifyChange()
    }
}