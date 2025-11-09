package com.afoxxvi.alopex.component.filter

import android.content.Context
import com.afoxxvi.alopex.R

/**
 * @param consume: Once match, prevent this notification being handled by other rules.
 */
class FilterRule(
    var name: String,
    var forTitle: List<Pattern>,
    var forContent: List<Pattern>,
    var notify: Boolean,
    var cancel: Boolean,
    var consume: Boolean,
) {
    fun description(context: Context): String {
        return buildString {
            if (forTitle.isNotEmpty()) {
                append(context.getString(R.string.filter_rule_desc_title))
                append(" ")
                append(forTitle.joinToString("|") { it.pattern })
            }
            if (forContent.isNotEmpty()) {
                if (isNotEmpty()) {
                    append("; ")
                }
                append(context.getString(R.string.filter_rule_desc_content))
                append(" ")
                append(forContent.joinToString("|") { it.pattern })
            }
            if (isEmpty()) {
                append(context.getString(R.string.filter_rule_desc_empty))
            }
        }
    }

    fun matches(title: String?, content: String?): Boolean {
        if (title == null && forTitle.isNotEmpty()) {
            return false
        }
        if (content == null && forContent.isNotEmpty()) {
            return false
        }
        if (forTitle.isNotEmpty() && forTitle.none { pat -> pat.matches(title!!) }) {
            return false
        }
        if (forContent.isNotEmpty() && forContent.none { pat -> pat.matches(content!!) }) {
            return false
        }
        return true
    }

    class Pattern(
        var pattern: String,
        var isRegex: Boolean,
    ) {
        fun matches(text: String): Boolean {
            return if (isRegex) {
                text.contains(Regex(pattern))
            } else {
                text.contains(pattern)
            }
        }
    }
}