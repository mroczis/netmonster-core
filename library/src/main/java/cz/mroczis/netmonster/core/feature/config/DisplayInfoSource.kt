package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.DisplayInfo
import cz.mroczis.netmonster.core.telephony.mapper.toDisplayInfo
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import cz.mroczis.netmonster.core.util.Threads
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Attempts to fetch fresh [TelephonyDisplayInfo] using [DisplayInfoSource].
 */
@TargetApi(Build.VERSION_CODES.R)
class DisplayInfoSource {

    /**
     * Registers [DisplayInfoListener] and awaits data. After 100 milliseconds time outs if
     * nothing is delivered.
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @Suppress("DEPRECATION")
    fun get(telephonyManager: TelephonyManager, subId: Int?): DisplayInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getFresh(telephonyManager, subId)?.toDisplayInfo()
        } else {
            null
        } ?: DisplayInfo()

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): TelephonyDisplayInfo? {
        var listener: DisplayInfoListener? = null
        val asyncLock = CountDownLatch(1)
        var displayInfo: TelephonyDisplayInfo? = null

        Threads.phoneStateListener.post {
            // We'll receive callbacks on thread that created instance of [listener] by default.
            // Async processing is required otherwise deadlock would arise cause we block
            // original thread
            listener = DisplayInfoListener(subId) {
                telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
                displayInfo = it
                asyncLock.countDown()
            }

            telephonyManager.listen(listener, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
        }

        // And we also must block original thread
        // It'll get unblocked once we receive required data
        // This usually takes +/- 20 ms to complete
        try {
            asyncLock.await(100, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            // System was not able to deliver PhysicalChannelConfig in this time slot
        }

        listener?.let { telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE) }

        return displayInfo
    }


    /**
     * Kotlin friendly PhoneStateListener that grabs [TelephonyDisplayInfo]
     */
    @TargetApi(Build.VERSION_CODES.R)
    private class DisplayInfoListener(
        subId: Int?,
        private val displayInfoListener: DisplayInfoListener.(info: TelephonyDisplayInfo) -> Unit
    ) : PhoneStateListenerPort(subId) {

        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            super.onDisplayInfoChanged(telephonyDisplayInfo)
            displayInfoListener.invoke(this, telephonyDisplayInfo)
        }
    }
}