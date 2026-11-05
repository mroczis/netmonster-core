package cz.mroczis.netmonster.core.subscription.mapper

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.ServiceState
import android.telephony.SubscriptionInfo
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork.RegistrationState

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

/**
 * Maps ServiceState.STATE_* to [RegistrationState]
 */
internal fun mapRegistrationState(state: Int): RegistrationState = when (state) {
    ServiceState.STATE_IN_SERVICE -> RegistrationState.IN_SERVICE
    ServiceState.STATE_OUT_OF_SERVICE -> RegistrationState.OUT_OF_SERVICE
    ServiceState.STATE_EMERGENCY_ONLY -> RegistrationState.EMERGENCY_ONLY
    else -> RegistrationState.POWER_OFF
}
