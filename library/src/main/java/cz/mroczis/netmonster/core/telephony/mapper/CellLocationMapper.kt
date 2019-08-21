package cz.mroczis.netmonster.core.telephony.mapper

import android.Manifest
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.signal.SignalLte
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCdma
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapGsm
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapLte
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapWcdma
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import cz.mroczis.netmonster.core.util.Reflection
import cz.mroczis.netmonster.core.util.inRangeOrNull
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Transforms [TelephonyManager.getCellLocation] into our representation.
 *
 * In older days signal, identity and PLMN info were separated into pieces and most of terminals
 * did not update them synchronously which led to false-positive results.
 * This class attempts to lower the overall error ratio.
 *
 * Generally I recommend to avoid using this data source when terminal has [android.os.Build.VERSION_CODES.N] or
 * newer -> it's better to rely on [CellInfoMapper].
 */
class CellLocationMapper(
    private val telephony: TelephonyManager,
    private val subId: Int? = null
) : ICellMapper<CellLocation?> {

    companion object {

        /**
         * Async executor so can await data from [PhoneStateListener] when device has older
         * Android device.
         */
        private val asyncExecutor by lazy {
            val thread = HandlerThread("CellLocationMapper").apply {
                start()
            }
            Handler(thread.looper)
        }
    }

    /**
     * Maps [CellLocation] to our format using multiple other methods from [TelephonyManager].
     * This method is blocking and processing might take ~500 ms on devices with Android O and older.
     * The processing is a bit faster on other devices with Android P+ and newer.
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun map(model: CellLocation?): List<ICell> {
        val scanResult = getUpdatedLocationAndSignal()

        return mutableListOf<ICell>().apply {
            if (scanResult?.location is GsmCellLocation) {
                map(scanResult.location, scanResult.signal)?.let { add(it) }
            } else if (scanResult?.location is CdmaCellLocation) {
                scanResult.location.mapCdma(scanResult.signal)?.let { add(it) }
            }
        }
    }

    private fun map(model: GsmCellLocation, signalStrength: SignalStrength?): ICell? {
        val network = NetworkTypeTable.get(telephony.networkType)
        val cid = model.cid
        val plmn = Network.map(telephony.networkOperator)

        val rsrp = Reflection.intFieldOrNull(Reflection.SS_LTE_RSRP, signalStrength)
            ?.toDouble()?.inRangeOrNull(SignalLte.RSRP_RANGE)
        val wcdma = Reflection.intFieldOrNull(Reflection.UMTS_RSCP, signalStrength)?.toLong()

        return if (rsrp != null && network is NetworkType.Lte && !CellGsm.CID_RANGE.contains(cid)) {
            model.mapLte(signalStrength, plmn)
        } else if (SignalWcdma.RSCP_RANGE.contains(wcdma) && network is NetworkType.Wcdma) {
            model.mapWcdma(signalStrength, plmn)
        } else if (CellGsm.CID_RANGE.contains(cid) && (!CellWcdma.PSC_RANGE.contains(model.psc) || network is NetworkType.Gsm)) {
            model.mapGsm(signalStrength, plmn)
        } else if (network is NetworkType.Wcdma) {
            model.mapWcdma(signalStrength, plmn)
        } else if (network is NetworkType.Lte) {
            model.mapLte(signalStrength, plmn)
        } else {
            null
        }

    }

    /**
     * Attempts to fetch [SignalStrength] from system. Which might take a while depending on current
     * OS version.
     */
    @WorkerThread
    @Suppress("DEPRECATION")
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun getUpdatedLocationAndSignal(): ScanResult? {
        var signal: SignalStrength? = null
        var location: CellLocation? = null
        val asyncLock = CountDownLatch(2) // CellLocation + SignalStrengths

        // Lets start listening for fresh data first
        asyncExecutor.post {
            // We must construct signal listener on custom thread
            // cause it takes Looper from it -> results will be delivered there
            val signalListener = SignalListener(
                subId = subId,
                signalCallback = { newSignal, firstShot ->
                    signal = newSignal

                    if (firstShot) {
                        asyncLock.countDown()
                    }

                    if (asyncLock.count == 0L) {
                        telephony.listen(this, PhoneStateListener.LISTEN_NONE)
                    }

                },
                locationCallback = { newLocation, firstShot ->
                    location = newLocation

                    if (firstShot) {
                        asyncLock.countDown()
                    }

                    if (asyncLock.count == 0L) {
                        telephony.listen(this, PhoneStateListener.LISTEN_NONE)
                    }
                }

            )

            telephony.listen(signalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_CELL_LOCATION)
        }

        // And we also must block original thread
        // It'll get unblocked once we receive required data
        // This usually takes +/- 20 ms to complete
        try {
            asyncLock.await(500, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            // System was not able to deliver SignalStrength in this time slot
        }

        // We want the freshest data possible, however on P+ we can grab also cached
        val finalSignal = if (signal == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephony.signalStrength
        } else {
            signal
        }

        val finalLocation = if (location == null) {
            telephony.cellLocation
        } else {
            location
        }

        return ScanResult(finalLocation, finalSignal)
    }

    /**
     * Wrapper for two instances we need to construct [ICell]
     */
    private data class ScanResult(
        val location: CellLocation?,
        val signal: SignalStrength?
    )

    /**
     * Kotlin friendly PhoneStateListener
     */
    private class SignalListener(
        subId: Int?,
        private val signalCallback: SignalListener.(signal: SignalStrength?, first: Boolean) -> Unit,
        private val locationCallback: SignalListener.(location: CellLocation?, first: Boolean) -> Unit
    ) : PhoneStateListenerPort(subId) {

        private var signalReceived = false
        private var locationReceived = false

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            signalCallback.invoke(this, signalStrength, !signalReceived)
            signalReceived = true
        }

        override fun onCellLocationChanged(location: CellLocation?) {
            super.onCellLocationChanged(location)
            locationCallback.invoke(this, location, !locationReceived)
            locationReceived = true
        }
    }


}