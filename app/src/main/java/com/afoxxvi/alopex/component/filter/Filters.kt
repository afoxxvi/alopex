package com.afoxxvi.alopex.component.filter

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object Filters {
    private val filterGroups: MutableList<FilterGroup> = mutableListOf()

    fun init(context: Context) {
        filterGroups.clear()
        runBlocking {
            context.filtersDataStore.data
                .map { proto -> proto.toObject() }
                .firstOrNull()?.let { filterGroups.addAll(it) }
        }
    }

    suspend fun saveAllToFile(context: Context) {
        context.filtersDataStore.updateData {
            filterGroups.toProto()
        }
    }

    fun getFilterGroups(): List<FilterGroup> {
        return filterGroups
    }

    fun saveFilterGroup(context: Context, filterGroup: FilterGroup) {
        val index = filterGroups.indexOf(filterGroup)
        if (index in filterGroups.indices) {
            runBlocking {
                context.filtersDataStore.updateData {
                    it.toBuilder().setGroup(index, filterGroup.toProto()).build()
                }
            }
        } else {
            filterGroups.add(filterGroup)
            runBlocking {
                context.filtersDataStore.updateData {
                    it.toBuilder().addGroup(filterGroup.toProto()).build()
                }
            }
        }
    }

    fun deleteFilterGroup(context: Context, filterGroup: FilterGroup) {
        val index = filterGroups.indexOf(filterGroup)
        if (index in filterGroups.indices) {
            filterGroups.removeAt(index)
            runBlocking {
                context.filtersDataStore.updateData {
                    it.toBuilder().removeGroup(index).build()
                }
            }
        }
    }

    class FilterResult {
        var doNotify = false
        var doCancel = false
        var doConsume = false
    }

    fun passNotification(appPackage: String, title: String?, content: String?): FilterResult {
        val result = FilterResult()
        filterGroups.forEach { group ->
            if (group.isApp(appPackage)) {
                group.passNotification(title, content, result)
                if (result.doConsume) {
                    return result
                }
            }
        }
        return result
    }
}