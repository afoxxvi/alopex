package com.afoxxvi.alopex.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.databinding.DialogAppListBinding
import com.afoxxvi.alopex.databinding.LiAppInfoBinding
import java.text.Collator
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory

open class AppListDialog @SuppressLint("QueryPermissionsNeeded") constructor(context: Context) : BaseDialog(context, "Installed App List") {
    private val appInfoList: MutableList<AppInfo>
    private val binding: DialogAppListBinding

    init {
        binding = DialogAppListBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        setBottomVisible(false)
        appInfoList = ArrayList()
        binding.recyclerAppList.visibility = View.GONE
        binding.recyclerAppList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerAppList.adapter = Adapter()
        ScheduledThreadPoolExecutor(1, object : ThreadFactory {
            val factory = Executors.defaultThreadFactory()
            override fun newThread(r: Runnable): Thread {
                return factory.newThread(r)
            }
        }).execute {
            val pm = context.packageManager
            for (info in pm.getInstalledApplications(0)) {
                if (info.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    continue
                }
                val label = info.loadLabel(pm).toString()
                if (label == info.packageName) {
                    continue
                }
                appInfoList.add(AppInfo(info.packageName, label, info.loadIcon(pm)))
            }
            appInfoList.sortWith { i1: AppInfo, i2: AppInfo -> Collator.getInstance(Locale.CHINA).compare(i1.appName, i2.appName) }
            Message.obtain(handler, WHAT_LOAD_COMPLETE).sendToTarget()
        }
    }

    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == WHAT_LOAD_COMPLETE) {
                binding.containerLoading.visibility = View.GONE
                val adapter = binding.recyclerAppList.adapter
                adapter?.notifyItemRangeInserted(0, appInfoList.size)
                binding.recyclerAppList.visibility = View.VISIBLE
            }
        }
    }

    open fun onSelect(appInfo: AppInfo) {}
    class AppInfo(@get:Bindable val packageName: String, @get:Bindable val appName: String, @get:Bindable val appIcon: Drawable) : BaseObservable()
    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var binding = LiAppInfoBinding.bind(itemView!!)
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.li_app_info, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.info = appInfoList[position]
            holder.itemView.setOnClickListener { onSelect(appInfoList[position]) }
        }

        override fun getItemCount(): Int {
            return appInfoList.size
        }
    }

    companion object {
        private const val WHAT_LOAD_COMPLETE = 1
    }
}