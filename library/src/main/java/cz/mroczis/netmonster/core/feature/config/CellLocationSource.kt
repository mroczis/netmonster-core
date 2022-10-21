package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.util.SingleEventPhoneStateListener

/**
 * Attempts to fetch fresh [CellLocation] using cell location listener. If this
 * approach fails then looks to cache in [TelephonyManager].
 */
class CellLocationSource {

    /**
     * Registers a cell location listener and awaits data. After 1000 milliseconds time outs if
     * nothing is delivered.
     *
     * On Android O and newer directly grabs [ServiceState] from [TelephonyManager].
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    @Suppress("DEPRECATION")
    fun get(telephonyManager: TelephonyManager, subId: Int?): CellLocation? =
        getFresh(telephonyManager, subId) ?: try {
            telephonyManager.cellLocation
        } catch (e : NullPointerException) {
            // Xiaomi Mi 10, SDK 30 throws NPE here when data are not available
            null
        }

    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): CellLocation? =
        TelephonyCache.getOrUpdate(subId, TelephonyCache.Event.CELL_LOCATION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.requestSingleUpdate { cellLocationListener(it) }
            } else {
                telephonyManager.requestPhoneStateUpdate { cellLocationListener(subId, it) }
            }
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [SignalStrength]
     */
    @TargetApi(Build.VERSION_CODES.R)
    private fun cellLocationListener(
        subId: Int?,
        onChanged: UpdateResult<SingleEventPhoneStateListener, CellLocation>
    ) = object : SingleEventPhoneStateListener(LISTEN_CELL_LOCATION, subId) {

        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        override fun onCellLocationChanged(location: CellLocation?) {
            super.onCellLocationChanged(location)
            if (location != null) {
                onChanged(this, location)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun cellLocationListener(onChanged: UpdateResult<TelephonyCallback, CellLocation>) =
        object : TelephonyCallback(), TelephonyCallback.CellLocationListener {
            override fun onCellLocationChanged(location: CellLocation) {
                onChanged(this, location)
            }
        }
}