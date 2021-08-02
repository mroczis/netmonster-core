package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import android.telephony.ServiceState
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Adds cell bandwidth from [ServiceState] to the cell objects.
 * [ServiceState].cellBandwidths even reports bandwidths for secondary cells, but determining
 * the order is complicated.
 */
class CellBandwidthPostprocessor(
    private val serviceStateGetter: (Int) -> ServiceState?
) : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            list.map { cell ->
                if (cell is CellLte && cell.connectionStatus is PrimaryConnection && cell.bandwidth == null) {
                    serviceStateGetter.invoke(cell.subscriptionId)?.let { serviceState ->
                        cell.copy(bandwidth = serviceState.cellBandwidths.firstOrNull())
                    } ?: cell
                } else cell
            }
        } else {
            list
        }
}
