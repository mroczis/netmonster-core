package cz.mroczis.netmonster.core

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * NetMonster Core's core.
 *
 * Adds multiple functions to AOSP.
 */
interface INetMonster {

    /**
     * Retrieves data from all of specified [sources] and then
     * merges them into one list removing possible duplicities.
     *
     * Resulting list contains only unique cells from selected [sources].
     * Note that some sources might not return valid data depending on
     * current SDK version.
     *
     * If none arguments are passed then this method attempts to utilise all sources
     * available on this device.
     *
     * For more information see documentation of each method that might be involved:
     *
     *  @see ITelephonyManagerCompat.getCellLocation
     *  @see ITelephonyManagerCompat.getNeighboringCellInfo
     *  @see ITelephonyManagerCompat.getAllCellInfo
     */
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    fun getCells(vararg sources: CellSource = CellSource.values()): List<ICell>

}