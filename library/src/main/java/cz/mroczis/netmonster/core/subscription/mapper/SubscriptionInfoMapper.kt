package cz.mroczis.netmonster.core.subscription.mapper

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.SubscriptionInfo
import cz.mroczis.netmonster.core.model.Network

/**
 * Grabs [Network] from [SubscriptionInfo]
 */
@SuppressLint("NewApi")
internal fun SubscriptionInfo.mapNetwork(): Network? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Network.map(mccString, mncString)
    } else {
        Network.map(mcc, mnc)
    }