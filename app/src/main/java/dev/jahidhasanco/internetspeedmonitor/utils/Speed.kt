package dev.jahidhasanco.internetspeedmonitor.utils

import android.content.Context
import dev.jahidhasanco.internetspeedmonitor.R
import java.util.*


class Speed(private val mContext: Context?) {
    private var totalSpeed: Long = 0L
    private var downSpeed: Long = 0L
    private var upSpeed: Long = 0L

    private var isSpeedUnitBits = false

    var total = NetSpeed()
    var down = NetSpeed()
    var up = NetSpeed()

    init {
        updateNetSpeeds()
    }

    private fun updateNetSpeeds() {
        total.setSpeed(totalSpeed)
        down.setSpeed(downSpeed)
        up.setSpeed(upSpeed)
    }

    fun calcSpeed(timeTaken: Long, downBytes: Long, upBytes: Long) {
        var totalSpeed: Long = 0
        var downSpeed: Long = 0
        var upSpeed: Long = 0
        val totalBytes = downBytes + upBytes
        if (timeTaken > 0) {
            totalSpeed = totalBytes * 1000 / timeTaken
            downSpeed = downBytes * 1000 / timeTaken
            upSpeed = upBytes * 1000 / timeTaken
        }
        this.totalSpeed = totalSpeed
        this.downSpeed = downSpeed
        this.upSpeed = upSpeed
        updateNetSpeeds()
    }


    fun getNetSpeed(name: String): NetSpeed {
        return when (name) {
            "up" -> up
            "down" -> down
            else -> total
        }
    }

    fun setIsSpeedUnitBits(isSpeedUnitBits: Boolean) {
        this.isSpeedUnitBits = isSpeedUnitBits
    }

    inner class NetSpeed {
        var speedValue = ""
        var speedUnit = ""

        fun setSpeed(s: Long) {
            var speed = s
            if (mContext == null) return

            if (isSpeedUnitBits) {
                speed *= 8
            }

            if (speed < 1000000) {
                speedUnit =
                    mContext.getString(if (isSpeedUnitBits) R.string.kbps else R.string.kBps)
                speedValue = (speed / 1000).toString()
            } else if (speed >= 1000000) {
                speedUnit =
                    mContext.getString(if (isSpeedUnitBits) R.string.Mbps else R.string.MBps)
                speedValue = if (speed < 10000000) {
                    java.lang.String.format(Locale.ENGLISH, "%.1f", speed / 1000000.0)
                } else if (speed < 100000000) {
                    (speed / 1000000).toString()
                } else {
                    mContext.getString(R.string.plus99)
                }
            } else {
                speedValue = mContext.getString(R.string.dash)
                speedUnit = mContext.getString(R.string.dash)
            }
        }
    }

}
