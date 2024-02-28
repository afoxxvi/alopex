package com.afoxxvi.alopex.component.filter

import com.afoxxvi.alopex.component.notify.Notify
import com.afoxxvi.alopex.util.Condition

class NotifyCondition(subConditions: List<Condition<Notify>>, private val pattern: String, private val rule: Int)
    : Condition<Notify>(subConditions) {
    private var matchCount = 0
    override fun matches(o: Notify): Boolean {
        var res = false
        when (rule) {
            RULE_TITLE_CONTAINS -> res = o.title?.contains(pattern) == true
            RULE_TITLE_MATCHES -> res = o.title?.matches(Regex(pattern)) == true
            RULE_TEXT_CONTAINS -> res = o.text?.contains(pattern) == true
            RULE_TEXT_MATCHES -> res = o.text?.matches(Regex(pattern)) == true
            else -> {}
        }
        if (res) {
            matchCount++
        }
        return res
    }

    companion object {
        const val RULE_TITLE_CONTAINS = 1
        const val RULE_TITLE_MATCHES = 2
        const val RULE_TEXT_CONTAINS = 3
        const val RULE_TEXT_MATCHES = 4
    }
}