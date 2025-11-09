package com.afoxxvi.alopex.component.filter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream

fun FiltersProto.toObject(): List<FilterGroup> {
    val filterGroups = mutableListOf<FilterGroup>()
    for (group in this.groupList) {
        filterGroups.add(group.toObject())
    }
    return filterGroups
}

fun List<FilterGroup>.toProto(): FiltersProto {
    val builder = FiltersProto.newBuilder()
    for (group in this) {
        builder.addGroup(group.toProto())
    }
    return builder.build()
}

fun FilterGroupProto.toObject(): FilterGroup {
    val filterRules = mutableListOf<FilterRule>()
    for (rule in this.ruleList) {
        val matchList = mutableListOf<FilterRule.Match>()
        for (match in rule.matchList) {
            matchList.add(FilterRule.Match(FilterRule.Match.Type.valueOf(match.type.name), match.pattern))
        }
        filterRules.add(FilterRule(rule.name, matchList, rule.notify, rule.cancel, rule.consume))
    }
    return FilterGroup(this.appPackage, filterRules)
}

fun FilterGroup.toProto(): FilterGroupProto {
    val builder = FilterGroupProto.newBuilder()
    builder.appPackage = this.appPackage
    for (rule in this.filterRules) {
        val ruleBuilder = FilterRuleProto.newBuilder()
        ruleBuilder.name = rule.name
        ruleBuilder.notify = rule.notify
        ruleBuilder.cancel = rule.cancel
        ruleBuilder.consume = rule.consume
        for (match in rule.match) {
            val matchBuilder = FilterRuleProto.MatchProto.newBuilder()
            matchBuilder.type = FilterRuleProto.MatchProto.TypeProto.valueOf(match.type.name)
            matchBuilder.pattern = match.pattern
            ruleBuilder.addMatch(matchBuilder)
        }
        builder.addRule(ruleBuilder)
    }
    return builder.build()
}

object FiltersSerializer : Serializer<FiltersProto> {
    override val defaultValue: FiltersProto
        get() = FiltersProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FiltersProto {
        return FiltersProto.parseFrom(input)
    }

    override suspend fun writeTo(t: FiltersProto, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.filtersDataStore: DataStore<FiltersProto>
    by dataStore(
        fileName = "filters.pb",
        serializer = FiltersSerializer,
    )