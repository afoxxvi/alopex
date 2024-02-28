package com.afoxxvi.alopex.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.component.filter.AlopexFilter
import com.afoxxvi.alopex.component.filter.AlopexFilterManager
import com.afoxxvi.alopex.databinding.DialogFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

open class FilterDialog(context: Context, private val filter: AlopexFilter) : BaseDialog(context, "Edit filter") {
    private val binding: DialogFilterBinding
    private val matchList: MutableList<AlopexFilter.Match>

    init {
        matchList = ArrayList()
        filter.matchList!!.forEach { matchList.add(AlopexFilter.Match.copyOf(it)) }
        binding = DialogFilterBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        binding.editPackage.setText(filter.packageName)
        binding.editPackage.setOnClickListener {
            object : AppListDialog(context) {
                override fun onSelect(appInfo: AppInfo) {
                    super.onSelect(appInfo)
                    binding.editPackage.setText(appInfo.packageName)
                    dialog?.dismiss()
                }
            }.show()
        }
        binding.checkCancel.isChecked = filter.cancelFiltered
        binding.checkNotify.isChecked = filter.notifyUnfiltered
        matchList.forEach { addChip(binding.chipsBlacklist, it, matchList) }
        binding.buttonAddBlacklist.setOnClickListener { addMatchRule(context, binding.editNewBlacklist, matchList, binding.chipsBlacklist, "enter match name.") }
    }

    private fun addMatchRule(context: Context, editFrom: EditText, matchList: MutableList<AlopexFilter.Match>, targetGroup: ChipGroup, emptyMsg: String) {
        val text = editFrom.text.toString()
        if (text.isNotEmpty()) {
            val match = AlopexFilter.Match(text)
            editFrom.setText("")
            object : MatchRuleDialog(context, match) {
                override fun onConfirm() {
                    super.onConfirm()
                    matchList.add(match)
                    this@FilterDialog.addChip(targetGroup, match, matchList)
                }
            }.show()
        } else {
            Alopex.showToast(context, emptyMsg, Toast.LENGTH_SHORT)
            editFrom.requestFocus()
        }
    }

    override fun onConfirm() {
        val manager = AlopexFilterManager
        val pkgName = binding.editPackage.text.toString()
        if (pkgName.isEmpty()) {
            binding.editPackage.requestFocus()
            Alopex.showToast(context, "package name is empty.", Toast.LENGTH_SHORT)
            return
        }
        filter.packageName = pkgName
        filter.matchList = matchList
        filter.cancelFiltered = binding.checkCancel.isChecked
        filter.notifyUnfiltered = binding.checkNotify.isChecked
        filter.notifyChange()
        if (!manager.filters.contains(filter)) {
            manager.filters.add(filter)
        }
        manager.save(context)
        dialog?.dismiss()
    }

    @SuppressLint("DefaultLocale")
    private fun addChip(target: ChipGroup, match: AlopexFilter.Match, list: MutableList<AlopexFilter.Match>) {
        val chip = Chip(context)
        chip.text = String.format("%s(%d)", match.name, match.matchCount)
        chip.setOnClickListener { chip.isCloseIconVisible = !chip.isCloseIconVisible }
        chip.setOnLongClickListener {
            MatchRuleDialog(context, match).show()
            true
        }
        chip.setOnCloseIconClickListener {
            object : BaseDialog(context, "Delete match rule?") {
                override fun onConfirm() {
                    val iterator: MutableIterator<AlopexFilter.Match> = list.listIterator()
                    while (iterator.hasNext()) {
                        val m = iterator.next()
                        if (m === match) {
                            iterator.remove()
                            target.removeView(chip)
                            break
                        }
                    }
                    super.onConfirm()
                }
            }.setButtonText("Yes", "No").show()
        }
        target.addView(chip)
    }

    override fun show() {
        super.show()
        dialog?.setCancelable(false)
    }
}