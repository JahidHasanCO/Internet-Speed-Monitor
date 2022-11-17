package dev.jahidhasanco.internetspeedmonitor.presentation.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import dev.jahidhasanco.internetspeedmonitor.R
import dev.jahidhasanco.internetspeedmonitor.SpeedMonitorServiceHelper
import dev.jahidhasanco.internetspeedmonitor.utils.Settings


class SettingFragment : PreferenceFragment() {

    private var mSharedPref: SharedPreferences? = null
    private var mContext: Context? = null

    private val mSettingsListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Settings.KEY_MONITOR_ENABLED) {
                if (mSharedPref!!.getBoolean(Settings.KEY_MONITOR_ENABLED, true)) {
                    startIndicatorService()
                } else {
                    stopIndicatorService()
                }
            } else if (key != Settings.KEY_START_ON_BOOT
                && key != Settings.KEY_MONITOR_ENABLED
            ) {
                startIndicatorService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = getActivity();

        addPreferencesFromResource(R.xml.preferences);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (mSharedPref!!.getBoolean(Settings.KEY_MONITOR_ENABLED, true)) {
            startIndicatorService();
        }
    }


    override fun onResume() {
        super.onResume()
        mSharedPref!!.registerOnSharedPreferenceChangeListener(mSettingsListener)
    }

    override fun onPause() {
        super.onPause()
        mSharedPref!!.unregisterOnSharedPreferenceChangeListener(mSettingsListener)
    }

    private fun startIndicatorService() {
            SpeedMonitorServiceHelper.startService(mContext!!)

    }

    private fun stopIndicatorService() {
        if (mContext != null) {
            SpeedMonitorServiceHelper.stopService(mContext!!)
        }
    }

}