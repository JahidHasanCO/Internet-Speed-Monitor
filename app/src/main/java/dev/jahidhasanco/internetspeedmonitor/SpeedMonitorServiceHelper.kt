package dev.jahidhasanco.internetspeedmonitor

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import dev.jahidhasanco.internetspeedmonitor.utils.Settings


object SpeedMonitorServiceHelper {
    private fun getServiceIntent(context: Context): Intent {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val serviceIntent = Intent(context, SpeedMonitorService::class.java)

        // Add all preferences to intent
        sharedPref.all.forEach {
            if (it.value is Boolean) {
                serviceIntent.putExtra(it.key, it.value as Any as Boolean)
            } else if (it.value is String) {
                serviceIntent.putExtra(it.key, it.value as String?)
            }
        }
        return serviceIntent
    }

    fun startService(context: Context) {
        context.startService(getServiceIntent(context))
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(Settings.KEY_MONITOR_STARTED, true).apply()
    }

    fun stopService(context: Context) {
        context.stopService(Intent(context, SpeedMonitorService::class.java))
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(Settings.KEY_MONITOR_STARTED, false).apply()
    }
}