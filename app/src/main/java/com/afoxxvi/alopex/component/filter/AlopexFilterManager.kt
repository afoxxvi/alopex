package com.afoxxvi.alopex.component.filter

import android.content.Context
import android.util.Log
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.component.filter.AlopexFilter.Companion.fromJsonObject
import com.afoxxvi.alopex.component.notify.Notify
import com.afoxxvi.alopex.util.FoxFiles.inputFile
import com.afoxxvi.alopex.util.FoxFiles.outputFile
import com.afoxxvi.alopex.util.FoxTools.singleExecutorService
import com.afoxxvi.alopex.util.Triplet
import org.json.JSONArray
import java.io.File
import java.util.concurrent.TimeUnit

object AlopexFilterManager {
    private var filterList: MutableList<AlopexFilter> = mutableListOf()
    val filters: MutableList<AlopexFilter>
        get() {
            return filterList
        }

    fun init(context: Context) {
        filters.clear()
        val file = File(context.filesDir, "filter.json")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            val doc = inputFile(context, "filter.json")
            val array = JSONArray(doc)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                if (obj != null) {
                    filters.add(fromJsonObject(obj))
                }
            }
        }
        singleExecutorService.scheduleWithFixedDelay({ save(context) }, 3, 300, TimeUnit.SECONDS)
    }

    fun save(context: Context?) {
        val array = JSONArray()
        for (filter in filters) {
            array.put(filter.toJsonObject())
        }
        outputFile(context!!, "filter.json", array.toString())
        Log.i(Alopex.TAG, "filter saved")
    }

    fun isFiltered(packageName: String, notify: Notify, count: Boolean): Triplet<Boolean, Boolean, Boolean> {
        return isFiltered(packageName, notify.title, notify.text, count)
    }

    /**
     * @return do notify, do cancel, is filtered
     */
    fun isFiltered(
        packageName: String,
        title: String?,
        text: String?,
        count: Boolean
    ): Triplet<Boolean, Boolean, Boolean> {
        for (filter in filters) {
            if (filter.packageName == packageName) {
                return if (filter.isFiltered(title!!, text!!, count)) {
                    Triplet(false, filter.cancelFiltered, true)
                } else {
                    Triplet(filter.notifyUnfiltered, b = false, c = false)
                }
            }
        }
        return Triplet(false, b = false, c = false)
    }
}