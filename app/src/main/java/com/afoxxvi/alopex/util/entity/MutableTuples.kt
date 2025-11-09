package com.afoxxvi.alopex.util.entity

data class MutableUnit<T>(var value: T)

data class MutablePair<A, B>(var first: A, var second: B)

data class MutableTriple<A, B, C>(var first: A, var second: B, var third: C)

fun <T> T.toMutableUnit() = MutableUnit(this)

fun <A, B> Pair<A, B>.toMutablePair() = MutablePair(first, second)

fun <A, B, C> Triple<A, B, C>.toMutableTriple() = MutableTriple(first, second, third)
