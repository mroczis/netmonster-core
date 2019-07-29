package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellSignalStrengthGsm
import cz.mroczis.netmonster.core.db.BandTableGsm
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandGsm
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.util.Reflection
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * [CellSignalStrengthGsm] -> [SignalGsm]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellSignalStrengthGsm.mapSignal(): SignalGsm {
    val rssi = dbm.inRangeOrNull(SignalGsm.RSSI_RANGE)
    val bitError = bitErrorRate.inRangeOrNull(SignalGsm.BIT_ERROR_RATE_RANGE)
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
 * [CellIdentityGsm] -> [CellGsm]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellIdentityGsm.mapCell(connection: IConnection, signal: SignalGsm): CellGsm? {
    val network = mapNetwork()
    val cid = cid.inRangeOrNull(CellGsm.CID_RANGE)
    val lac = lac.inRangeOrNull(CellGsm.LAC_RANGE)

    val bsic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        bsic.inRangeOrNull(CellGsm.BSIC_RANGE)
    } else null

    val arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        arfcn.inRangeOrNull(BandGsm.ARFCN_RANGE)
    } else null

    val band = if (arfcn != null && network != null) {
        BandTableGsm.map(arfcn, network.mcc)
    } else null

    return if (cid != null && lac != null) {
        CellGsm(
            network = network,
            cid = cid,
            lac = lac,
            bsic = bsic,
            connectionStatus = connection,
            signal = signal,
            band = band
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
