package cz.mroczis.netmonster.core.telephony.mapper

import android.os.Build
import android.telephony.NeighboringCellInfo
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.TillSdk
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm
import cz.mroczis.netmonster.core.model.signal.SignalWcdma
import cz.mroczis.netmonster.core.util.getGsmRssi
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * Neighbouring cell info which was deprecated in [Build.VERSION_CODES.M]
 * and direct call to retrieve instances of [NeighboringCellInfo] removed from SDK in [Build.VERSION_CODES.Q].
 *
 * Supports only [ICell] subset:
 *  - [cz.mroczis.netmonster.core.model.cell.CellGsm]
 *  - [cz.mroczis.netmonster.core.model.cell.CellWcdma]
 */
@TillSdk(
    sdkInt = Build.VERSION_CODES.Q,
    fallbackBehaviour = "This class is usable till Q cause required method was removed from SDK"
)
@Suppress("DEPRECATION")
class NeighbouringCellInfoMapper(
    private val telephony: TelephonyManager,
    private val subId: Int
) : ICellMapper<List<NeighboringCellInfo>?> {

    override fun map(model: List<NeighboringCellInfo>?): List<ICell> {
        val plmn = Network.map(telephony.networkOperator)
        return model?.mapNotNull {
            when(NetworkTypeTable.get(telephony.networkType)) {
                is NetworkType.Gsm -> processGsm(it, plmn)
                is NetworkType.Wcdma -> processWcdma(it, plmn)
                // Phones report just RSSI which is useless for us
                is NetworkType.Lte -> null
                // Other types were added to SDK when this method was already deprecated
                // or contents of NeighboringCellInfo do not match requirements (CDMA does not have CID, ...)
                else -> null
            }
        } ?: emptyList()
    }

    private fun processGsm(it: NeighboringCellInfo, plmn: Network?): ICell? {
        val cid = it.cid.inRangeOrNull(CellGsm.CID_RANGE)
        val lac = it.lac.inRangeOrNull(CellGsm.LAC_RANGE)
        val rssi = it.getGsmRssi()?.inRangeOrNull(SignalGsm.RSSI_RANGE)

        return if (cid != null && lac != null) {
            CellGsm(
                network = plmn,
                cid = cid,
                lac = lac,
                bsic = null,
                band = null,
                signal = SignalGsm(rssi, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = subId,
                timestamp = null
            )
        } else null
    }

    private fun processWcdma(it: NeighboringCellInfo, plmn: Network?) : ICell? {
        val psc = it.psc.inRangeOrNull(CellWcdma.PSC_RANGE)
        val rssi = it.rssi.inRangeOrNull(SignalWcdma.RSSI_RANGE)

        return if (psc != null) {
            CellWcdma(
                network = plmn,
                ci = null,
                lac = null,
                psc = psc,
                band = null,
                signal = SignalWcdma(rssi, null, null, null, null),
                connectionStatus = NoneConnection(),
                subscriptionId = subId,
                timestamp = null
            )
        } else null
    }
}