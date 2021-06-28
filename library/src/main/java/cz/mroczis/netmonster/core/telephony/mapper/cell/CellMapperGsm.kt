package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthWcdma
import android.telephony.SignalStrength
import android.telephony.gsm.GsmCellLocation
import cz.mroczis.netmonster.core.db.BandTableGsm
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandGsm
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.util.Reflection
import cz.mroczis.netmonster.core.util.getGsmRssi
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * [CellSignalStrengthGsm] -> [SignalGsm]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellSignalStrengthGsm.mapSignal(): SignalGsm {
    val rssi = dbm.inRangeOrNull(SignalGsm.RSSI_RANGE)
    val bitError = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitErrorRate.inRangeOrNull(SignalGsm.BIT_ERROR_RATE_RANGE)
    } else Reflection.intFieldOrNull(Reflection.GSM_BIT_ERROR_RATE, this)
        ?.inRangeOrNull(SignalGsm.BIT_ERROR_RATE_RANGE)


    val ta = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            timingAdvance.inRangeOrNull(SignalGsm.TIMING_ADVANCE_RANGE)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            val sonyTa = Reflection.intFieldOrNull(Reflection.GSM_TA, this)
            val androidTa = Reflection.intFieldOrNull(Reflection.GSM_TIMING_ADVANCE, this)
            (androidTa ?: sonyTa)?.inRangeOrNull(SignalGsm.TIMING_ADVANCE_RANGE)
        }
        else -> null
    }

    return SignalGsm(
        rssi = rssi,
        bitErrorRate = bitError,
        timingAdvance = ta
    )
}

/**
 * Special mapper dedicated to Samsung SM-G973F.
 * In GSM this device reports signal in CellSignalStrengthWcdma instances.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun CellSignalStrengthWcdma.mapWcdmaSignalToGsm(): SignalGsm {
    val rssi = dbm.inRangeOrNull(SignalGsm.RSSI_RANGE)
    val ber = Reflection.intFieldOrNull(Reflection.GSM_BIT_ERROR_RATE, this)
        ?.inRangeOrNull(SignalGsm.BIT_ERROR_RATE_RANGE)

    return SignalGsm(
        rssi = rssi,
        bitErrorRate = ber,
        timingAdvance = null
    )
}

/**
 * [CellIdentityGsm] -> [CellGsm]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellIdentityGsm.mapCell(
    subId: Int,
    connection: IConnection,
    signal: SignalGsm,
    timestamp: Long
): CellGsm? {
    val network = mapNetwork()
    val cid = cid.inRangeOrNull(CellGsm.CID_RANGE)
    val lac = lac.inRangeOrNull(CellGsm.LAC_RANGE)

    val bsic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Huawei YAL-L41 reports constantly 0 as BSIC but only for serving cells
        if (Build.MODEL == "YAL-L41" && connection is PrimaryConnection && bsic == 0) {
            Int.MAX_VALUE
        } else {
            bsic
        }.inRangeOrNull(CellGsm.BSIC_RANGE)
    } else null

    val arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        arfcn.inRangeOrNull(BandGsm.ARFCN_RANGE)
    } else null

    val band = if (arfcn != null) {
        BandTableGsm.map(arfcn, network?.mcc)
    } else null

    return if ((cid != null && lac != null) || ((arfcn != null || bsic != null) && connection !is PrimaryConnection)) {
        CellGsm(
            network = network,
            cid = cid,
            lac = lac,
            bsic = bsic,
            connectionStatus = connection,
            signal = if (cid == null && lac == null && signal.rssi == SignalGsm.RSSI_MIN.toInt()) {
                // When LTE is serving phones show neighbouring GSM cells but with invalid -113 dBm RSSI
                signal.copy(rssi = null)
            } else signal,
            band = band,
            subscriptionId = subId,
            timestamp = timestamp,
        )
    } else null
}

/**
 * [CellIdentityGsm] -> [Network]
 */
@Suppress("DEPRECATION")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellIdentityGsm.mapNetwork(): Network? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Network.map(mccString, mncString)
    } else {
        Network.map(mcc, mnc)
    }

@Suppress("DEPRECATION")
internal fun GsmCellLocation.mapGsm(
    subId: Int,
    signalStrength: SignalStrength?,
    network: Network?,
): ICell? {
    val cid = cid.inRangeOrNull(CellGsm.CID_RANGE)
    val lac = lac.inRangeOrNull(CellGsm.LAC_RANGE)

    val signal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val gsm = signalStrength?.getCellSignalStrengths(CellSignalStrengthGsm::class.java)
            ?.firstOrNull()
            ?.mapSignal()

        // Samsung SM-G973F in GSM mode uses CellSignalStrengthWcdma for some reason
        // So if GSM instance will not work, let's try look for WCDMA and grab only convertible fields
        val wcdma = if (gsm == null) {
            signalStrength?.getCellSignalStrengths(CellSignalStrengthWcdma::class.java)
                ?.firstOrNull()
                ?.mapWcdmaSignalToGsm()
        } else {
            null
        }
        
        gsm ?: wcdma ?: SignalGsm(null, null, null)
    } else {
        val rssi = signalStrength?.getGsmRssi()?.inRangeOrNull(SignalGsm.RSSI_RANGE)
        val ber = signalStrength?.gsmBitErrorRate?.inRangeOrNull(SignalGsm.BIT_ERROR_RATE_RANGE)

        SignalGsm(
            rssi = rssi,
            bitErrorRate = ber,
            timingAdvance = null
        )
    }

    return if (cid != null && lac != null) {
        return CellGsm(
            cid = cid,
            lac = lac,
            bsic = null,
            band = null,
            signal = signal,
            network = network,
            connectionStatus = PrimaryConnection(),
            subscriptionId = subId,
            timestamp = null,
        )
    } else null
}