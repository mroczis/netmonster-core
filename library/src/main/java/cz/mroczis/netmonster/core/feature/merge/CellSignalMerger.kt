package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Merges data from new API represented by [CellSource.ALL_CELL_INFO] and
 * signal API represented by [CellSource.SIGNAL_STRENGTH]
 */
class CellSignalMerger {

    /**
     * Merge in this case adds cell from [signalApi] if [newApi] does not contain
     * NR cell that is bound to same the subscription id and is primary.
     */
    fun merge(newApi: List<ICell>, signalApi: List<CellNr>) : List<ICell> {
        val nrCells = newApi.filterIsInstance(CellNr::class.java)
        val nonPresentNr = signalApi.toMutableList().filter { signalCell ->
            nrCells.find {
                it.subscriptionId == signalCell.subscriptionId && it.connectionStatus is PrimaryConnection
            } == null
        }

        return if (nonPresentNr.isEmpty()) {
            newApi
        } else {
            newApi.toMutableList().apply {
                addAll(nonPresentNr)
            }.toList()
        }
    }

}