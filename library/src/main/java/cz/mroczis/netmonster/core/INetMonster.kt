package cz.mroczis.netmonster.core

import android.Manifest
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.feature.detect.*
import cz.mroczis.netmonster.core.feature.config.*
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.annotation.TillSdk
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat

/**
 * NetMonster Core's core.
 *
 * Adds multiple functions to AOSP.
 */
interface INetMonster {

    /**
     * Retrieves data from all sources available in AOSP and then
     * merges them into one list removing possible duplicities.
     *
     * For more information see documentation of each method that might be involved:
     *
     * @see ITelephonyManagerCompat.getCellLocation
     * @see ITelephonyManagerCompat.getNeighboringCellInfo
     * @see ITelephonyManagerCompat.getAllCellInfo
     */
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    fun getCells(): List<ICell>

    /**
     * Retrieves data from all of specified [sources] and then
     * merges them into one list removing possible duplicities.
     *
     * Resulting list contains only unique cells from selected [sources].
     * Note that some sources might not return valid data depending on
     * current SDK version.
     *
     * If none arguments are passed then you'll always get an empty list
     *
     * For more information see documentation of each method that might be involved:
     *
     * @see ITelephonyManagerCompat.getCellLocation
     * @see ITelephonyManagerCompat.getNeighboringCellInfo
     * @see ITelephonyManagerCompat.getAllCellInfo
     */
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    fun getCells(vararg sources: CellSource): List<ICell>


    /**
     * Attempts to detect presence of:
     * - LTE Advanced / LTE Carrier aggregation
     * - HSPA+ 42 / HSPA+ DC
     * using multiple other sources available in AOSP, mainly cells from [getCells].
     *
     * If none are present then falls back to AOSP's implementation.
     *
     * @param subId - subscription id as unique identifier for SIM/eSIM card
     *
     * @see TelephonyManager.getNetworkType
     * @see INetworkDetector
     */
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    fun getNetworkType(subId: Int) : NetworkType

    /**
     * Attempts to detect current network type using selected [detectors].
     * Detectors are used in an sequential order and first valid instance of [NetworkType] is returned so
     * order in which they are passed is important.
     *
     * You might write your own [INetworkDetector] or use subset of bundled ones:
     *  - [DetectorHspaDc] (experimental, works since [Build.VERSION_CODES.N])
     *  - [DetectorLteAdvancedNrServiceState] (stable, works since [Build.VERSION_CODES.P])
     *  - [DetectorLteAdvancedPhysicalChannel] (stable, works since [Build.VERSION_CODES.P])
     *  - [DetectorLteAdvancedCellInfo] (experimental, works since [Build.VERSION_CODES.N])
     *  - [DetectorAosp] (legacy AOSP)
     *
     * If [detectors] are empty or all of them return null then you'll get also null.
     *
     * @param subId - subscription id as unique identifier for SIM/eSIM card
     * @param detectors - set of detectors that are used to determine [NetworkType]
     */
    @WorkerThread
    fun getNetworkType(subId: Int, vararg detectors: INetworkDetector) : NetworkType?

    /**
     * Obtains synchronously currently active configurations for physical channel.
     *
     * This method is not publicly accessible in AOSP and can be used to detect multiple
     * active carriers when LTE is active.
     *
     * Works since [Build.VERSION_CODES.P] on some phones. Look to [PhysicalChannelConfig] for
     * more details.
     *
     * @param subId - subscription id as unique identifier for SIM/eSIM card
     *
     * @see PhysicalChannelConfig
     * @see DetectorLteAdvancedPhysicalChannel
     * @see PhysicalChannelConfigSource
     */
    @WorkerThread
    @SinceSdk(Build.VERSION_CODES.P)
    @TillSdk(
        sdkInt = Build.VERSION_CODES.Q,
        fallbackBehaviour = "Way to access this data was removed, expect empty list on Android Q+"
    )
    fun getPhysicalChannelConfiguration(subId: Int) : List<PhysicalChannelConfig>

}