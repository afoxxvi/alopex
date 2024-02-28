package com.afoxxvi.alopex

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.afoxxvi.alopex.component.filter.AlopexFilterManager
import com.afoxxvi.alopex.component.notify.NotifyManager
import com.afoxxvi.alopex.databinding.ActivityMainBinding
import com.afoxxvi.alopex.listener.RestartReceiver
import com.afoxxvi.alopex.service.AlopexNotificationListenerService
import com.afoxxvi.alopex.service.LifeService
import com.afoxxvi.alopex.ui.fragment.FilterFragment
import com.afoxxvi.alopex.ui.fragment.InformationFragment
import com.afoxxvi.alopex.ui.fragment.NotificationFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), Thread.UncaughtExceptionHandler {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        Alopex.active = true
        Alopex.init(this)
        if (!isNotificationListenerEnabled) {
            openNotificationListenerSettings()
        } else if (!AlopexNotificationListenerService.active) {
            startService(Intent(this, AlopexNotificationListenerService::class.java))
            startService(Intent(this, LifeService::class.java))
        }
        AlopexFilterManager.init(this)
        NotifyManager.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        val channel = NotificationChannel(Alopex.CHANNEL_ID, "Alopex Channel 1", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, 60000,
            PendingIntent.getBroadcast(this, 1, Intent(this, RestartReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
        )
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(false)
            }
        })
        //Thread.setDefaultUncaughtExceptionHandler(this)
    }

    private fun setupUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val tabs = arrayOf("Notification", "Filter", "Information")
        binding.pager.offscreenPageLimit = 3
        binding.pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> return NotificationFragment.newInstance()
                    1 -> return FilterFragment.newInstance()
                    2 -> return InformationFragment.newInstance()
                    else -> Fragment()
                }
            }

            override fun getItemCount(): Int = tabs.size
        }
        val mediator = TabLayoutMediator(binding.tabs, binding.pager) { tab: TabLayout.Tab, position: Int -> tab.text = tabs[position] }
        mediator.attach()
    }

    override fun onDestroy() {
        AlopexFilterManager.save(this)
        Alopex.active = false
        super.onDestroy()
    }

    private val isNotificationListenerEnabled: Boolean
        get() = NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.packageName)

    private fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Alopex.showToastShort(this, "Exception Detected.")
        val dir = File(this.dataDir, "error_logs")
        if (!dir.exists()) dir.mkdirs()
        val os = FileOutputStream(
            File(dir, "${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}.log")
        )
        val ps = PrintStream(os, true)
        e.printStackTrace(ps)
        ps.close()
        os.close()
        try {
            Thread.sleep(2000)
        } catch (_: Exception) {

        }
        exitProcess(1)
    }
}