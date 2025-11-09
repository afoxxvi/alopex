package com.afoxxvi.alopex.component.filter

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class FilterGroup(
    @get:Bindable var appPackage: String,
    val filterRules: MutableList<FilterRule> = mutableListOf(),
) : BaseObservable() {
    @get:Bindable
    val rules: String
        get() {
            if (filterRules.isEmpty()) {
                return "<空>"
            }
            val builder = StringBuilder()
            for (i in filterRules.indices) {
                if (i > 0) {
                    builder.append('\n')
                }
                val rule = filterRules[i]
                builder.append(rule.name).append(" (").append('-').append(")")
            }
            return builder.toString()
        }

    fun isApp(appPackage: String): Boolean {
        return this.appPackage == appPackage
    }

    fun passNotification(title: String?, content: String?, result: Filters.FilterResult) {
        filterRules.forEach { rule ->
            if (rule.matches(title, content)) {
                if (rule.notify) result.doNotify = true
                if (rule.cancel) result.doCancel = true
                if (rule.consume) result.doConsume = true
                if (rule.consume) {
                    return
                }
            }
        }
    }
}