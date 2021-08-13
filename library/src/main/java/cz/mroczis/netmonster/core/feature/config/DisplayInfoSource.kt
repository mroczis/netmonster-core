package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.model.DisplayInfo
import cz.mroczis.netmonster.core.telephony.mapper.toDisplayInfo
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort

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
    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): TelephonyDisplayInfo? =
        TelephonyCache.getOrUpdate(subId, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED) {
            telephonyManager.requestSingleUpdate<TelephonyDisplayInfo>(PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED) { onData ->
                DisplayInfoListener(subId, onData)
            }
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