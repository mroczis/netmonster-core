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
 * Based on [ServiceState]'s contents added in Android O which describe if aggregation is currently active.
 */
class DetectorLteAdvancedServiceState : INetworkDetector {

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    @SinceSdk(Build.VERSION_CODES.O)
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephony.getTelephonyManager()?.serviceState?.toString()?.let { serviceState ->
                NetworkTypeTable.get(NetworkType.LTE_CA).takeIf { isUsingCarrierAggregation(serviceState) }
            }
        } else {
            null
        }

    /**
     * Android O - IsUsingCarrierAggregation=true
     * Android O_mr1 and P - mIsUsingCarrierAggregation=true
     * Android 10 - mIsUsingCarrierAggregation = true
     * Huawei connected to 5G NSA - accessNetworkTechnology=LTE-CA, mIsUsingCarrierAggregation=false
     *                              and mRilDataRadioTechnology=20(NR) - Tested on Mate 20X 5G
     * LG Android O, P - AdvanceMode1 and mIsUsingCarrierAggregation=false - Tested on LG G7
     */
    @VisibleForTesting
    internal fun isUsingCarrierAggregation(serviceState: String) =
        ((serviceState.contains("[mI|i]sUsingCarrierAggregation ?= ?true".toRegex()) ||
                serviceState.contains("accessNetworkTechnology=LTE-CA") ||
                serviceState.contains("AdvanceMode1")) &&
                serviceState.contains("cellIdentity=CellIdentityLte"))

}