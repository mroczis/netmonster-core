package cz.mroczis.netmonster.core.telephony

import android.Manifest
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.callback.CellCallbackError
import cz.mroczis.netmonster.core.callback.CellCallbackSuccess
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.DisplayInfo
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.annotation.TillSdk
import cz.mroczis.netmonster.core.model.cell.ICell

interface ITelephonyManagerCompat {

    /**
     * Getter for AOSP's telephony manager that is used to obtain all info
     */
    fun getTelephonyManager(): TelephonyManager?

    /**
     * Current subscriber id, [Int.MAX_VALUE] if invalid or unspecified
     */
    fun getSubscriberId(): Int

    /**
     * Requests all available cell information from all radios on the device including the
     * camped/registered, serving, and neighboring cells.
     *
     * Make sure to call this method on worker thread since it might take a while to fetch
     * and process the data (reflection is involved here).
     *
     * This method always invokes request to RIL so returned data are as fresh as possible.
     * If any error occurs during processing then [onSuccess] is invoked with latest valid
     * data. If none are present then expect empty list.
     *
     * Based on:
     *  - [TelephonyManager.getAllCellInfo]
     *  - [TelephonyManager.requestCellInfoUpdate]
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @SinceSdk(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getAllCellInfo(
        onSuccess: CellCallbackSuccess
    )

    /**
     * Requests all available cell information from all radios on the device including the
     * camped/registered, serving, and neighboring cells.
     *
     * Make sure to call this method on worker thread since it might take a while to fetch
     * and process the data (reflection is involved here).
     *
     * This method always invokes request to RIL so returned data are as fresh as possible.
     * If [onError] is not passed then this method will call [onSuccess] with an empty list.
     *
     * Based on:
     *  - [TelephonyManager.getAllCellInfo]
     *  - [TelephonyManager.requestCellInfoUpdate]
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @SinceSdk(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getAllCellInfo(
        onSuccess: CellCallbackSuccess,
        onError: CellCallbackError?
    )

    /**
     * Works as other [getAllCellInfo] methods but intentionally BLOCKS current thread
     * till data are not retrieved from system and post-processed.
     *
     * This method always does not block current thread for longer than [timeoutMilliseconds].
     * In case of timeout empty list is returned, no exceptions are thrown here.
     *
     * @return freshest data possible, if error occurs then latest valid data or eventually empty list
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @SinceSdk(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getAllCellInfo(timeoutMilliseconds: Long = 500): List<ICell>

    /**
     * Attempts to retrieve cell information using older telephony methods.
     * Result is eventually merged into our [ICell] representation.
     *
     * Since underlying methods are deprecated AOSP advises you to migrate to [getAllCellInfo]
     * if possible. However not all terminals do not work properly with new way of retrieving
     * cells.
     *
     * Supports only [ICell] subset:
     *  - [cz.mroczis.netmonster.core.model.cell.CellCdma]
     *  - [cz.mroczis.netmonster.core.model.cell.CellGsm]
     *  - [cz.mroczis.netmonster.core.model.cell.CellWcdma]
     *  - [cz.mroczis.netmonster.core.model.cell.CellLte]
     *
     * Based on:
     *  - [TelephonyManager.getCellLocation]
     *  - [TelephonyManager.getNetworkOperator]
     *  - [TelephonyManager.getNetworkType]
     */
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    fun getCellLocation(): List<ICell>


    /**
     * Returns the neighboring cell information for GSM (CID, LAC, RSSI) and WCDMA networks (PSC, RSSI).
     * Other networks are not supported. To get more information use [getAllCellInfo].
     *
     * Supports only [ICell] subset:
     *  - [cz.mroczis.netmonster.core.model.cell.CellGsm]
     *  - [cz.mroczis.netmonster.core.model.cell.CellWcdma]
     *
     * Based on:
     *  - [TelephonyManager.getNeighboringCellInfo]
     *  - [TelephonyManager.getNetworkOperator]
     */
    @WorkerThread
    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    @TillSdk(
        sdkInt = Build.VERSION_CODES.Q,
        fallbackBehaviour = "On Q+ returns empty list since method was removed from SDK"
    )
    fun getNeighboringCellInfo(): List<ICell>

    /**
     * Currently active network technology grabbed from AOSP and mapped to [NetworkType].
     *
     * Based on:
     *  - [TelephonyManager.getNetworkType]
     */
    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getNetworkType(): NetworkType


    /**
     * Obtains synchronously current [ServiceState].
     *
     * This information was available since SDK 1, however till
     * SDK 26 registering of [PhoneStateListener] was required to obtain the data.
     * This method just simplifies access to the data.
     *
     * Based on:
     *  - [TelephonyManager.getServiceState]
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getServiceState(): ServiceState?

    /**
     * PLMN of currently registered network.
     * Expect `null` in CDMA networks and when phone is not connected to networks.
     * Some devices return valid values even when they are in 'Emergency calls only' mode.
     *
     * Based on:
     *  - [TelephonyManager.getServiceState]
     *  - [TelephonyManager.getNetworkOperator] (fallback if previous one returns `null`)
     */
    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getNetworkOperator(): Network?

    /**
     * PLMN of SIM card that is assigned to current subscription
     * Expect `null` in CDMA networks and when phone is not connected to networks.
     * 
     * Based on:
     *  - [TelephonyManager.getSimOperator]
     */
    @WorkerThread
    fun getSimOperator() : Network?

    /**
     * Fetches the most recent information about signal from the modem.
     * In case of failure returns cached data.
     *
     * Based on:
     *  - [TelephonyManager.getSignalStrength]
     */
    @WorkerThread
    fun getSignalStrength() : SignalStrength?

    /**
     * Fetches information about current network type and possible override
     * that should be presented to user.
     *
     * On SDKs lower than [Build.VERSION_CODES.R] always returns [DisplayInfo] with unknwon
     * network type and none override type
     */
    @WorkerThread
    @SinceSdk(Build.VERSION_CODES.R)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE])
    fun getDisplayInfo(): DisplayInfo

}