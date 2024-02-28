package com.afoxxvi.alopex.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.AlopexFilter
import com.afoxxvi.alopex.component.filter.AlopexFilterManager
import com.afoxxvi.alopex.databinding.FragmentFilterBinding
import com.afoxxvi.alopex.databinding.LiFilterInfoBinding
import com.afoxxvi.alopex.ui.dialog.FilterDialog

class FilterFragment : Fragment() {
    private var binding: FragmentFilterBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)
        binding = FragmentFilterBinding.bind(view)
        binding!!.recyclerBroadcast.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding!!.recyclerBroadcast.adapter = Adapter()
        binding!!.fabAddFilter.setOnClickListener {
            object : FilterDialog(requireContext(), AlopexFilter("")) {
                override fun onConfirm() {
                    val adapter = binding!!.recyclerBroadcast.adapter
                    var oldSize = -1
                    if (adapter != null) {
                        oldSize = adapter.itemCount
                    }
                    super.onConfirm()
                    if (adapter != null && adapter.itemCount > oldSize) {
                        adapter.notifyItemInserted(adapter.itemCount)
                    }
                }
            }.show()
        }
        return view
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: LiFilterInfoBinding

        init {
            binding = LiFilterInfoBinding.bind(itemView)
        }
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.li_filter_info, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val filter = AlopexFilterManager.filters[position]
            holder.binding.filter = filter
            holder.binding.root.setOnClickListener {
                FilterDialog(
                    holder.itemView.context,
                    filter
                ).show()
            }
            holder.binding.root.setOnLongClickListener {
                val str = filter.toJsonObject().toString()
                val context = holder.binding.root.context
                val clipboardManager = context.getSystemService(
                    ClipboardManager::class.java
                )
                clipboardManager.setPrimaryClip(ClipData.newPlainText("filter json", str))
                Alopex.showToast(context, "filter json copied", Toast.LENGTH_SHORT)
                true
            }
        }

        override fun getItemCount(): Int {
            return AlopexFilterManager.filters.size
        }
    }

    companion object {
        fun newInstance(): FilterFragment {
            return FilterFragment()
        }
    }
}