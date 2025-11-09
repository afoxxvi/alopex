package com.afoxxvi.alopex.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.FilterGroup
import com.afoxxvi.alopex.component.filter.Filters
import com.afoxxvi.alopex.databinding.FragmentFilterBinding
import com.afoxxvi.alopex.databinding.LiFilterGroupBinding
import com.afoxxvi.alopex.ui.dialog.FilterGroupDialog

class FilterFragment : Fragment() {
    private lateinit var binding: FragmentFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)
        binding = FragmentFilterBinding.bind(view)
        setupUI()
        return view
    }

    private fun setupUI() {
        binding.recyclerBroadcast.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerBroadcast.adapter = Adapter()
        binding.fabAddFilter.setOnClickListener {
            object : FilterGroupDialog(requireContext(), "New Filter Group", FilterGroup("")) {
                override fun onConfirm() {
                    val adapter = binding.recyclerBroadcast.adapter!!
                    val oldSize = adapter.itemCount
                    super.onConfirm()
                    if (adapter.itemCount > oldSize) {
                        adapter.notifyItemInserted(adapter.itemCount)
                    }
                }
            }.show()
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: LiFilterGroupBinding = LiFilterGroupBinding.bind(itemView)
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.li_filter_group, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val filter = Filters.getFilterGroups()[position]
            holder.binding.filterGroup = filter
            holder.binding.root.setOnClickListener {
                object : FilterGroupDialog(holder.binding.root.context, "Edit Filter", filter) {
                    override fun onConfirm() {
                        super.onConfirm()
                        notifyItemChanged(Filters.getFilterGroups().indexOf(filter))
                    }

                    override fun onMiddle() {
                        super.onMiddle()
                        // Ask for delete
                        AlertDialog.Builder(context)
                            .setTitle("Confirm")
                            .setMessage("Delete this group?")
                            .setPositiveButton("Delete") { _, _ ->
                                val index = Filters.getFilterGroups().indexOf(filter)
                                Filters.deleteFilterGroup(context, filter)
                                notifyItemRemoved(index)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                }.setMiddleVisible(true).setMiddleText("Delete").show()
            }
        }

        override fun getItemCount(): Int {
            return Filters.getFilterGroups().size
        }
    }

    companion object {
        fun newInstance(): FilterFragment {
            return FilterFragment()
        }
    }
}