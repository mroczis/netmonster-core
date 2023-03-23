package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.SubscriptionId
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection

/**
 * Devices equipped with Samsung modems tend to return fixed '0' value when it comes to LTE's timing advance.
 * This postprocessor will block all TA occurrences till value grater than zero shows up.
 */
class TimingAdvancePostprocessor(
    /**
     * Enables automatic postprocessing for all other runs till Android
     * build changes (security patch, major / minor version, ...)
     */
    private val setValidTaDetected: (id: SubscriptionId) -> Unit,
    /**
     * Checks if endianness should be flipped automatically not matter current state
     */
    private val wasValidTaDetected: (id: SubscriptionId) -> Boolean,
) : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        list.map { cell ->
            if (cell is CellLte) {
                // Neighbouring cells cannot have TA since phone does not actively communicate with them
                val isNeighbourInvalid = cell.connectionStatus is NoneConnection && cell.signal.timingAdvance != null
                val validTaDetected = wasValidTaDetected(cell.subscriptionId)
                val taPositive = cell.signal.timingAdvance?.let { it > 0 } == true

                if (!validTaDetected && taPositive) {
                    setValidTaDetected(cell.subscriptionId)
                }

                if (isNeighbourInvalid || (!validTaDetected && !taPositive)) {
                    cell.withoutTimingAdvance()
                } else {
                    cell
                }
            } else cell
        }

    /**
     * Erases TA from signal
     */
    private fun CellLte.withoutTimingAdvance() = copy(signal = signal.copy(timingAdvance = null))
}