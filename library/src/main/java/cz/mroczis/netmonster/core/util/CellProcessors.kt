package cz.mroczis.netmonster.core.util

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Built in [ICellProcessor] implementations that are used in NetMonster core
 * to post-process raw data from Android's API
 */
object CellProcessors {

    /**
     * Switches connection status of given cell to [PrimaryConnection]
     */
    val SWITCH_TO_PRIMARY_CONNECTION = object : ICellProcessor<ICell> {
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
    val CAN_BE_PRIMARY_CONNECTION = object : ICellProcessor<Boolean> {
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