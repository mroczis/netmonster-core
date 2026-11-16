package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection

/**
 * Some modems copy identity of the LTE anchor to the NR cell whilst connected to
 * NR NSA and mark it as primary. Such cell would look like NR SA.
 *
 * Two variants are seen in the wild:
 *
 * 1. The NR cell carries the anchor's ECI in its NCI (Xiaomi 2602BPC18G, reported).
 * 2. The NR cell has no NCI and no PLMN at all, but its PCI and TAC are the
 *    anchor's (Xiaomi 15 / HyperOS 4.0.4.0, Qualcomm `6.6.118-android15-8`,
 *    Android 17). The framework then reports an NR primary whose PCI changes
 *    with every LTE cell change while the modem knows the real NR PCI - on that
 *    device the QMI `GET_SYS_INFO` response carries NR PCI 776 for n41 while
 *    `CellIdentityNr.pci` reports the LTE anchor's PCI.
 *
 * For both variants the identity is not the NR cell's own, so we drop the copied
 * fields and treat the cell as an NSA secondary one.
 */
class NrIdentityFromLtePostprocessor : ICellPostprocessor {

    override fun postprocess(list: List<ICell>) = list.groupBy { it.subscriptionId }.flatMap { (_, cells) ->
        val lte = cells.filterIsInstance<CellLte>().filter {
            it.connectionStatus is PrimaryConnection && it.eci != null
        }

        cells.map { cell ->
            if (cell !is CellNr || cell.connectionStatus !is PrimaryConnection) {
                return@map cell
            }

            val copiedNci = cell.nci != null && lte.any { it.eci?.toLong() == cell.nci }
            // No NCI at all but the anchor's PCI and TAC: the whole identity is the anchor's.
            val copiedPciTac = cell.nci == null && cell.pci != null &&
                lte.any { it.pci == cell.pci && it.tac == cell.tac }

            when {
                copiedNci -> cell.copy(nci = null, connectionStatus = SecondaryConnection(isGuess = false))
                copiedPciTac -> cell.copy(
                    pci = null,
                    tac = null,
                    connectionStatus = SecondaryConnection(isGuess = false),
                )
                else -> cell
            }
        }
    }
}
