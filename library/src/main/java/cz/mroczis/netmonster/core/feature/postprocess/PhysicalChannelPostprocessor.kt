package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Enriches data, works only for LTE.
 *
 * Basic abilities:
 *  - adds bandwidth to [PrimaryConnection], since [Build.VERSION_CODES.P]
 *  - adds bandwidth to [NoneConnection] and marks cell [SecondaryConnection], since [Build.VERSION_CODES.Q]
 */
@SinceSdk(Build.VERSION_CODES.P)
class PhysicalChannelPostprocessor(
    private val physicalChannelConfigGetter: (Int) -> List<PhysicalChannelConfig>
) : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            list.groupBy { it.subscriptionId }
                .map { (subId, cells) ->
                    val configs = physicalChannelConfigGetter
                        .invoke(subId)
                        .toMutableList()

                    cells.map { cell ->
                        if (cell is CellLte) {
                            var resultCell: CellLte = cell

                            // Round 1 - we care just about PCI which is supported in newer AOSP
                            // versions, here we also get rid of cells that are already used
                            val iterator = configs.iterator()
                            while (iterator.hasNext()) {
                                val config = iterator.next()
                                val modifiedCell = mergeByCode(resultCell, config)

                                if (modifiedCell != null) {
                                    resultCell = modifiedCell
                                    iterator.remove()
                                }
                            }

                            // Round 2 - if there's something left we will try to merge it using
                            // connectionStatus
                            for (config in configs) {
                                val modifiedCell = mergeByConnection(resultCell, config)
                                if (modifiedCell != null) {
                                    resultCell = modifiedCell
                                }
                            }

                            resultCell
                        } else {
                            cell
                        }
                    }

                }.flatten()
        } else {
            list
        }

    /**
     * Merging by PCI, works since Android Q.
     *
     * Attempts to merge [cell] with [config].
     *  - Some phones do not return valid bandwidth using standard API, this method will attempt to fix
     * the issue
     *  - Some phones also indicate secondary connection to some cells, we take advantage of that
     */
    private fun mergeByCode(cell: CellLte, config: PhysicalChannelConfig): CellLte? =
        if (cell.pci == config.pci) {
            val connection =
                if (cell.connectionStatus is PrimaryConnection) {
                    cell.connectionStatus // Keep primary
                } else if (config.connectionStatus is SecondaryConnection) {
                    config.connectionStatus // Switch to secondary as reported by PhysicalChannel
                } else if (cell.connectionStatus is NoneConnection) {
                    // Samsung reports via PCC Primary connection for secondary cells, at least
                    // that's what I saw on multiple devices, so keep it as a guess
                    SecondaryConnection(isGuess = config.connectionStatus is PrimaryConnection)
                } else {
                    // Otherwise just keep it as it was
                    cell.connectionStatus
                }

            cell.copy(
                bandwidth = cell.bandwidth ?: config.bandwidth,
                connectionStatus = connection
            )
        } else {
            null
        }

    /**
     * Merging by ConnectionStatus, works since P.
     *
     * Attempts to merge [cell] with [config].
     *  - Some phones do not return valid bandwidth using standard API, this method will attempt to fix
     * the issue
     *  - Some phones also indicate secondary connection to some cells, we take advantage of that
     */
    private fun mergeByConnection(cell: CellLte, config: PhysicalChannelConfig): CellLte? =
        if (cell.bandwidth == null) {
            if (cell.connectionStatus == config.connectionStatus && cell.connectionStatus is PrimaryConnection) {
                // We can add bandwidth if it's not filled yet
                cell.copy(bandwidth = cell.bandwidth ?: config.bandwidth)
            } else if (cell.connectionStatus !is PrimaryConnection && config.connectionStatus !is PrimaryConnection && cell.pci == config.pci) {
                // PhysicalChannelConfig is the first source that is able to identify SecondaryConnections
                // since Android 9
                cell.copy(
                    bandwidth = cell.bandwidth ?: config.bandwidth,
                    connectionStatus = config.connectionStatus
                )
            } else null
        } else null


}
