package cz.mroczis.netmonster.core.telephony

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.SubscriptionId

@TargetApi(Build.VERSION_CODES.N)
internal open class TelephonyManagerCompat24(
    context: Context,
    subId: SubscriptionId = Int.MAX_VALUE
) : TelephonyManagerCompat17(context, subId) {

    /**
     * The voice network - [getNetworkType] is the data one, it can't tell the radio when data go over Wi-Fi
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun getCellularNetworkType(): Int? =
        telephony.voiceNetworkType.takeIf { it.isCellular() }
}
