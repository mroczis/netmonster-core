package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Attempts to find one primary cell if none of cells is marked as primary.
 * Marks first cell that matches pre-conditions to be primarily serving.
 *
 * 100 % reliable for WCDMA, LTE, TD-SCDMA, NR
 */
class PrimaryCellPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (list.firstOrNull { it.connectionStatus is PrimaryConnection } != null) {
            list
        } else {
            // No Primary connection found -> in this case phone might be in emergency calls
            // mode only. Which means that Android is connected to some cell as primary
            // but it does not admit the fact.
            // In case of NR, LTE and WCDMA networks it's easy to find the cell - CID is filled
            // only for serving cells. In case of GSM we grab 1st cell.

            list.firstOrNull { it.let(PrimaryConnectionDetector()) }?.let { primaryCell ->
                list.toMutableList().apply {
                    remove(primaryCell)
                    add(0, primaryCell.let(SwitchToPrimaryConnection()))
                }
            } ?: list
        }

    /**
     * Switches connection status of given cell to [PrimaryConnection]
     */
    private class SwitchToPrimaryConnection : ICellProcessor<ICell> {
        override fun processCdma(cell: CellCdma) =
            cell.copy(connectionStatus = PrimaryConnection())

        override fun processGsm(cell: CellGsm) =
            cell.copy(connectionStatus = PrimaryConnection())

        override fun processLte(cell: CellLte) =
            cell.copy(connectionStatus = PrimaryConnection())

        override fun processNr(cell: CellNr) =
            cell.copy(connectionStatus = PrimaryConnection())

        override fun processTdscdma(cell: CellTdscdma) =
            cell.copy(connectionStatus = PrimaryConnection())

        override fun processWcdma(cell: CellWcdma) =
            cell.copy(connectionStatus = PrimaryConnection())
    }

    /**
     * Searches for cell that can be possibly primarily serving one
     */
    private class PrimaryConnectionDetector : ICellProcessor<Boolean> {
        override fun processCdma(cell: CellCdma) =
            cell.bid != null && cell.nid != null

        override fun processGsm(cell: CellGsm) =
            cell.cid != null && cell.lac != null

        override fun processLte(cell: CellLte) =
            cell.cid != null && cell.tac != null && cell.pci != null

        override fun processNr(cell: CellNr) =
            cell.nci != null && cell.tac != null && cell.pci != null

        override fun processTdscdma(cell: CellTdscdma) =
            cell.cid != null && cell.lac != null && cell.cpid != null

        override fun processWcdma(cell: CellWcdma) =
            cell.cid != null && cell.lac != null && cell.psc != null
    }

}