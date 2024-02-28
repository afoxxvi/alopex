package com.afoxxvi.alopex.ui.dialog

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.component.filter.AlopexFilter
import com.afoxxvi.alopex.component.filter.NotifyRule
import com.afoxxvi.alopex.databinding.DialogRuleBinding
import com.afoxxvi.alopex.util.Pair
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.function.Consumer

open class MatchRuleDialog(context: Context?, match: AlopexFilter.Match) : BaseDialog(context!!, "Edit match rule") {
    private val match: AlopexFilter.Match
    private val binding: DialogRuleBinding
    private val list: MutableList<Pair<NotifyRule, String>>

    init {
        binding = DialogRuleBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        this.match = match
        binding.editName.setText(match.name)
        list = ArrayList()
        match.ruleList.forEach { pair ->
            list.add(pair.copy())
        }
        list.forEach(Consumer { p: Pair<NotifyRule, String> -> addChip(binding.chipsRuleList, p, list) })
        val strings: MutableList<String> = ArrayList()
        for (rule in NotifyRule.values()) {
            strings.add(rule.name)
        }
        binding.spinnerRuleMethod.adapter = ArrayAdapter(context!!, R.layout.simple_spinner_dropdown_item, strings)
        binding.buttonAddNewRule.setOnClickListener {
            val pattern = binding.editNewRulePattern.text.toString()
            if (pattern.isEmpty()) {
                Alopex.showToast(context, "Enter pattern", Toast.LENGTH_SHORT)
                binding.editNewRulePattern.requestFocus()
                return@setOnClickListener
            }
            binding.editNewRulePattern.setText("")
            val pair = Pair(NotifyRule.valueOf(binding.spinnerRuleMethod.selectedItem.toString()), pattern)
            list.add(pair)
            addChip(binding.chipsRuleList, pair, list)
        }
    }

    override fun onConfirm() {
        val name = binding.editName.text.toString()
        if (name.isEmpty()) {
            Alopex.showToast(context, "Enter rule name", Toast.LENGTH_SHORT)
            binding.editName.requestFocus()
            return
        }
        match.name = name
        match.ruleList = list
        super.onConfirm()
    }

    private fun addChip(group: ChipGroup, pair: Pair<NotifyRule, String>, list: MutableList<Pair<NotifyRule, String>>) {
        val chip = Chip(context)
        chip.text = String.format("[%s]%s", pair.a.display(), pair.b)
        chip.setOnClickListener { chip.isCloseIconVisible = !chip.isCloseIconVisible }
        chip.setOnCloseIconClickListener {
            object : BaseDialog(context, "Delete rule?") {
                override fun onConfirm() {
                    list.remove(pair)
                    group.removeView(chip)
                    super.onConfirm()
                }
            }.setButtonText("Yes", "No").show()
        }
        group.addView(chip, ChipGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
}