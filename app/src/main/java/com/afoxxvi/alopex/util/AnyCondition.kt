package com.afoxxvi.alopex.util

class AnyCondition<T>(subConditions: List<Condition<T>>?) : Condition<T>(subConditions) {
    override fun matches(o: T) = subConditions != null && subConditions!!.any { it.matches(o) }
}