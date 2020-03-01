package cz.mroczis.netmonster.core.telephony

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import cz.mroczis.netmonster.core.callback.CellCallbackError
import cz.mroczis.netmonster.core.callback.CellCallbackSuccess
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.feature.config.ServiceStateSource
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.model.CellError
import cz.mroczis.netmonster.core.telephony.mapper.CellInfoMapper
import cz.mroczis.netmonster.core.telephony.mapper.CellLocationMapper
import cz.mroczis.netmonster.core.telephony.mapper.NeighbouringCellInfoMapper
import cz.mroczis.netmonster.core.util.isHuawei
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Modifies some functionalities of [TelephonyManager] and unifies access to
 * methods across all platform versions.
 */
internal open class TelephonyManagerCompat14(
    private val context: Context,
    private val subId: Int
) : ITelephonyManagerCompat {

    protected val telephony: TelephonyManager
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).createForSubscriptionId(subId)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isHuawei()) {
            (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).createForSubscriptionId(subId)
        } else {
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        }

    protected val cellInfoMapper = CellInfoMapper(subId)
    private val cellLocationMapper = CellLocationMapper(telephony)
    private val neighbouringCellInfoMapper = NeighbouringCellInfoMapper(telephony, subId)
    private val serviceStateSource = ServiceStateSource()

    override fun getTelephonyManager(): TelephonyManager? = telephony

    override fun getSubscriberId(): Int = subId

    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getAllCellInfo(
        onSuccess: CellCallbackSuccess
    ) = getAllCellInfo(onSuccess, null)

    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getAllCellInfo(
        onSuccess: CellCallbackSuccess,
        onError: CellCallbackError?
    ) =
        if (onError != null) {
            onError.invoke(CellError.UNSUPPORTED_AOSP_VERSION)
        } else {
            onSuccess.invoke(emptyList())
        }

    @WorkerThread
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getAllCellInfo(timeoutMilliseconds: Long): List<ICell> {
        var result = emptyList<ICell>()
        val asyncLock = CountDownLatch(1)

        getAllCellInfo { data ->
            result = data
            asyncLock.countDown()
        }

        try {
            asyncLock.await(timeoutMilliseconds, TimeUnit.MILLISECONDS)
        } catch (ignored: InterruptedException) {

        }

        return result
    }

    @Suppress("DEPRECATION")
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    override fun getCellLocation(): List<ICell> =
        cellLocationMapper.map(subId)

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    @WorkerThread
    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override fun getNeighboringCellInfo(): List<ICell> =
        try {
            // Reflection required cause we compile against SDK 29+ where this method
            // was removed
            val method = TelephonyManager::class.java.getMethod("getNeighboringCellInfo")
            val cells = method.invoke(telephony) as? List<NeighboringCellInfo>?
            neighbouringCellInfoMapper.map(cells)
        } catch (e: Exception) {
            emptyList()
        }

    @RequiresPermission(
        allOf = [Manifest.permission.READ_PHONE_STATE]
    )
    override fun getNetworkType(): NetworkType =
        NetworkTypeTable.get(telephony.networkType)

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getServiceState(): ServiceState? =
        serviceStateSource.get(telephony, subId)

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getNetworkOperator(): Network? =
        Network.map(getServiceState()?.operatorNumeric) ?: Network.map(telephony.networkOperator)

}