package com.afoxxvi.alopex.util

abstract class Condition<T>(protected var subConditions: List<Condition<T>>?) {
    abstract fun matches(o: T): Boolean
}