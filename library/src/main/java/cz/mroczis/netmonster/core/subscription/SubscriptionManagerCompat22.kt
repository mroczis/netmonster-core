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
                val simServiceState = telephony.getServiceState()?.state ?: ServiceState.STATE_POWER_OFF
                if (simServiceState != ServiceState.STATE_POWER_OFF && simServiceState != ServiceState.STATE_OUT_OF_SERVICE) {
                    SubscribedNetwork(it.simSlotIndex, it.subscriptionId, it.mapNetwork())
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
}