package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell

/**
 * Some cells seem to pass all validations and act like they are correct but based on
 * several observations we know that those data are certainly invalid.
 * We will filter them out here.
 */
class InvalidCellsPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> = list.toMutableList().asSequence()
        // Google Pixel phones in LTE use this to say that cell is nearby but it's data will appear in a near future
        .filterNot { it is CellLte && it.pci == 0 && it.signal.rssi == -51 && it.signal.rsrp == null && it.signal.rsrq == null  }
        .toList()

}