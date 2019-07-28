package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityWcdma
import android.telephony.CellSignalStrengthWcdma
import cz.mroczis.netmonster.core.db.WcdmaBandTable
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandWcdma
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import cz.mroczis.netmonster.core.util.Reflection
import cz.mroczis.netmonster.core.util.inRangeOrNull

private val REGEX_BIT_ERROR = "ber=([^ ]*)".toRegex()
private val REGEX_RSCP = "rscp=([^ ]*)".toRegex()
private val REGEX_RSSI = "ss=([^ ]*)".toRegex()
private val REGEX_ECNO = "ecno=([^ ]*)".toRegex()

/**
 * [CellSignalStrengthWcdma] -> [SignalWcdma]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal fun CellSignalStrengthWcdma.mapSignal(): SignalWcdma {
    val string = toString()

    val rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android Q changes how 'CellSignalStrengthWcdma.getDbm()' works and
        // returns RSCP if available. In other cases RSSI is returned.
        // The only working way to get RSSI is again from string cause methods have @hide annotation
        REGEX_RSSI.find(string)?.groupValues?.getOrNull(1)?.toInt()
            ?.inRangeOrNull(SignalWcdma.RSSI_RANGE)
    } else {
        // Some older phones reported inadequate values when it came to ASU and DBM sources
        // We must decide what happens if values to dot fit
        val rssiFromAsu = (-113 + 2 * asuLevel).inRangeOrNull(SignalWcdma.RSSI_RANGE) // ASU -> DBM
        val rssiFromDbm = dbm.inRangeOrNull(SignalWcdma.RSSI_RANGE)
        // In real world those two values must be equal
        if (rssiFromAsu != rssiFromAsu) {
            rssiFromDbm ?: rssiFromAsu
        } else rssiFromDbm
    }

    val ecio = Reflection.intFieldOrNull(Reflection.UMTS_ECIO, this)?.inRangeOrNull(SignalWcdma.ECIO_RANGE)
    val rscp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        REGEX_RSCP.find(string)?.groupValues?.getOrNull(1)?.toInt()
            ?.inRangeOrNull(SignalWcdma.RSCP_RANGE)
    } else Reflection.intFieldOrNull(Reflection.UMTS_RSCP, this)

    val bitError = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        REGEX_BIT_ERROR.find(string)?.groupValues?.getOrNull(1)?.toInt()
            ?.inRangeOrNull(SignalWcdma.BIT_ERROR_RATE_RANGE)
    } else null

    val ecno = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        REGEX_ECNO.find(string)?.groupValues?.getOrNull(1)?.toInt()
            ?.inRangeOrNull(SignalWcdma.ECNO_RANGE)
    } else null

    return SignalWcdma(
        rssi = rssi,
        bitErrorRate = bitError,
        ecno = ecno,
        rscp = rscp,
        ecio = ecio
    )
}

/**
 * [CellIdentityWcdma] -> [CellWcdma]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal fun CellIdentityWcdma.mapCell(connection: IConnection, signal: SignalWcdma): CellWcdma? {
    val network = mapNetwork()
    val ci = cid.inRangeOrNull(CellWcdma.CID_RANGE)
    val lac = lac.inRangeOrNull(CellWcdma.LAC_RANGE)
    val psc = psc.inRangeOrNull(CellWcdma.PSC_RANGE)

    val uarfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        uarfcn.inRangeOrNull(BandWcdma.DOWNLINK_UARFCN_RANGE)
    } else null

    val band = if (uarfcn != null) {
        WcdmaBandTable.map(uarfcn)
    } else null

    return if (lac == null && ci != null && ci < 100) {
        // Samsung phones (SM-G960F) tend to report LAC = 0 and sequence of CIs starting with 1 (step 1) for
        // neighbouring cells. This check assumes there's less than 100 neighbouring cells
        null
    } else {
        CellWcdma(
            network = network,
            ci = ci,
            lac = lac,
            psc = psc,
            connectionStatus = connection,
            signal = signal,
            band = band
        )
    }
}

/**
 * [CellIdentityWcdma] -> [Network]
 */
@Suppress("DEPRECATION")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal fun CellIdentityWcdma.mapNetwork(): Network? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Network.map(mccString, mncString)
    } else {
        Network.map(mcc, mnc)
    }