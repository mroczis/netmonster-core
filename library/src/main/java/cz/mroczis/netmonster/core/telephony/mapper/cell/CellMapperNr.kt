package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellSignalStrengthNr
import cz.mroczis.netmonster.core.db.BandTableNr
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.band.BandNr
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalNr
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * [CellIdentityNr] -> [CellNr]
 */
@TargetApi(Build.VERSION_CODES.Q)
internal fun CellIdentityNr.mapCell(
    subId: Int,
    connection: IConnection,
    signal: SignalNr?,
    timestamp: Long? = null,
    plmn: Network? = null,
): CellNr {
    val network = plmn ?: Network.map(mccString, mncString)
    // 268435455 is LTE max CID and unfortunately used as N/A value on many MTK devices...
    val nci = nci.inRangeOrNull(CellNr.CID_RANGE)?.takeIf { it != Int.MAX_VALUE.toLong() && it != 268435455L }
    val tac = tac.inRangeOrNull(CellNr.TAC_RANGE)
    val pci = pci.inRangeOrNull(CellNr.PCI_RANGE)
    val arfcn = nrarfcn.inRangeOrNull(BandNr.DOWNLINK_EARFCN_RANGE)
    val band = arfcn?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BandTableNr.map(it, bands)
        } else {
            BandTableNr.map(it)
        }
    }

    return CellNr(
        network = network,
        nci = nci,
        tac = tac,
        pci = pci,
        connectionStatus = if (nci == null && connection is PrimaryConnection) {
            // Pixel 7 (Pro) whilst actively connected to NSA NR returns only one NR cell saying it's a primary one
            // Cell must be NR SA in order to serve independently. Hence without proper NCI primary connection
            // is not permitted
            SecondaryConnection(isGuess = false)
        } else {
            connection
        },
        signal = signal ?: SignalNr (),
        band = band,
        subscriptionId = subId,
        timestamp = timestamp
    )
}

@TargetApi(Build.VERSION_CODES.Q)
internal fun CellSignalStrengthNr.mapSignal(): SignalNr {
    val ssRsrp = ssRsrp.inRangeOrNull(SignalNr.RSRP_RANGE) ?: (ssRsrp * -1).inRangeOrNull(SignalNr.RSRP_RANGE)
    val ssRsrq = ssRsrq.inRangeOrNull(SignalNr.RSRQ_RANGE) ?: (ssRsrq * -1).inRangeOrNull(SignalNr.RSRQ_RANGE)
    val ssSinr = ssSinr.inRangeOrNull(SignalNr.SINR_RANGE)

    val csiRsrp = csiRsrp.inRangeOrNull(SignalNr.RSRP_RANGE) ?: (csiRsrp * -1).inRangeOrNull(SignalNr.RSRP_RANGE)
    val csiRsrq = csiRsrq.inRangeOrNull(SignalNr.RSRQ_RANGE) ?: (csiRsrq * -1).inRangeOrNull(SignalNr.RSRQ_RANGE)
    val csiSinr = csiSinr.inRangeOrNull(SignalNr.SINR_RANGE)

    return SignalNr(
        ssRsrp = ssRsrp,
        ssRsrq = ssRsrq,
        ssSinr = ssSinr,
        csiRsrp = csiRsrp,
        csiRsrq = csiRsrq,
        csiSinr = csiSinr
    )
}