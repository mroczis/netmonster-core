package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Merges cells from older API and newer API.
 */
class CellMerger {

    private val primaryMerger : ICellMerger = CellMergerPrimary()
    private val otherMerger : ICellMerger = CellMergerNotPrimary()

    fun merge(oldApi: List<ICell>, newApi: List<ICell>, displayOn : Boolean, subscriptions: List<Int>) : List<ICell> =
        subscriptions.flatMap { subId ->
            val old = oldApi.filter { it.subscriptionId == subId }
            val new = newApi.filter { it.subscriptionId == subId }

            val oldPrimary = old.filter { it.connectionStatus is PrimaryConnection }
            val newPrimary = new.filter { it.connectionStatus is PrimaryConnection }

            val oldOther = old.toMutableList().apply {
                removeAll(oldPrimary)
            }
            val newOther = new.toMutableList().apply {
                removeAll(newPrimary)
            }

            primaryMerger.merge(oldPrimary, newPrimary, displayOn) + otherMerger.merge(oldOther, newOther, displayOn)
        }
}