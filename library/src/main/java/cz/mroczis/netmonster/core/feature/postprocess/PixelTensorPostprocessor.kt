package cz.mroczis.netmonster.core.feature.postprocess

import android.os.Build
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm

/**
 * Pixel-specific issues that came with Google Tensor (and Samsung modems)
 */
class PixelTensorPostprocessor : ICellPostprocessor {

    private val isTensorPixel = Build.PRODUCT.lowercase() in GOOGLE_TENSOR_DEVICES

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (isTensorPixel) {
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

        override fun processLte(cell: CellLte) = with(cell) {
            // GSM neighbouring cells marked as LTE
            if (band == null && pci != null && with(signal) { rsrp != null && rssi == null && rsrq == null }) {
                CellGsm(
                    network = network,
                    cid = null,
                    lac = null,
                    bsic = pci,
                    band = null,
                    signal = SignalGsm(rssi = signal.rsrp?.toInt(), bitErrorRate = null, timingAdvance = null),
                    connectionStatus = NoneConnection(),
                    subscriptionId = subscriptionId,
                    timestamp = timestamp,
                )
            } else {
                this
            }
        }

        // No problems detected
        override fun processGsm(cell: CellGsm) = cell
        override fun processWcdma(cell: CellWcdma) = cell
        override fun processCdma(cell: CellCdma): ICell = cell
        override fun processNr(cell: CellNr) = cell
        override fun processTdscdma(cell: CellTdscdma) = cell
    }

    companion object {
        private val GOOGLE_TENSOR_DEVICES = listOf(
            "oriole", // 6
            "raven", // 6 pro
            "bluejay", // 6a
            "panther", // 7
            "cheetah", // 7 pro
        )
    }
}