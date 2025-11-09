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
import com.afoxxvi.alopex.component.filter.Filters
import com.afoxxvi.alopex.component.notify.NotificationGroup
import com.afoxxvi.alopex.component.notify.Notifications
import com.afoxxvi.alopex.databinding.DialogNotifyListBinding
import com.afoxxvi.alopex.databinding.LiNotificationSummaryBinding

class NotifyListDialog(context: Context?, private val notificationGroup: NotificationGroup) : BaseDialog(context!!, "Notification list") {
    private val binding: DialogNotifyListBinding

    init {
        binding = DialogNotifyListBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        setBottomVisible(false)
        binding.recyclerNotifyList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerNotifyList.adapter = Adapter()
        binding.buttonLoad.setOnClickListener { v: View ->
            val oldCount = notificationGroup.notifyCount
            Notifications.requestMore(notificationGroup, 10)
            val newCount = notificationGroup.notifyCount
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
            holder.binding.notify = notificationGroup.getNotification(position)
            val result = Filters.passNotification(
                notificationGroup.packageName,
                notificationGroup.getNotification(position).title,
                notificationGroup.getNotification(position).text,
            )
            if (result.doConsume || result.doNotify || result.doCancel) {
                val list = mutableListOf<String>()
                if (result.doConsume) {
                    list.add("Consume")
                }
                if (result.doNotify) {
                    list.add("Notify")
                }
                if (result.doCancel) {
                    list.add("Cancel")
                }
                holder.binding.textAction.text = list.joinToString(" | ")
                val tv = TypedValue()
                context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, tv, true)
                holder.binding.textTitle.setTextColor(ColorStateList.valueOf(tv.data))
                holder.binding.textContent.setTextColor(ColorStateList.valueOf(tv.data))
                holder.binding.textTime.setTextColor(ColorStateList.valueOf(tv.data))
            }
        }

        override fun getItemCount(): Int {
            return notificationGroup.notifyCount
        }
    }
}