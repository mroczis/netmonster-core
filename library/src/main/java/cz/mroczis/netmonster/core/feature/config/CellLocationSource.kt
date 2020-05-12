package cz.mroczis.netmonster.core.feature.config

import android.Manifest
import android.os.Handler
import android.os.HandlerThread
import android.telephony.*
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Attempts to fetch fresh [CellLocation] using [CellLocationListener]. If this
 * approach fails then looks to cache in [TelephonyManager].
 */
class CellLocationSource {

    companion object {

        /**
         * Async executor so can await data from [CellLocationListener]
         */
        private val asyncExecutor by lazy {
            val thread = HandlerThread("CellLocationSource").apply {
                start()
            }
            Handler(thread.looper)
        }
    }

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

        asyncExecutor.post {
            // We'll receive callbacks on thread that created instance of [listener] by default.
            // Async processing is required otherwise deadlock would arise cause we block
            // original thread
            listener = CellLocationListener(subId) {
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