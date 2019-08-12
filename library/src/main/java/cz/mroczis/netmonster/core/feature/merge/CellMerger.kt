package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Merges cells from older API and newer API.
 */
class CellMerger : ICellMerger {

    private val primaryMerger : ICellMerger = CellMergerPrimary()
    private val otherMerger : ICellMerger = CellMergerNotPrimary()

    override fun merge(oldApi: List<ICell>, newApi: List<ICell>, displayOn : Boolean) : List<ICell> {
        val oldPrimary = oldApi.filter { it.connectionStatus is PrimaryConnection }
        val newPrimary = newApi.filter { it.connectionStatus is PrimaryConnection }

        val oldOther = oldApi.toMutableList().apply {
            removeAll(oldPrimary)
        }
        val newOther = newApi.toMutableList().apply {
            removeAll(newPrimary)
        }

        return mutableListOf<ICell>().apply {
            addAll(primaryMerger.merge(oldPrimary, newPrimary, displayOn))
            addAll(otherMerger.merge(oldOther, newOther, displayOn))
        }
    }

}