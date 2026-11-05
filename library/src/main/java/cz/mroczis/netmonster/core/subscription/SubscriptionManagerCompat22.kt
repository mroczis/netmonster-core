package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.ServiceState
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
import cz.mroczis.netmonster.core.subscription.mapper.mapNetwork
import cz.mroczis.netmonster.core.subscription.mapper.mapRegistrationState
import cz.mroczis.netmonster.core.util.registrationState

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
internal open class SubscriptionManagerCompat22(
    context: Context
) : SubscriptionManagerCompat14(context) {

    private val manager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getActiveSubscriptionIds(): List<Int> = getActiveSubscriptions().map {
        it.subscriptionId
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getActiveSubscriptions(): List<SubscribedNetwork> {
        val subscriptions = manager.activeSubscriptionInfoList
            ?.sortedBy { it.simSlotIndex }
            ?.mapNotNull {
                val id = it.subscriptionId

                // Phones report deactivated SIMs in list of active subscriptions
                // Explicit check if SIM is ready is required in order to not show obsolete data
                val telephony = NetMonsterFactory.getTelephony(context, id)
                val simServiceState = telephony.getServiceState()?.registrationState ?: ServiceState.STATE_POWER_OFF
                if (simServiceState != ServiceState.STATE_POWER_OFF && (simServiceState != ServiceState.STATE_OUT_OF_SERVICE || OUT_OF_SERVICE_SUPPORTED)) {
                    SubscribedNetwork(
                        simSlotIndex = it.simSlotIndex,
                        subscriptionId = it.subscriptionId,
                        network = it.mapNetwork(),
                        registrationState = mapRegistrationState(simServiceState),
                    )
                } else {
                    null
                }
            }

        return if (subscriptions.isNullOrEmpty()) {
            super.getActiveSubscriptions()
        } else {
            subscriptions
        }
    }

    companion object {

        /**
         * Out of service SIMs are active since Android 10 - cells are requested fresh from the modem
         * (TelephonyManager.requestCellInfoUpdate) and only the fresh ones are taken, see NetMonster.getCells.
         * Older devices tend to report the last cells they had, not updated anymore, out of service.
         */
        private val OUT_OF_SERVICE_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
