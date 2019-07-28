package cz.mroczis.netmonster.core.telephony.mapper

import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal

/**
 * Transforms result of [TelephonyManager.getAllCellInfo] into our list
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class CellInfoMapper : ICellMapper<List<CellInfo>> {

    override fun map(model: List<CellInfo>): List<ICell> =
        model.mapNotNull {
            if (it is CellInfoGsm) {
                mapGsm(it)
            } else if (it is CellInfoLte) {
                null
            } else if (it is CellInfoCdma) {
                null
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && it is CellInfoWcdma) {
                null
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoTdscdma) {
                null
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr) {
                null
            } else null
        }


    private fun mapGsm(model: CellInfoGsm): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

}