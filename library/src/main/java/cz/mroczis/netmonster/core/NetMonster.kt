package cz.mroczis.netmonster.core

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.feature.detect.*
import cz.mroczis.netmonster.core.feature.merge.CellMerger
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat
import cz.mroczis.netmonster.core.util.isDisplayOn

internal class NetMonster(
    private val context: Context,
    private val telephony: ITelephonyManagerCompat
) : INetMonster {

    private val merger = CellMerger()

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(): List<ICell> = getCells(
        CellSource.CELL_LOCATION, CellSource.ALL_CELL_INFO, CellSource.NEIGHBOURING_CELLS
    )

    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCells(vararg sources: CellSource): List<ICell> {
        val oldApi = mutableListOf<ICell>().apply {
            if (sources.contains(CellSource.CELL_LOCATION)) {
                addAll(telephony.getCellLocation())
            }

            if (sources.contains(CellSource.NEIGHBOURING_CELLS)) {
                addAll(telephony.getNeighboringCellInfo())
            }
        }

        val newApi = if (sources.contains(CellSource.ALL_CELL_INFO)) {
            telephony.getAllCellInfo()
        } else emptyList()

        return merger.merge(oldApi, newApi, context.isDisplayOn())
    }

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getNetworkType(): NetworkType = getNetworkType(
        DetectorHspaDc(),
        DetectorLteAdvancedServiceState(),
        DetectorLteAdvancedCellInfo(),
        DetectorAosp() // best to keep last when all other strategies fail
    ) ?: NetworkTypeTable.get(NetworkType.UNKNOWN)

    override fun getNetworkType(vararg detectors: INetworkDetector): NetworkType? {
        for (detector in detectors) {
            detector.detect(this, telephony)?.let {
                return it
            }
        }

        return null
    }

}