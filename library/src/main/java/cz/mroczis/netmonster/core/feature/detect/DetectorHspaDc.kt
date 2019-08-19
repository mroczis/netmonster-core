package cz.mroczis.netmonster.core.feature.detect

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.model.annotation.Experimental
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * Attempts to detect HSPA+42 / HSPA+DC.
 *
 * Based on different UARFCNs and same PSC in list of network scan result. Ignores invalid PSCs and bands
 */
@Experimental("Attempts to guess if HSPA+DC is installed. Cannot guarantee correctness.")
class DetectorHspaDc : INetworkDetector {

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    @SinceSdk(Build.VERSION_CODES.N)
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        detect(netmonster.getCells(CellSource.ALL_CELL_INFO))


    @VisibleForTesting
    internal fun detect(cells: List<ICell>) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val wcdmaCells = cells.filterIsInstance(CellWcdma::class.java)
            val primary = wcdmaCells.filter { it.connectionStatus is PrimaryConnection && it.band != null }

            if (primary.isNotEmpty()) {
                // Search for secondary cell with same PSC and different band
                val secondary = wcdmaCells.filter { it.connectionStatus !is PrimaryConnection && it.band != null }

                secondary.firstOrNull { sec ->
                    primary.firstOrNull { prim -> prim.band?.number == sec.band?.number && prim.band?.channelNumber != sec.band?.channelNumber } != null
                }?.let {
                    NetworkTypeTable.get(NetworkType.HSPA_DC)
                }
            } else {
                // Primary / serving WCDMA cell is required
                null
            }

        } else {
            // UARFCN was added in Android N, till that version we were not able to detect HSPA+DC
            null
        }


}