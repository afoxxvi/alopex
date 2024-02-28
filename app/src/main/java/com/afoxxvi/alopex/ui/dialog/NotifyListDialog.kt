package com.afoxxvi.alopex.ui.dialog

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.AlopexFilterManager
import com.afoxxvi.alopex.component.notify.NotifyGroup
import com.afoxxvi.alopex.component.notify.NotifyManager
import com.afoxxvi.alopex.databinding.DialogNotifyListBinding
import com.afoxxvi.alopex.databinding.LiNotificationSummaryBinding

class NotifyListDialog(context: Context?, private val notifyGroup: NotifyGroup) : BaseDialog(context!!, "Notification list") {
    private val binding: DialogNotifyListBinding

    init {
        binding = DialogNotifyListBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        setBottomVisible(false)
        binding.recyclerNotifyList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerNotifyList.adapter = Adapter()
        binding.buttonLoad.setOnClickListener { v: View ->
            val oldCount = notifyGroup.notifyCount
            NotifyManager.requestMore(notifyGroup, 10)
            val newCount = notifyGroup.notifyCount
            if (oldCount == newCount) {
                v.visibility = View.GONE
            } else {
                val adapter = binding.recyclerNotifyList.adapter
                adapter?.notifyItemRangeInserted(oldCount, newCount - oldCount)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = LiNotificationSummaryBinding.bind(itemView)
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_notification_summary, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.notify = notifyGroup.getNotify(position)
            if (AlopexFilterManager.isFiltered(notifyGroup.packageName, notifyGroup.getNotify(position), false).c) {
                val tv = TypedValue()
                context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, tv, true)
                holder.binding.textTitle.setTextColor(ColorStateList.valueOf(tv.data))
                holder.binding.textContent.setTextColor(ColorStateList.valueOf(tv.data))
                holder.binding.textTime.setTextColor(ColorStateList.valueOf(tv.data))
            }
        }

        override fun getItemCount(): Int {
            return notifyGroup.notifyCount
        }
    }
}