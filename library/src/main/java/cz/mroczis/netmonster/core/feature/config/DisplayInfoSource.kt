package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.model.DisplayInfo
import cz.mroczis.netmonster.core.telephony.mapper.toDisplayInfo
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import cz.mroczis.netmonster.core.util.SingleEventPhoneStateListener

/**
 * Attempts to fetch fresh [TelephonyDisplayInfo] using [DisplayInfoSource].
 */
@TargetApi(Build.VERSION_CODES.R)
class DisplayInfoSource {

    /**
     * Registers a display info listener and awaits data. After 1000 milliseconds time outs if
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
        TelephonyCache.getOrUpdate(subId, TelephonyCache.Event.DISPLAY_INFO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.requestSingleUpdate { displayInfoListener(it) }
            } else {
                telephonyManager.requestPhoneStateUpdate { displayInfoListener(subId, it) }
            }
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [SignalStrength]
     */
    @TargetApi(Build.VERSION_CODES.R)
    private fun displayInfoListener(
        subId: Int?,
        onChanged: UpdateResult<SingleEventPhoneStateListener, TelephonyDisplayInfo>
    ) = object : SingleEventPhoneStateListener(LISTEN_DISPLAY_INFO_CHANGED, subId) {

        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            super.onDisplayInfoChanged(telephonyDisplayInfo)
            onChanged.invoke(this, telephonyDisplayInfo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun displayInfoListener(onChanged: UpdateResult<TelephonyCallback, TelephonyDisplayInfo>) =
        object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                onChanged(this, telephonyDisplayInfo)
            }
        }
}