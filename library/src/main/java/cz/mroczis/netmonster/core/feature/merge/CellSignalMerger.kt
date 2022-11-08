package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte

/**
 * Merges data from new API represented by [CellSource.ALL_CELL_INFO] and
 * signal API represented by [CellSource.SIGNAL_STRENGTH]
 */
internal class CellSignalMerger {

    /**
     * Merge in this case adds nr cells from [signalApi] if [newApi] does not contain
     * NR cell that is bound to same the subscription id and is primary, it also adds LTE signal
     * if it's missing in the LTE primary cell
     */
    fun merge(newApi: List<ICell>, signalApi: List<ICell>): List<ICell> {
        val nrCells = newApi.filterIsInstance(CellNr::class.java)
        val signalApiNr = signalApi.filterIsInstance(CellNr::class.java)
        val nonPresentNr = signalApiNr.toMutableList().filter { signalCell ->
            nrCells.find {
                it.subscriptionId == signalCell.subscriptionId && it.connectionStatus is PrimaryConnection
            } == null
        }

        val newApiMutable = newApi.toMutableList()

        if (nonPresentNr.isNotEmpty()) {
            if (nrCells.size == 1 && signalApiNr.size == 1) {
                // Usually NR in NSA, signal source has correct signal, CellInfo source has PSC and ARFCN, must merge manually
                // HUAWEI CDY-NX9A
                val mergedCell = nrCells[0] mergeWith signalApiNr[0]
                newApiMutable.apply {
                    remove(nrCells[0])
                    add(mergedCell)
                }
            } else if (nrCells.size > 1 && signalApiNr.size == 1) {
                // Multiple NR cells (two sims with working 5G), decide using subscription id
                val targetSub = signalApiNr[0].subscriptionId
                val sourceCell = nrCells.find { it.subscriptionId == targetSub }
                if (sourceCell != null) {
                    val mergedCell = sourceCell mergeWith signalApiNr[0]
                    newApiMutable.apply {
                        remove(nrCells[0])
                        add(mergedCell)
                    }
                } else {
                    // No matching sub, pass everything
                    newApiMutable.apply {
                        addAll(nonPresentNr)
                    }
                }
            } else {
                newApiMutable.apply {
                    addAll(nonPresentNr)
                }
            }
        }

        val ltePCCnoSignal = newApi.filterIsInstance(CellLte::class.java)
            .filter { it.connectionStatus is PrimaryConnection && it.signal == SignalLte.EMPTY }
        if (ltePCCnoSignal.isNotEmpty()) {
            val signalApiLte = signalApi.filterIsInstance(CellLte::class.java)
            ltePCCnoSignal.forEach { ltePCC ->
                signalApiLte.find { it.subscriptionId == ltePCC.subscriptionId }
                    ?.let {
                        val mergedCell = ltePCC mergeWith it
                        newApiMutable.apply {
                            remove(ltePCC)
                            add(mergedCell)
                        }
                    }
            }
        }

        return newApiMutable.toList()
    }


    private infix fun CellNr.mergeWith(other: CellNr): CellNr = copy(
        connectionStatus = connectionStatus.takeIf { it is PrimaryConnection }
            ?: other.connectionStatus.takeIf { it is PrimaryConnection }
            ?: connectionStatus.takeIf { it is SecondaryConnection }
            ?: other.connectionStatus.takeIf { it is SecondaryConnection } ?: NoneConnection(),
        nci = nci ?: other.nci,
        tac = tac ?: other.tac,
        pci = pci ?: other.pci,
        band = band ?: other.band,
        signal = signal.copy(
            csiRsrp = signal.csiRsrp minOr other.signal.csiRsrp,
            csiRsrq = signal.csiRsrq minOr other.signal.csiRsrq,
            csiSinr = signal.csiSinr minOr other.signal.csiSinr,
            ssRsrp = signal.ssRsrp minOr other.signal.ssRsrp,
            ssRsrq = signal.ssRsrq minOr other.signal.ssRsrq,
            ssSinr = signal.ssSinr minOr other.signal.ssSinr
        )
    )

    private infix fun CellLte.mergeWith(other: CellLte): CellLte = copy(
        signal = other.signal.merge(signal)
    )

    /**
     * Takes first not not null or min out of two
     */
    private infix fun Int?.minOr(other: Int?) =
        if (this != null && other != null) {
            kotlin.math.min(this, other)
        } else this ?: other
}