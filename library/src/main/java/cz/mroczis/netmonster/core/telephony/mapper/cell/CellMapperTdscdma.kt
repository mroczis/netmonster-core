package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityTdscdma
import android.telephony.CellSignalStrengthTdscdma
import cz.mroczis.netmonster.core.db.BandTableTdscdma
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandTdscdma
import cz.mroczis.netmonster.core.model.cell.CellTdscdma
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalTdscdma
import cz.mroczis.netmonster.core.util.inRangeOrNull

private val REGEX_BIT_ERROR = "ber=([^ ]*)".toRegex()
private val REGEX_RSSI = "rssi=([^ ]*)".toRegex()

/**
 * [CellSignalStrengthTdscdma] -> [SignalTdscdma]
 */
@TargetApi(Build.VERSION_CODES.Q)
internal fun CellSignalStrengthTdscdma.mapSignal(): SignalTdscdma {
    val string = toString()

    val rscp = rscp.inRangeOrNull(SignalTdscdma.RSCP_RANGE)
    val rssi = REGEX_RSSI.find(string)?.groupValues?.getOrNull(1)?.toInt()
        ?.inRangeOrNull(SignalTdscdma.RSSI_RANGE)
    val bitError = REGEX_BIT_ERROR.find(string)?.groupValues?.getOrNull(1)?.toInt()
        ?.inRangeOrNull(SignalTdscdma.BIT_ERROR_RATE_RANGE)

    return SignalTdscdma(
        rssi = rssi,
        bitErrorRate = bitError,
        rscp = rscp
    )
}

/**
 * [CellIdentityTdscdma] -> [CellTdscdma]
 */
@TargetApi(Build.VERSION_CODES.Q)
internal fun CellIdentityTdscdma.mapCell(subId: Int, connection: IConnection, signal: SignalTdscdma, timestamp: Long): CellTdscdma? {
    val network =  Network.map(mccString, mncString)
    val ci = cid.inRangeOrNull(CellTdscdma.CID_RANGE)
    val lac = lac.inRangeOrNull(CellTdscdma.LAC_RANGE)
    val cpid = cpid.inRangeOrNull(CellTdscdma.CPID_RANGE)
    val uarfcn = uarfcn.inRangeOrNull(BandTdscdma.DOWNLINK_UARFCN_RANGE)

    val band = if (uarfcn != null) {
        BandTableTdscdma.map(uarfcn, network?.mcc)
    } else null

    return if (ci == null && cpid == null && uarfcn == null) {
        // Generally invalid data that cannot be used
        null
    } else {
        CellTdscdma(
            network = network,
            ci = ci,
            lac = lac,
            cpid = cpid,
            connectionStatus = connection,
            signal = signal,
            band = band,
            subscriptionId = subId,
            timestamp = timestamp
        )
    }
}