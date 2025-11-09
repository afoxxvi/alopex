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

open class FilterRuleDialog(context: Context, title: String?, private val filterRule: FilterRule) : BaseDialog(context, title) {
    private val binding: DialogFilterRuleBinding
    private val matchList: MutableList<FilterRule.Match> = filterRule.match.toMutableList()

    init {
        binding = DialogFilterRuleBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        setupUI()
        loadProperties()
    }

    private fun setupUI() {
        binding.recyclerMatchList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerMatchList.adapter = Adapter()
        binding.buttonAddMatch.setOnClickListener {
            matchList.add(FilterRule.Match(FilterRule.Match.Type.TITLE_CONTAIN, ""))
            binding.recyclerMatchList.adapter?.notifyItemInserted(matchList.size - 1)
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
        filterRule.match.clear()
        matchList.removeIf { it.pattern.isEmpty() }
        filterRule.match.addAll(matchList)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = LiFilterRuleMatchBinding.bind(itemView)
        lateinit var match: FilterRule.Match
        var matchTypeIndex: Int = 0
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_filter_rule_match, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.match = matchList[position]
            val match = holder.match
            holder.matchTypeIndex = match.type.ordinal
            holder.binding.chipType.setText(match.type.resId)
            holder.binding.labelPattern.setText(match.pattern)
            holder.binding.chipType.isChecked = true
            holder.binding.chipType.isCheckable = false
            holder.binding.chipType.setOnClickListener {
                holder.matchTypeIndex = (holder.matchTypeIndex + 1) % FilterRule.Match.Type.values().size
                holder.binding.chipType.setText(FilterRule.Match.Type.values()[holder.matchTypeIndex].resId)
                match.type = FilterRule.Match.Type.values()[holder.matchTypeIndex]
            }
            holder.binding.labelPattern.doAfterTextChanged { text: Editable? ->
                match.pattern = text.toString()
            }
            binding.root.setOnLongClickListener {
                // Ask for delete
                AlertDialog.Builder(context)
                    .setTitle("Confirm")
                    .setMessage("Delete this match?")
                    .setPositiveButton("Delete") { _, _ ->
                        val index = matchList.indexOf(match)
                        matchList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int {
            return matchList.size
        }
    }
}