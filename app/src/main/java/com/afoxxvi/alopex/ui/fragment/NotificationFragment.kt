package com.afoxxvi.alopex.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.notify.Notifications
import com.afoxxvi.alopex.databinding.FragmentNotificationBinding
import com.afoxxvi.alopex.databinding.LiNotificationInfoBinding
import com.afoxxvi.alopex.ui.dialog.NotifyListDialog

class NotificationFragment : Fragment() {
    private var binding: FragmentNotificationBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Alopex.handlerNotification = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        binding = FragmentNotificationBinding.bind(view)
        binding!!.recyclerNotification.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding!!.recyclerNotification.adapter = Adapter()
        binding!!.btnSend.setOnClickListener {
            if (Alopex.handlerNotification != null) {
                val msg = Message.obtain(Alopex.handlerNotification, WHAT_NOTIFICATION_LISTENER_SERVICE)
                val bd = Bundle()
                val txt = binding!!.editPackageId.text.toString()
                bd.putString("package", "debug.package.$txt")
                bd.putString("title", "Title:$txt")
                bd.putString("content", "" + System.currentTimeMillis())
                bd.putLong("time", System.currentTimeMillis())
                msg.data = bd
                msg.sendToTarget()
            }
        }
        return view
    }

    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            if (msg.what == WHAT_NOTIFICATION_LISTENER_SERVICE) {
                val from = msg.arg1
                val adapter = binding?.recyclerNotification?.adapter
                if (adapter != null && from != -1) {
                    if (from == adapter.itemCount) {
                        adapter.notifyItemInserted(0)
                    } else {
                        adapter.notifyItemMoved(from, 0)
                    }
                    binding?.recyclerNotification?.scrollToPosition(0)
                }
            }
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: LiNotificationInfoBinding

        init {
            binding = LiNotificationInfoBinding.bind(itemView.rootView)
        }
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.li_notification_info, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val group = Notifications.getNotifyGroupList()[position]
            holder.binding.group = group
            holder.itemView.setOnClickListener { v: View -> NotifyListDialog(v.context, group).show() }
            //holder.itemView.setOnLongClickListener { throw Exception("Test") }
        }

        override fun getItemCount(): Int {
            return Notifications.getNotifyGroupList().size
        }
    }

    companion object {
        fun newInstance(): NotificationFragment {
            return NotificationFragment()
        }

        const val WHAT_NOTIFICATION_LISTENER_SERVICE = 1
    }
}