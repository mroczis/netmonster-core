package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.feature.config.CellLocationSource.CellLocationListener
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort

/**
 * Attempts to fetch fresh [CellLocation] using [CellLocationListener]. If this
 * approach fails then looks to cache in [TelephonyManager].
 */
class CellLocationSource {

    /**
     * Registers [CellLocationListener] and awaits data. After 100 milliseconds time outs if
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
        TelephonyCache.getOrUpdate(subId, PhoneStateListener.LISTEN_CELL_LOCATION) {
            telephonyManager.requestSingleUpdate<CellLocation>(PhoneStateListener.LISTEN_CELL_LOCATION) { onData ->
                CellLocationListener(subId, onData)
            }
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [CellLocation]
     */
    private class CellLocationListener(
        subId: Int?,
        private val simStateListener: CellLocationListener.(state: CellLocation) -> Unit
    ) : PhoneStateListenerPort(subId) {

        override fun onCellLocationChanged(location: CellLocation?) {
            super.onCellLocationChanged(location)
            if (location != null) {
                simStateListener.invoke(this, location)
            }
        }
    }
}