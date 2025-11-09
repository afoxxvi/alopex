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
        filterRules.add(
            FilterRule(
                rule.name,
                rule.forTitleList.map { pattern ->
                    FilterRule.Pattern(pattern.pattern, pattern.isRegex)
                },
                rule.forContentList.map { pattern ->
                    FilterRule.Pattern(pattern.pattern, pattern.isRegex)
                },
                rule.notify,
                rule.cancel,
                rule.consume,
            )
        )
    }
    return FilterGroup(this.appPackage, filterRules)
}

fun FilterGroup.toProto(): FilterGroupProto {
    val builder = FilterGroupProto.newBuilder()
    builder.appPackage = this.appPackage
    for (rule in this.filterRules) {
        val ruleBuilder = FilterRuleProto.newBuilder()
        ruleBuilder.name = rule.name
        ruleBuilder.clearForTitle()
        ruleBuilder.addAllForTitle(rule.forTitle.map { pattern ->
            FilterRuleProto.Pattern.newBuilder().setPattern(pattern.pattern).setIsRegex(pattern.isRegex).build()
        })
        ruleBuilder.clearForContent()
        ruleBuilder.addAllForContent(rule.forContent.map { pattern ->
            FilterRuleProto.Pattern.newBuilder().setPattern(pattern.pattern).setIsRegex(pattern.isRegex).build()
        })
        ruleBuilder.notify = rule.notify
        ruleBuilder.cancel = rule.cancel
        ruleBuilder.consume = rule.consume
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