package com.afoxxvi.alopex.ui.dialog

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.FilterRule
import com.afoxxvi.alopex.databinding.DialogFilterRuleBinding
import com.afoxxvi.alopex.databinding.LiFilterRuleMatchBinding
import com.afoxxvi.alopex.util.entity.MutablePair
import com.afoxxvi.alopex.util.entity.MutableUnit
import com.afoxxvi.alopex.util.entity.toMutableUnit

open class FilterRuleDialog(context: Context, title: String?, private val filterRule: FilterRule) : BaseDialog(context, title) {
    private val binding: DialogFilterRuleBinding = DialogFilterRuleBinding.inflate(LayoutInflater.from(context))
    private val forTitle: MutableList<FilterRule.Pattern> = filterRule.forTitle.toMutableList()
    private val forContent: MutableList<FilterRule.Pattern> = filterRule.forContent.toMutableList()

    init {
        setContent(binding.root)
        setupUI()
        loadProperties()
    }

    private fun setupUI() {
        binding.recyclerForTitle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerForTitle.adapter = Adapter(forTitle)
        binding.buttonAddForTitle.setOnClickListener {
            forTitle.add(FilterRule.Pattern("", false))
            binding.recyclerForTitle.adapter?.notifyItemInserted(forTitle.size - 1)
        }
        binding.recyclerForContent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerForContent.adapter = Adapter(forContent)
        binding.buttonAddForContent.setOnClickListener {
            forContent.add(FilterRule.Pattern("", false))
            binding.recyclerForContent.adapter?.notifyItemInserted(forContent.size - 1)
        }
    }

    private fun loadProperties() {
        binding.editName.setText(filterRule.name)
        binding.chipNotify.isChecked = filterRule.notify
        binding.chipCancel.isChecked = filterRule.cancel
        binding.chipConsume.isChecked = filterRule.consume
    }

    override fun onConfirm() {
        super.onConfirm()
        filterRule.name = binding.editName.text.toString()
        filterRule.notify = binding.chipNotify.isChecked
        filterRule.cancel = binding.chipCancel.isChecked
        filterRule.consume = binding.chipConsume.isChecked
        filterRule.forTitle = forTitle
        filterRule.forContent = forContent
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = LiFilterRuleMatchBinding.bind(itemView)
        var pattern: FilterRule.Pattern = FilterRule.Pattern("", false)
    }

    inner class Adapter(private val backendList: MutableList<FilterRule.Pattern>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_filter_rule_match, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.pattern = backendList[position]
            holder.binding.labelPattern.setText(holder.pattern.pattern)
            holder.binding.chipRegex.isChecked = holder.pattern.isRegex
            holder.binding.chipRegex.setOnCheckedChangeListener { _, isChecked ->
                holder.pattern.isRegex = isChecked
            }
            holder.binding.labelPattern.doAfterTextChanged { text: Editable? ->
                holder.pattern.pattern = text.toString()
            }
            binding.root.setOnLongClickListener {
                // Ask for delete
                AlertDialog.Builder(context)
                    .setTitle("Confirm")
                    .setMessage("Delete this match?")
                    .setPositiveButton("Delete") { _, _ ->
                        val index = backendList.indexOf(holder.pattern)
                        backendList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int {
            return backendList.size
        }
    }
}