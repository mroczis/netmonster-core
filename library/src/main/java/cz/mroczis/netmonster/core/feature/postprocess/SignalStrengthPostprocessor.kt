package cz.mroczis.netmonster.core.feature.postprocess

import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.signal.ISignal

/**
 * Iterates over [SignalStrength] provided by [TelephonyManager] and attempts enrich
 * already parsed serving cells with new signal data that are sometimes only available
 * in this object.
 *
 * Usually phones report different signal values when it comes to instance of [CellSignalStrength]
 * bound to [CellInfo] and [SignalStrength] object that is just freely existing in [TelephonyManager].
 *
 * When speaking of [SignalStrength] from [TelephonyManager] we do not access it directly,
 * we use [ICell] instances that are already parsed & validated by Core (old getCellLocation)
 */
class SignalStrengthPostprocessor(
    private val pairedCellGetter: (Int) -> ICell?
) : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        list.toMutableList().map {cell ->
            if (cell.connectionStatus is PrimaryConnection) {
                pairedCellGetter.invoke(cell.subscriptionId)?.let { pairedCell ->
                    cell.let(SignalStrengthModifier(pairedCell))
                } ?: cell
            } else {
                cell
            }
        }


    /**
     * Merges [ISignal] for two [ICell]. Prioritising data that are saved in
     * [ICell] that is modified, not [category].
     *
     * [ISignal] is merged only if primary cell indicator (usually CID) match.
     */
    private class SignalStrengthModifier(
        private val candidate: ICell
    ) : ICellProcessor<ICell> {
        override fun processCdma(cell: CellCdma): ICell =
            if (candidate is CellCdma && candidate.bid == cell.bid) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

        override fun processGsm(cell: CellGsm): ICell =
            if (candidate is CellGsm && candidate.cid == cell.cid) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

        override fun processLte(cell: CellLte): ICell =
            if (candidate is CellLte && candidate.eci == cell.eci) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

        override fun processNr(cell: CellNr): ICell =
            if (candidate is CellNr && candidate.nci == cell.nci) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

        override fun processTdscdma(cell: CellTdscdma): ICell =
            if (candidate is CellTdscdma && candidate.ci == cell.ci) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

        override fun processWcdma(cell: CellWcdma): ICell =
            if (candidate is CellWcdma && candidate.ci == cell.ci) {
                cell.copy(signal = cell.signal.merge(candidate.signal))
            } else {
                cell
            }

    }
}