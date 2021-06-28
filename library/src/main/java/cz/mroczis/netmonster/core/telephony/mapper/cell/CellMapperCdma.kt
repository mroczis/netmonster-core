package cz.mroczis.netmonster.core.telephony.mapper.cell

import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellSignalStrengthCdma
import android.telephony.SignalStrength
import android.telephony.cdma.CdmaCellLocation
import cz.mroczis.netmonster.core.model.cell.CellCdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalCdma
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * [CellSignalStrengthCdma] -> [SignalCdma]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellSignalStrengthCdma.mapSignal(): SignalCdma {
    val cdmaRssi = cdmaDbm.inRangeOrNull(SignalCdma.RSSI_RANGE)
    val cdmaEcio = cdmaEcio.inRangeOrNull(SignalCdma.ECIO_RANGE)?.toDouble()?.let { it / 10.0 }

    val evdoRssi = evdoDbm.inRangeOrNull(SignalCdma.RSSI_RANGE)
    val evdoEcio = evdoEcio.inRangeOrNull(SignalCdma.ECIO_RANGE)?.toDouble()?.let { it / 10.0 }
    val evdoSnr = evdoSnr.inRangeOrNull(SignalCdma.SNR_RANGE)

    return SignalCdma(
        cdmaRssi = cdmaRssi,
        cdmaEcio = cdmaEcio,
        evdoRssi = evdoRssi,
        evdoEcio = evdoEcio,
        evdoSnr = evdoSnr
    )
}

/**
 * [CellIdentityCdma] -> [CellCdma]
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal fun CellIdentityCdma.mapCell(subId: Int, connection: IConnection, signal: SignalCdma, timestamp: Long): CellCdma? {
    val bid = basestationId.inRangeOrNull(CellCdma.BID_RANGE)
    val nid = networkId.inRangeOrNull(CellCdma.NID_RANGE)
    val sid = systemId.inRangeOrNull(CellCdma.SID_RANGE)
    val lat = latitude.inRangeOrNull(CellCdma.LAT_RANGE)?.toDouble()?.let { it * 90.0 / 1296000 }
    val lon = longitude.inRangeOrNull(CellCdma.LON_RANGE)?.toDouble()?.let { it * 90.0 / 1296000 }

    return if (sid != null) {
        CellCdma(
            sid = sid,
            bid = bid,
            nid = nid,
            lat = lat,
            lon = lon,
            signal = signal,
            connectionStatus = connection,
            subscriptionId = subId,
            timestamp = timestamp
        )
    } else null
}

@Suppress("DEPRECATION")
internal fun CdmaCellLocation.mapCdma(subId: Int, signal: SignalStrength?): ICell? {
    val bid = baseStationId.inRangeOrNull(CellCdma.BID_RANGE)
    val nid = networkId.inRangeOrNull(CellCdma.NID_RANGE)
    val sid = systemId.inRangeOrNull(CellCdma.SID_RANGE)
    val lat = baseStationLatitude.inRangeOrNull(CellCdma.LAT_RANGE)?.toDouble()?.let { it * 90.0 / 1296000 }
    val lon = baseStationLongitude.inRangeOrNull(CellCdma.LON_RANGE)?.toDouble()?.let { it * 90.0 / 1296000 }

    val cdmaRssi = signal?.cdmaDbm?.inRangeOrNull(SignalCdma.RSSI_RANGE)
    val cdmaEcio = signal?.cdmaEcio?.inRangeOrNull(SignalCdma.ECIO_RANGE)?.toDouble()?.let { it / 10.0 }

    val evdoRssi = signal?.evdoDbm?.inRangeOrNull(SignalCdma.RSSI_RANGE)
    val evdoEcio = signal?.evdoEcio?.inRangeOrNull(SignalCdma.ECIO_RANGE)?.toDouble()?.let { it / 10.0 }
    val evdoSnr = signal?.evdoSnr?.inRangeOrNull(SignalCdma.SNR_RANGE)

    return if (sid != null) {
        CellCdma(
            sid = sid,
            bid = bid,
            nid = nid,
            lat = lat,
            lon = lon,
            signal = SignalCdma(
                cdmaRssi = cdmaRssi,
                cdmaEcio = cdmaEcio,
                evdoRssi = evdoRssi,
                evdoEcio = evdoEcio,
                evdoSnr = evdoSnr
            ),
            connectionStatus = PrimaryConnection(),
            subscriptionId = subId,
            timestamp = null,
        )
    } else null
}