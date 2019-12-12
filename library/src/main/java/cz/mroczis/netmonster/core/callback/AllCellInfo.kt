package cz.mroczis.netmonster.core.callback

import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.model.CellError

typealias CellCallbackSuccess = (cells: List<ICell>) -> Unit
typealias CellCallbackError = (error: CellError) -> Unit
