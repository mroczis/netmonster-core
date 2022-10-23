package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Some phones (Google Pixel 7 Pro) report all neighbouring as secondarily-serving cells.
 * This postprocessor attempts to fix it.
 *
 * https://issuetracker.google.com/issues/254843949
 */
class InvalidSecondaryCellsPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> {
        val invalid = list.all { it.connectionStatus is PrimaryConnection || (it.connectionStatus as? SecondaryConnection)?.isGuess == false }
        return if (invalid) {
            list.map {
                if ((it.connectionStatus as? SecondaryConnection)?.isGuess == false) {
                    it.let(CellConnectionSwitcher)
                } else {
                    it
                }
            }
        } else {
            list
        }
    }

    private object CellConnectionSwitcher : ICellProcessor<ICell> {
        override fun processCdma(cell: CellCdma): ICell = cell.copy(connectionStatus = NoneConnection())
        override fun processGsm(cell: CellGsm): ICell = cell.copy(connectionStatus = NoneConnection())
        override fun processLte(cell: CellLte): ICell = cell.copy(connectionStatus = NoneConnection())
        override fun processNr(cell: CellNr): ICell = cell.copy(connectionStatus = NoneConnection())
        override fun processTdscdma(cell: CellTdscdma): ICell = cell.copy(connectionStatus = NoneConnection())
        override fun processWcdma(cell: CellWcdma): ICell = cell.copy(connectionStatus = NoneConnection())
    }
}