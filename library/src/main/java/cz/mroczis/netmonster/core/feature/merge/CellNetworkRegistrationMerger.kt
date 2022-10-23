package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.*

/**
 * Merges cells with cells from network registration info.
 * Merge is simple - if cell with CID is not present in the original list then it's appended
 */
class CellNetworkRegistrationMerger {

    fun merge(existing: List<ICell>, nriCells: List<ICell>): List<ICell> {
        val toAdd = nriCells.filterNot { cell -> existing containsSimilar cell }
        return existing + toAdd
    }

    private infix fun List<ICell>.containsSimilar(other: ICell) = any { candidate ->
        if (candidate.subscriptionId == other.subscriptionId) {
            when (candidate) {
                is CellGsm -> (other as? CellGsm)?.cid == candidate.cid
                is CellWcdma -> (other as? CellWcdma)?.ci == candidate.ci
                is CellLte -> (other as? CellLte)?.eci == candidate.eci
                is CellNr -> (other as? CellNr)?.nci == candidate.nci
                is CellCdma -> (other as? CellCdma)?.bid == candidate.bid
                is CellTdscdma -> (other as? CellTdscdma)?.ci == candidate.ci
                else -> false
            }
        } else false
    }

}