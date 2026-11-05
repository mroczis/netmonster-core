package cz.mroczis.netmonster.core.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.NetworkRegistrationInfo
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.SubscriptionId
import cz.mroczis.netmonster.core.feature.config.DisplayInfoSource
import cz.mroczis.netmonster.core.model.DisplayInfo

@TargetApi(Build.VERSION_CODES.R)
internal open class TelephonyManagerCompat30(
    context: Context,
    subId: SubscriptionId = Int.MAX_VALUE
) : TelephonyManagerCompat29(context, subId) {

    private val displayInfoSource = DisplayInfoSource()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE])
    override fun getDisplayInfo(): DisplayInfo =
        displayInfoSource.get(telephony, subId)

    /**
     * Cellular registrations of [getServiceState] tell the radio even when data goes over Wi-Fi - the registered one
     * first, data before voice. Out of service the technology of the cell the modem is camped on or searching is there.
     */
    @SuppressLint("MissingPermission")
    override fun getCellularNetworkType(): Int? =
        getServiceState()?.networkRegistrationInfoList
            ?.filter { it.transportType == AccessNetworkConstants.TRANSPORT_TYPE_WWAN && it.accessNetworkTechnology.isCellular() }
            ?.sortedWith(
                compareByDescending<NetworkRegistrationInfo> { it.isRegistered }
                    .thenByDescending { it.domain and NetworkRegistrationInfo.DOMAIN_PS != 0 }
            )
            ?.firstOrNull()
            ?.accessNetworkTechnology
            ?: super.getCellularNetworkType()
}
