package com.afoxxvi.alopex.component.filter

import com.afoxxvi.alopex.component.notify.Notify
import com.afoxxvi.alopex.util.Condition
import com.afoxxvi.alopex.util.Pair

class FilterChain(private val packageName: String) {
    private val chain: List<ChainUnit>? = null

    fun handle(notification: Notify): Pair<Boolean, Boolean> {
        var notify = false
        var cancel = false
        if (chain != null) {
            for (unit in chain) {
                if (unit.condition!!.matches(notification)) {
                    if (unit.notify) {
                        notify = true
                    }
                    if (unit.cancel) {
                        cancel = true
                    }
                    if (unit.consume) {
                        break
                    }
                }
            }
        }
        return Pair(notify, cancel)
    }

    class ChainUnit {
        val consume = false
        val cancel = false
        val notify = false
        val condition: Condition<Notify>? = null
    }
}