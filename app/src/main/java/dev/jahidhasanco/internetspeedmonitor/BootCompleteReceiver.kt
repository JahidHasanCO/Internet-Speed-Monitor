package dev.jahidhasanco.internetspeedmonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import dev.jahidhasanco.internetspeedmonitor.utils.Settings


class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null ||
            !intent.action.equals(Intent.ACTION_BOOT_COMPLETED)
        ) return

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        sharedPreferences.edit()
            .putBoolean(Settings.KEY_MONITOR_STARTED, false)
            .apply()

        if (!sharedPreferences.getBoolean(Settings.KEY_START_ON_BOOT, true)
            || !sharedPreferences.getBoolean(Settings.KEY_MONITOR_ENABLED, true)
        ) {
            return
        }

        SpeedMonitorServiceHelper.startService(context)
    }
}
