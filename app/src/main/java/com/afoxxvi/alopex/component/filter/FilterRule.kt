package com.afoxxvi.alopex.component.filter

import com.afoxxvi.alopex.R

/**
 * @param consume: Once match, prevent this notification being handled by other rules.
 */
class FilterRule(
    var name: String,
    val match: MutableList<Match>,
    var notify: Boolean,
    var cancel: Boolean,
    var consume: Boolean,
) {
    fun matches(title: String?, content: String?) = match.any { it.matches(title, content) }

    fun match(title: String?, content: String?): Match? = match.firstOrNull { it.matches(title, content) }

    class Match(var type: Type, var pattern: String) {
        fun matches(title: String?, content: String?): Boolean {
            return when (type) {
                Type.TITLE_CONTAIN -> title != null && title.contains(pattern)
                Type.TITLE_MATCH -> title != null && title.matches(pattern.toRegex())
                Type.CONTENT_CONTAIN -> content != null && content.contains(pattern)
                Type.CONTENT_MATCH -> content != null && content.matches(pattern.toRegex())
            }
        }

        enum class Type(val resId: Int) {
            TITLE_CONTAIN(R.string.filter_rule_match_type_title_contain),
            TITLE_MATCH(R.string.filter_rule_match_type_title_match),
            CONTENT_CONTAIN(R.string.filter_rule_match_type_content_contain),
            CONTENT_MATCH(R.string.filter_rule_match_type_content_match),
        }
    }
}