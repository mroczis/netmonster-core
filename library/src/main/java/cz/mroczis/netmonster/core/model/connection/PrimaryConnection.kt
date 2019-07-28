package cz.mroczis.netmonster.core.model.connection

import cz.mroczis.netmonster.core.model.cell.ICell

/**
 * Cell is primary for data or voice communication with outer world
 */
data class PrimaryConnection(
    val aggregatedCells: List<ICell> = emptyList()
) : IConnection