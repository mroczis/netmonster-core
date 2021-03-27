package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.feature.config.CellLocationSource.CellLocationListener
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import cz.mroczis.netmonster.core.util.Threads
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
        getFresh(telephonyManager, subId) ?: telephonyManager.cellLocation

    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?) : CellLocation? {
        var listener: CellLocationListener? = null
        val asyncLock = CountDownLatch(1)
        var cellLocation: CellLocation? = null

        Threads.phoneStateListener.post {
            // We'll receive callbacks on thread that created instance of [listener] by default.
            // Async processing is required otherwise deadlock would arise cause we block
            // original thread
            listener = CellLocationListener(subId) {
                telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
                cellLocation = it
                asyncLock.countDown()
            }

            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION)
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

        return cellLocation
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