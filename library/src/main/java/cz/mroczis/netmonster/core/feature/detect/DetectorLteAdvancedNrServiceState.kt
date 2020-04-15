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
 * Attempts to detect LTE Advanced / LTE Carrier aggregation and NR in NSA mode
 *
 * Based on [ServiceState]'s contents added in Android O which describe if aggregation is currently active.
 */
class DetectorLteAdvancedNrServiceState : INetworkDetector {

    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE]
    )
    @SinceSdk(Build.VERSION_CODES.O)
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephony.getTelephonyManager()?.serviceState?.toString()?.let {
                val lteA = isUsingCarrierAggregation(it)
                val nr = is5gActive(it)
                when {
                    lteA && nr -> NetworkTypeTable.get(NetworkType.LTE_CA_NR)
                    nr -> NetworkTypeTable.get(NetworkType.LTE_NR)
                    lteA -> NetworkTypeTable.get(NetworkType.LTE_CA)
                    else -> null
                }
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
        ((serviceState.contains("IsUsingCarrierAggregation ?= ?true".toRegex()) ||
                serviceState.contains("accessNetworkTechnology=LTE-CA") ||
                serviceState.contains("AdvanceMode1")) &&
                serviceState.contains("cellIdentity=CellIdentityLte"))

    /**
     * AOSP documentation (Android 10):
     * The device is camped on an LTE cell that supports E-UTRA-NR Dual Connectivity(EN-DC) and
     * also connected to at least one 5G cell as a secondary serving cell.
     *
     * NR_STATE_CONNECTED / 3
     *
     * Android 10 and some Android P devices - nrStatus=CONNECTED
     * Huawei Android P - nsaState=5 - Tested on Mate 20X 5G
     * LG Android P - EnDc=true and 5G Allocated=true - Not tested on a real LG 5G device
     */
    @VisibleForTesting
    internal fun is5gActive(serviceState: String) =
        serviceState.contains("nrState=CONNECTED") ||
                serviceState.contains("nsaState=5") ||
                (serviceState.contains("EnDc=true") &&
                        serviceState.contains("5G Allocated=true"))
    
}