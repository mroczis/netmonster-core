package cz.mroczis.netmonster.core.feature.detect

import android.Manifest
import android.os.Build
import android.telephony.ServiceState
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat


/**
 * Attempts to detect LTE Advanced / LTE Carrier aggregation
 *
 * Based on [ServiceState]'s contents added in Android Q which describe if aggregation is currently active.
 */
class DetectorLteAdvancedServiceState : INetworkDetector {

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    @SinceSdk(Build.VERSION_CODES.Q)
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            telephony.getTelephonyManager()?.serviceState?.toString()?.let {
                detect(it)
            }
        } else {
            null
        }

    @VisibleForTesting
    internal fun detect(serviceState: String) =
        if (serviceState.contains("mIsUsingCarrierAggregation = true") && serviceState.contains("cellIdentity=CellIdentityLte")) {
            NetworkTypeTable.get(NetworkType.LTE_CA)
        } else null

}