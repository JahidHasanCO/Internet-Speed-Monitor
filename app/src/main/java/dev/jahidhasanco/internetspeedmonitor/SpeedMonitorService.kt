package dev.jahidhasanco.internetspeedmonitor

import android.app.KeyguardManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import dev.jahidhasanco.internetspeedmonitor.utils.Settings
import dev.jahidhasanco.internetspeedmonitor.utils.Speed


class SpeedMonitorService: Service() {

    private var mKeyguardManager: KeyguardManager? = null

    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0

    private var mSpeed: Speed? = null

    private var mIndicatorNotification: SpeedMonitorNotification? = null

    private var mNotificationCreated = false

    private var mNotificationOnLockScreen = false

    private val mHandler: Handler = Handler()


    private val mHandlerRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            val usedRxBytes = currentRxBytes - mLastRxBytes
            val usedTxBytes = currentTxBytes - mLastTxBytes
            val currentTime = System.currentTimeMillis()
            val usedTime = currentTime - mLastTime
            mLastRxBytes = currentRxBytes
            mLastTxBytes = currentTxBytes
            mLastTime = currentTime
            mSpeed!!.calcSpeed(usedTime, usedRxBytes, usedTxBytes)
            mIndicatorNotification?.updateNotification(mSpeed!!)
            mHandler.postDelayed(this, 1000)
        }
    }

    private val mScreenBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                pauseNotifying()
                if (!mNotificationOnLockScreen) {
                    mIndicatorNotification?.hideNotification()
                }
                mSpeed?.let { mIndicatorNotification?.updateNotification(it) }
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                if (mNotificationOnLockScreen || !mKeyguardManager!!.isKeyguardLocked) {
                    mIndicatorNotification?.showNotification()
                    restartNotifying()
                }
            } else if (intent.action == Intent.ACTION_USER_PRESENT) {
                mIndicatorNotification?.showNotification()
                restartNotifying()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseNotifying()
        unregisterReceiver(mScreenBroadcastReceiver)
        removeNotification()
    }

    override fun onCreate() {
        super.onCreate()
        mLastRxBytes = TrafficStats.getTotalRxBytes()
        mLastTxBytes = TrafficStats.getTotalTxBytes()
        mLastTime = System.currentTimeMillis()
        mSpeed = Speed(this)
        mKeyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        mIndicatorNotification = SpeedMonitorNotification(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handleConfigChange(intent.extras)
        createNotification()
        restartNotifying()
        return START_REDELIVER_INTENT
    }

    private fun createNotification() {
        if (!mNotificationCreated) {
            mIndicatorNotification?.start(this)
            mNotificationCreated = true
        }
    }

    private fun removeNotification() {
        if (mNotificationCreated) {
            mIndicatorNotification?.stop(this)
            mNotificationCreated = false
        }
    }

    private fun pauseNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable)
    }

    private fun restartNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable)
        mHandler.post(mHandlerRunnable)
    }

    private fun handleConfigChange(config: Bundle?) {
        // Show/Hide on lock screen
        val screenBroadcastIntentFilter = IntentFilter()
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        mNotificationOnLockScreen =
            config!!.getBoolean(Settings.KEY_NOTIFICATION_ON_LOCK_SCREEN, false)
        if (!mNotificationOnLockScreen) {
            screenBroadcastIntentFilter.addAction(Intent.ACTION_USER_PRESENT)
            screenBroadcastIntentFilter.priority = 999
        }
        if (mNotificationCreated) {
            unregisterReceiver(mScreenBroadcastReceiver)
        }
        registerReceiver(mScreenBroadcastReceiver, screenBroadcastIntentFilter)

        // Speed unit, bps or Bps
        val isSpeedUnitBits = config.getString(Settings.KEY_INTERNET_SPEED_UNIT, "Bps") == "bps"
        mSpeed!!.setIsSpeedUnitBits(isSpeedUnitBits)

        // Pass it to notification
        mIndicatorNotification?.handleConfigChange(config)
    }
}