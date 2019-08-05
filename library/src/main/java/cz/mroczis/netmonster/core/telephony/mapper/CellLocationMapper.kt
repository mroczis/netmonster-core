package cz.mroczis.netmonster.core.telephony.mapper

import android.Manifest
import android.os.Build
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
import java.util.concurrent.Executors
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
        private val asyncExecutor by lazy { Executors.newFixedThreadPool(1) }
    }

    /**
     * Maps [CellLocation] to our format using multiple other methods from [TelephonyManager].
     * This method is blocking and processing might take ~500 ms on devices with Android O and older.
     * The processing is a bit faster on other devices with Android P+ and newer.
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    @WorkerThread
    override fun map(model: CellLocation?): List<ICell> {
        val signalStrength = getSignal()

        return mutableListOf<ICell>().apply {
            if (model is GsmCellLocation) {
                map(model, signalStrength)?.let { add(it) }
            } else if (model is CdmaCellLocation) {
                model.mapCdma(signalStrength)?.let { add(it) }
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
    private fun getSignal(): SignalStrength? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephony.signalStrength
        } else {
            val await = CountDownLatch(1)
            var signal: SignalStrength? = null
            val signalListener = SignalListener(subId) {
                // Invoked when data are updated for the 1st time
                signal = it
                await.countDown()
            }

            telephony.listen(signalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

            // This usually takes +/- 20 ms to complete
            try {
                await.await(500, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                // System was not able to deliver SignalStrength in this time slot
            }

            telephony.listen(signalListener, PhoneStateListener.LISTEN_NONE)
            signal
        }

    private class SignalListener(
        subId: Int?,
        private val callback: (signal: SignalStrength?) -> Unit
    ) : PhoneStateListenerPort(asyncExecutor, subId) {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            callback.invoke(signalStrength)
        }
    }

}