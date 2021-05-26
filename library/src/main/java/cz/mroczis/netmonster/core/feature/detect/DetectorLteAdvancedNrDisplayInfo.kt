package cz.mroczis.netmonster.core.feature.detect

import android.Manifest
import android.os.Build
import android.telephony.TelephonyDisplayInfo
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.db.NetworkTypeTable
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.model.DisplayInfo
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.telephony.ITelephonyManagerCompat


/**
 * Attempts to detect LTE Advanced / LTE Carrier aggregation and NR in NSA mode
 *
 * Based on [TelephonyDisplayInfo]'s contents added in Android R.
 *
 * NOTE: This detector relies on [TelephonyDisplayInfo] which means that detected network
 * types might not be correct since [TelephonyDisplayInfo] returns data in accordance with carrier
 * policy and branding preferences.
 *
 * For more info refer to Android Reference.
 */
class DetectorLteAdvancedNrDisplayInfo : INetworkDetector {

    @SinceSdk(Build.VERSION_CODES.R)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE])
    override fun detect(netmonster: INetMonster, telephony: ITelephonyManagerCompat): NetworkType? =
        when (telephony.getDisplayInfo().overrideNetworkType) {
            DisplayInfo.NetworkOverrideType.LTE_CA -> {
                NetworkTypeTable.get(NetworkType.LTE_CA)
            }
            DisplayInfo.NetworkOverrideType.NR_ADVANCED, DisplayInfo.NetworkOverrideType.NR_NSA -> {
                NetworkTypeTable.get(NetworkType.LTE_NR)
            }
            else -> null
        }

}