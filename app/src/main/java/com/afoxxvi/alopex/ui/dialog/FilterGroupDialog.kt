package com.afoxxvi.alopex.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.FilterGroup
import com.afoxxvi.alopex.component.filter.FilterRule
import com.afoxxvi.alopex.component.filter.Filters
import com.afoxxvi.alopex.databinding.DialogFilterGroupBinding
import com.afoxxvi.alopex.databinding.LiFilterRuleBinding
import kotlinx.coroutines.runBlocking

open class FilterGroupDialog(context: Context, title: String?, private val filterGroup: FilterGroup) : BaseDialog(context, title) {
    private val binding: DialogFilterGroupBinding
    private val ruleList: MutableList<FilterRule> = filterGroup.filterRules.toMutableList()

    init {
        binding = DialogFilterGroupBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        setupUI()
        loadProperties()
    }

    private fun setupUI() {
        binding.recyclerRuleList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerRuleList.adapter = Adapter()

        binding.editPackage.setText(filterGroup.appPackage)
        binding.editPackage.setOnClickListener {
            object : AppListDialog(context) {
                override fun onSelect(appInfo: AppInfo) {
                    super.onSelect(appInfo)
                    binding.editPackage.setText(appInfo.packageName)
                    dialog?.dismiss()
                }
            }.show()
        }

        binding.buttonAddRule.setOnClickListener {
            val rule = FilterRule("New Filter", mutableListOf(), notify = false, cancel = false, consume = false)
            ruleList.add(rule)
            binding.recyclerRuleList.adapter?.notifyItemInserted(ruleList.size - 1)
        }
    }

    private fun loadProperties() {
        binding.editPackage.setText(filterGroup.appPackage)
    }

    override fun onConfirm() {
        super.onConfirm()
        val appPackage = binding.editPackage.text.toString()
        if (appPackage.isEmpty()) {
            binding.editPackage.requestFocus()
            Alopex.showToast(context, "package name is empty.", Toast.LENGTH_SHORT)
            return
        }
        filterGroup.appPackage = appPackage
        filterGroup.filterRules.clear()
        filterGroup.filterRules.addAll(ruleList)
        filterGroup.notifyChange()
        Filters.saveFilterGroup(context, filterGroup)
        Alopex.showToast(context, "Filter group saved", Toast.LENGTH_SHORT)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = LiFilterRuleBinding.bind(itemView)
        lateinit var rule: FilterRule
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.li_filter_rule, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val rule = ruleList[position]
            holder.rule = rule
            holder.binding.labelRuleName.text = "Rule: ${rule.name}"
            holder.binding.labelRuleMatches.text = "Contains ${rule.match.size} match(es)"
            val action = mutableListOf<String>()
            if (rule.notify) action.add("Notify")
            if (rule.cancel) action.add("Cancel")
            if (rule.consume) action.add("Consume")
            if (action.isEmpty()) action.add("No action")
            holder.binding.labelRuleActions.text = action.joinToString(" | ")
            holder.binding.root.setOnClickListener {
                object : FilterRuleDialog(context, "Edit rule", rule) {
                    override fun onConfirm() {
                        super.onConfirm()
                        notifyItemChanged(ruleList.indexOf(rule))
                    }
                }.show()
            }
            holder.binding.root.setOnLongClickListener {
                // Ask for delete
                AlertDialog.Builder(context)
                    .setTitle("Confirm")
                    .setMessage("Delete this rule?")
                    .setPositiveButton("Delete") { _, _ ->
                        val index = ruleList.indexOf(rule)
                        ruleList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int {
            return ruleList.size
        }
    }
}