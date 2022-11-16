package dev.jahidhasanco.internetspeedmonitor


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import dev.jahidhasanco.internetspeedmonitor.presentation.activity.SettingActivity
import dev.jahidhasanco.internetspeedmonitor.utils.Settings
import dev.jahidhasanco.internetspeedmonitor.utils.Speed
import java.util.*


@RequiresApi(Build.VERSION_CODES.M)
class SpeedMonitorNotification(private val mContext: Context) {
    private val NOTIFICATION_ID = 1

    private var mIconSpeedPaint: Paint? = null
    private var mIconUnitPaint: Paint? = null
    private var mIconBitmap: Bitmap? = null
    private var mIconCanvas: Canvas? = null

    private var mNotificationContentView: RemoteViews? = null

    private var mNotificationManager: NotificationManager? = null
    private var mNotificationBuilder: Notification.Builder? = null

    private var mNotificationPriority = 0
    private var mSpeedToShow = "total"

    init {
        setup()
    }

    fun start(serviceContext: Service) {
        serviceContext.startForeground(
            NOTIFICATION_ID,
            mNotificationBuilder?.build() ?: Notification()
        )
    }

    fun stop(serviceContext: Service) {
        serviceContext.stopForeground(true)
    }

    fun hideNotification() {
        mNotificationBuilder?.setPriority(Notification.PRIORITY_MIN)
    }

    fun showNotification() {
        mNotificationBuilder?.setPriority(mNotificationPriority)
    }

    fun updateNotification(speed: Speed) {
        val speedToShow: Speed.NetSpeed = speed.getNetSpeed(mSpeedToShow)
        mNotificationBuilder?.setSmallIcon(
            getIndicatorIcon(speedToShow.speedValue, speedToShow.speedUnit)
        )
        val contentView = mNotificationContentView!!.clone()
        contentView.setTextViewText(
            R.id.notificationSpeedValue,
            speedToShow.speedValue
        )
        contentView.setTextViewText(
            R.id.notificationSpeedUnit,
            speedToShow.speedUnit
        )
        contentView.setTextViewText(
            R.id.notificationText,
            java.lang.String.format(
                Locale.ENGLISH, mContext.getString(R.string.notif_up_down_speed),
                speed.down.speedValue, speed.down.speedUnit,
                speed.up.speedValue, speed.up.speedUnit
            )
        )
        mNotificationBuilder?.setContent(contentView)
        mNotificationManager!!.notify(
            NOTIFICATION_ID,
            mNotificationBuilder?.build() ?: Notification()
        )
    }

    fun handleConfigChange(extras: Bundle) {
        // Which speed to show in indicator icon
        mSpeedToShow = extras.getString(Settings.KEY_INTERNET_SPEED_TO_SHOW, "total")

        // Show/Hide settings button
        if (extras.getBoolean(Settings.KEY_SHOW_SETTINGS_BUTTON, false)) {
            mNotificationContentView!!.setViewVisibility(R.id.notificationSettings, View.VISIBLE)
        } else {
            mNotificationContentView!!.setViewVisibility(R.id.notificationSettings, View.GONE)
        }
        when (extras.getString(Settings.KEY_NOTIFICATION_PRIORITY, "max")) {
            "low" -> mNotificationPriority = Notification.PRIORITY_LOW
            "default" -> mNotificationPriority = Notification.PRIORITY_DEFAULT
            "high" -> mNotificationPriority = Notification.PRIORITY_HIGH
            "max" -> mNotificationPriority = Notification.PRIORITY_MAX
        }
        mNotificationBuilder?.setPriority(mNotificationPriority)

        // Show/Hide on lock screen
        if (extras.getBoolean(Settings.KEY_NOTIFICATION_ON_LOCK_SCREEN, false)) {
            mNotificationBuilder?.setVisibility(Notification.VISIBILITY_PUBLIC)
        } else {
            mNotificationBuilder?.setVisibility(Notification.VISIBILITY_SECRET)
        }
    }

    @SuppressLint("RemoteViewLayout")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setup() {
        setupIndicatorIconGenerator()
        mNotificationContentView =
            RemoteViews(mContext.packageName, R.layout.view_notification_layout)
        val openSettingsIntent = PendingIntent.getActivity(
            mContext, 0, Intent(
                mContext,
                SettingActivity::class.java
            ), 0
        )
        mNotificationContentView!!.setOnClickPendingIntent(
            R.id.notificationSettings,
            openSettingsIntent
        )
        mNotificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationBuilder = Notification.Builder(mContext)
            .setSmallIcon(getIndicatorIcon("", ""))
            .setPriority(Notification.PRIORITY_MAX)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .setContent(mNotificationContentView)
            .setOngoing(true)
            .setLocalOnly(true)
    }

    private fun setupIndicatorIconGenerator() {
        mIconSpeedPaint = Paint()
        mIconSpeedPaint!!.color = Color.WHITE
        mIconSpeedPaint!!.isAntiAlias = true
        mIconSpeedPaint!!.textSize = 65F
        mIconSpeedPaint!!.textAlign = Paint.Align.CENTER
        mIconSpeedPaint!!.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        mIconUnitPaint = Paint()
        mIconUnitPaint!!.color = Color.WHITE
        mIconUnitPaint!!.isAntiAlias = true
        mIconUnitPaint!!.textSize = 40F
        mIconUnitPaint!!.textAlign = Paint.Align.CENTER
        mIconUnitPaint!!.typeface = Typeface.DEFAULT_BOLD
        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        mIconCanvas = Canvas(mIconBitmap!!)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getIndicatorIcon(speedValue: String, speedUnit: String): Icon {
        mIconCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mIconSpeedPaint?.let { mIconCanvas?.drawText(speedValue, 48F, 52F, it) }
        mIconUnitPaint?.let { mIconCanvas?.drawText(speedUnit, 48F, 95F, it) }
        return Icon.createWithBitmap(mIconBitmap)
    }
}