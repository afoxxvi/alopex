package com.afoxxvi.alopex.component.filter

import java.util.*

enum class NotifyRule {
    TITLE_CONTAINS, TITLE_MATCHES, TEXT_CONTAINS, TEXT_MATCHES;

    fun display(): String {
        val raw = name
        val builder = StringBuilder()
        val sp = raw.split("_".toRegex()).toTypedArray()
        for (i in sp.indices) {
            if (i == 0) {
                builder.append(sp[i].lowercase(Locale.getDefault()))
            } else {
                builder.append(sp[i][0]).append(sp[i].substring(1).lowercase(Locale.getDefault()))
            }
        }
        return builder.toString()
    }
}