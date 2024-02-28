package com.afoxxvi.alopex.component.filter

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.afoxxvi.alopex.BR
import com.afoxxvi.alopex.util.Pair
import org.json.JSONArray
import org.json.JSONObject

class AlopexFilter @JvmOverloads constructor(
    @get:Bindable var packageName: String,
    @get:Bindable var cancelFiltered: Boolean = false,
    @get:Bindable var notifyUnfiltered: Boolean = false,
    var matchList: List<Match>? = ArrayList()
) : BaseObservable() {

    @get:Bindable
    val actionText: String
        get() = if (cancelFiltered) if (notifyUnfiltered) "cancel | notify" else "cancel" else if (notifyUnfiltered) "notify" else "none"

    @get:Bindable
    val blacklistText: String
        get() {
            if (matchList == null || matchList!!.isEmpty()) {
                return "<>"
            }
            val builder = StringBuilder()
            for (i in matchList!!.indices) {
                if (i > 0) {
                    builder.append('\n')
                }
                val match = matchList!![i]
                builder.append(match.name).append(" (").append(match.matchCount).append(")")
            }
            return builder.toString()
        }

    fun isFiltered(title: String, text: String, doCount: Boolean): Boolean {
        for (match in matchList!!) {
            if (match.matches(title, text, doCount)) {
                if (doCount) {
                    notifyPropertyChanged(BR.blacklistText)
                }
                return true
            }
        }
        return false
    }

    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("packageName", packageName)
        jsonObject.put("cancelFiltered", cancelFiltered)
        jsonObject.put("notifyUnfiltered", notifyUnfiltered)
        val array = JSONArray()
        for (match in matchList!!) {
            array.put(match.toJsonObject())
        }
        jsonObject.put("matchList", array)
        return jsonObject
    }

    class Match @JvmOverloads constructor(
        var name: String,
        var ruleList: List<Pair<NotifyRule, String>> = ArrayList(),
        var matchCount: Int = 0
    ) {
        fun matches(title: String, text: String, doCount: Boolean): Boolean {
            for ((a, b) in ruleList) {
                val res: Boolean = when (a) {
                    NotifyRule.TITLE_CONTAINS -> title.contains(b)
                    NotifyRule.TITLE_MATCHES -> title.matches(Regex(b))
                    NotifyRule.TEXT_CONTAINS -> text.contains(b)
                    NotifyRule.TEXT_MATCHES -> text.matches(Regex(b))
                }
                if (!res) {
                    return false
                }
            }
            if (doCount) {
                matchCount++
            }
            return true
        }

        fun toJsonObject(): JSONObject {
            val jsonObject = JSONObject()
            val array = JSONArray()
            for ((a, b) in ruleList) {
                array.put(a.toString() + "\u0000" + b)
            }
            jsonObject.put("name", name)
            jsonObject.put("ruleList", array)
            jsonObject.put("matchCount", matchCount)
            return jsonObject
        }

        companion object {
            fun copyOf(match: Match): Match {
                return Match(match.name, match.ruleList, match.matchCount)
            }

            fun fromJsonObject(`object`: JSONObject): Match {
                val array = `object`.optJSONArray("ruleList")
                val list: MutableList<Pair<NotifyRule, String>> = ArrayList()
                val name = `object`.optString("name", "unnamed")
                val matchCount = `object`.optInt("matchCount", 0)
                if (array != null) {
                    for (i in 0 until array.length()) {
                        val sp = array.optString(i, "").split("\u0000".toRegex()).toTypedArray()
                        if (sp.size > 1) {
                            list.add(Pair(NotifyRule.valueOf(sp[0]), sp[1]))
                        }
                    }
                }
                return Match(name, list, matchCount)
            }
        }
    }

    companion object {
        @JvmStatic
        fun fromJsonObject(jsonObject: JSONObject): AlopexFilter {
            val pkg = jsonObject.optString("packageName")
            val b1 = jsonObject.optBoolean("cancelFiltered", false)
            val b2 = jsonObject.optBoolean("notifyUnfiltered", false)
            val ls: MutableList<Match> = ArrayList()
            val a1 = jsonObject.optJSONArray("matchList")
            if (a1 != null) {
                for (i in 0 until a1.length()) {
                    val `object` = a1.optJSONObject(i)
                    if (`object` != null) {
                        ls.add(Match.fromJsonObject(`object`))
                    }
                }
            }
            return AlopexFilter(pkg, b1, b2, ls)
        }
    }
}