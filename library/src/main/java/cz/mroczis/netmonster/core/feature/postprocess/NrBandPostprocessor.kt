package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.db.BandTableNr
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * NR neighbours usually come without PLMN, so their band was resolved without regional priorities
 * of [BandTableNr] (n77 vs n78, n7 vs n41, ...).
 *
 * We take MCC of serving cells on the same subscription and resolve the band again. Band number found
 * so far acts as a hint, regional priorities override it when it contradicts them.
 */
class NrBandPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> {
        val mccs = list
            .filter { it.connectionStatus is PrimaryConnection }
            .groupBy { it.subscriptionId }
            .mapValues { (_, cells) -> cells.mapNotNull { it.network?.mcc }.distinct().singleOrNull() }

        return list.map { cell ->
            val band = (cell as? CellNr)?.band
            val mcc = mccs[cell.subscriptionId]
            if (cell is CellNr && cell.network == null && band != null && mcc != null) {
                cell.copy(
                    band = BandTableNr.map(
                        arfcn = band.downlinkArfcn,
                        bandHints = band.number?.let { intArrayOf(it) } ?: intArrayOf(),
                        mcc = mcc,
                    )
                )
            } else {
                cell
            }
        }
    }
}
