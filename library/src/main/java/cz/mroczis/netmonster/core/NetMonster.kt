package cz.mroczis.netmonster.core

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
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
    override fun getCells(vararg sources: CellSource): List<ICell> {
        val oldApi = mutableListOf<ICell>().apply {
            if (sources.contains(CellSource.CELL_LOCATION)) {
                addAll(telephony.getCellLocation())
            }

            if (sources.contains(CellSource.NEIGHBOURING_CELLS)) {
                addAll(telephony.getNeighbouringCells())
            }
        }

        val newApi = if (sources.contains(CellSource.ALL_CELL_INFO)) {
            telephony.getAllCellInfo()
        } else emptyList()

        return merger.merge(oldApi, newApi, context.isDisplayOn())
    }

}