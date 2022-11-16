package dev.jahidhasanco.internetspeedmonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import dev.jahidhasanco.internetspeedmonitor.utils.Settings


class UpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent.action == null ||
            !intent.action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)
        ) return

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (sharedPreferences.getBoolean(Settings.KEY_MONITOR_STARTED, false)) {
            if (context != null) {
                SpeedMonitorServiceHelper.startService(context)
            }
        }
    }
}