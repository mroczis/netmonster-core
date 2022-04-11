package cz.mroczis.netmonster.core.feature.postprocess

import android.Manifest
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.util.isSamsung

/**
 * Samsung phones (e.g. b0sxeea) on Android 12+ have endianness issue (bytes are
 * incorrectly glued together).
 * This issue is present only for serving cells.
 *
 * Example:
 * - Input from the new API: 5220 (bin: 000101000 01100100)
 * - Correct value in the old API: 25620 (bin: 01100100 000101000)
 */
class SamsungEndiannessPostprocessor(
    /**
     * Returns suggested data that should be used once endianness issues is detected
     */
    private val getCellSkeleton: (Int) -> CellSkeleton?,
    /**
     * Enables automatic postprocessing for all other runs till Android
     * build changes (security patch, major / minor version, ...)
     */
    private val setEnabled: () -> Unit,
    /**
     * Checks if endianness should be flipped automatically not matter current state
     */
    private val isEnabled: () -> Boolean,
) : ICellPostprocessor {

    override fun postprocess(list: List<ICell>): List<ICell> =
        if (isSamsung() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.map { cell ->
                if (cell.connectionStatus is PrimaryConnection) {
                    when {
                        isEnabled() -> FlipEndianness()
                        cell.hasIncorrectEndiannessNow() -> {
                            setEnabled()
                            FlipEndianness()
                        }
                        else -> null
                    }
                } else {
                    null
                }?.let { postprocessor -> cell.let(postprocessor) } ?: cell
            }
        } else {
            list
        }

    /**
     * Flips endianness assuming 16-bit long number at most
     */
    @VisibleForTesting
    fun flipEndianness(number: Int) =
        (number and 0xFF shl 8) + (number shr 8 and 0xFF)

    /**
     * This method assumes 16-bit long number at most!
     * Checks if 1st byte of [first] equals to 2nd byte of [second] and vice versa
     */
    @VisibleForTesting
    fun hasFlippedEndianness(first: Int?, second: Int?): Boolean =
        if (first != null && second != null && first != second) {
            first.toByte() == (second shr 8).toByte() && second.toByte() == (first shr 8).toByte()
        } else {
            false
        }

    private fun ICell.hasIncorrectEndiannessNow() =
        getCellSkeleton(subscriptionId)?.let { cellSkeleton ->
            let(ShouldAreaFlipEndianness(cid = cellSkeleton.cid, area = cellSkeleton.area))
        } ?: false

    /**
     * Flips LAC / TAC endianness
     */
    private inner class FlipEndianness : ICellProcessor<ICell> {
        override fun processGsm(cell: CellGsm): ICell = cell.copy(lac = cell.lac?.flipEndianness)
        override fun processLte(cell: CellLte) = cell.copy(tac = cell.tac?.flipEndianness)
        override fun processTdscdma(cell: CellTdscdma) = cell.copy(lac = cell.lac?.flipEndianness)
        override fun processWcdma(cell: CellWcdma) = cell.copy(lac = cell.lac?.flipEndianness)
        override fun processNr(cell: CellNr): ICell = cell
        override fun processCdma(cell: CellCdma): ICell = cell

        private val Int.flipEndianness
            get() = flipEndianness(this)
    }

    /**
     * Checks if [area]'s endianness is flipped compared to TAC / LAC of other cells
     */
    private inner class ShouldAreaFlipEndianness(
        private val cid: Int?,
        private val area: Int?,
    ) : ICellProcessor<Boolean> {

        override fun processGsm(cell: CellGsm) = shouldFlipArea(cell.cid, cell.lac)
        override fun processLte(cell: CellLte) = shouldFlipArea(cell.eci, cell.tac)
        override fun processTdscdma(cell: CellTdscdma) = shouldFlipArea(cell.ci, cell.lac)
        override fun processWcdma(cell: CellWcdma) = shouldFlipArea(cell.ci, cell.lac)
        override fun processCdma(cell: CellCdma) = false // Not applicable for CDMA
        override fun processNr(cell: CellNr) = false  // Not enough data for NR NSA

        private fun shouldFlipArea(cellCid: Int?, cellArea: Int?): Boolean =
            cellCid == cid && hasFlippedEndianness(cellArea, area)
    }

    /**
     * Suggested cell metadata that should be used if endianness issues are detected
     */
    data class CellSkeleton(
        /**
         * Represents complete cell id (the longest one possible)
         */
        val cid: Int?,
        /**
         * Area location code / tracking area code
         * 16-bit at most
         */
        val area: Int?,
    )
}

/**
 * Reads data from old cell API and transforms it into input for [SamsungEndiannessPostprocessor]
 */
@get:RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
@Suppress("deprecation")
internal val TelephonyManager?.cellSkeleton
    get() = (this?.cellLocation as? GsmCellLocation)?.let {
        SamsungEndiannessPostprocessor.CellSkeleton(cid = it.cid, area = it.lac)
    }
