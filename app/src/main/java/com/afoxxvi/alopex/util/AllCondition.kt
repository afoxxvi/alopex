package com.afoxxvi.alopex.util

class AllCondition<T>(subConditions: List<Condition<T>>?) : Condition<T>(subConditions) {
    override fun matches(o: T) =
        subConditions != null && subConditions!!.isNotEmpty() && subConditions!!.all { it.matches(o) }
}