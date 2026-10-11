package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Some modems (Xiaomi 2602BPC18G) copy identity of LTE anchor (ECI, TAC) to NR cell
 * whilst connected to NR NSA and mark it as primary. Such cell would look like NR SA.
 *
 * If NR primary cell has the same NCI as LTE primary cell on the same subscription
 * we drop NCI and treat the cell as NSA secondary one.
 */
class NrIdentityFromLtePostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>) = list.groupBy { it.subscriptionId }.flatMap { (_, cells) ->
        val lte = cells.filterIsInstance<CellLte>().filter {
            it.connectionStatus is PrimaryConnection && it.eci != null
        }

        cells.map { cell ->
            if (cell is CellNr && cell.connectionStatus is PrimaryConnection && cell.nci != null &&
                lte.any { it.eci?.toLong() == cell.nci }
            ) {
                cell.copy(nci = null, connectionStatus = SecondaryConnection(isGuess = false))
            } else {
                cell
            }
        }
    }
}
