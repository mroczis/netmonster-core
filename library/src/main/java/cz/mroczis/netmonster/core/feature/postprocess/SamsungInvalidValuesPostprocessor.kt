package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.util.isSamsung

/**
 * Well. There's an Android documentation how invalid values should look like.
 * And then out there, in a wild, are Samsung devices which simply use different
 * constants.
 *
 * This class attempts to remove most frequent mishaps.
 * Note that it can eventually also discard valid values, but in most of the cases it does not.
 */
class SamsungInvalidValuesPostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (isSamsung()) {
            list.map {
                if (it.connectionStatus is NoneConnection) {
                    it.let(neighbourMetadataFixer)
                } else {
                    it
                }
            }
        } else {
            list
        }

    private val neighbourMetadataFixer = object : ICellProcessor<ICell> {

        override fun processGsm(cell: CellGsm) = cell.copy(
            signal = cell.signal.copy(
                rssi = cell.signal.rssi?.takeIf { it != -51 },
                timingAdvance = cell.signal.timingAdvance?.takeIf { it > 0 },
                bitErrorRate = cell.signal.bitErrorRate?.takeIf { it > 0 }
            )
        )

        override fun processLte(cell: CellLte) = cell.copy(
            signal = cell.signal.copy(
                rssi = cell.signal.rssi?.takeIf { it != -51 },
                timingAdvance = cell.signal.timingAdvance?.takeIf { it > 0 },
            )
        )

        override fun processWcdma(cell: CellWcdma) = cell.copy(
            signal = cell.signal.copy(
                rssi = cell.signal.rssi?.takeIf { it != -51 },
                bitErrorRate = cell.signal.bitErrorRate?.takeIf { it > 0 },
                ecno = cell.signal.ecno?.takeIf { it != 0 },
            )
        )

        // Not enough information for instance below, so no sanitization
        override fun processCdma(cell: CellCdma): ICell = cell
        override fun processNr(cell: CellNr) = cell
        override fun processTdscdma(cell: CellTdscdma) = cell
    }
}