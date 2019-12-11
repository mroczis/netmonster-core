package cz.mroczis.netmonster.core.telephony.mapper

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapCell
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapConnection
import cz.mroczis.netmonster.core.telephony.mapper.cell.mapSignal
import cz.mroczis.netmonster.core.util.CellProcessors

/**
 * Transforms result of [TelephonyManager.getAllCellInfo] into our list
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class CellInfoMapper : ICellMapper<List<CellInfo>?> {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun map(model: List<CellInfo>?): List<ICell> {
        val processed = model?.mapNotNull {
            if (it is CellInfoGsm) {
                mapGsm(it)
            } else if (it is CellInfoLte) {
                mapLte(it)
            } else if (it is CellInfoCdma) {
                mapCdma(it)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && it is CellInfoWcdma) {
                mapWcdma(it)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoTdscdma) {
                mapTdscdma(it)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr) {
                mapNr(it)
            } else null
        } ?: emptyList()

        return if (processed.firstOrNull { it.connectionStatus is PrimaryConnection } != null) {
            processed
        } else {
            // No Primary connection found -> in this case phone might be in emergency calls
            // mode only. Which means that Android is connected to some cell as primary
            // but it does not admit the fact.
            // In case of NR, LTE and WCDMA networks it's easy to find the cell - CID is filled
            // only for serving cells. In case of GSM we grab 1st cell.

            processed.firstOrNull {
                it.let(CellProcessors.CAN_BE_PRIMARY_CONNECTION)
            }?.let { primaryCell ->
                processed.toMutableList().apply {
                    remove(primaryCell)
                    add(0, primaryCell.let(CellProcessors.SWITCH_TO_PRIMARY_CONNECTION))
                }
            } ?: processed
        }
    }


    private fun mapGsm(model: CellInfoGsm): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

    private fun mapLte(model: CellInfoLte): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

    private fun mapCdma(model: CellInfoCdma): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun mapWcdma(model: CellInfoWcdma): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun mapTdscdma(model: CellInfoTdscdma): ICell? {
        val connection = model.mapConnection()
        val signal = model.cellSignalStrength.mapSignal()
        return model.cellIdentity.mapCell(connection, signal)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun mapNr(model: CellInfoNr): ICell? {
        val connection = model.mapConnection()
        val signal = (model.cellSignalStrength as? CellSignalStrengthNr)?.mapSignal()
        return (model.cellIdentity as? CellIdentityNr)?.mapCell(connection, signal)
    }

}