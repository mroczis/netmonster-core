package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.factory.NetMonsterFactory

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
internal open class SubscriptionManagerCompat22(
    context: Context
) : SubscriptionManagerCompat14(context) {

    private val manager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun getActiveSubscriptionIds(): List<Int> {
        val subscriptions = manager.activeSubscriptionInfoList?.mapNotNull {
            val id = it.subscriptionId

            // Samsung phones report deactivated SIMs in list of active subscriptions
            // Explicit check if SIM is ready is required in order to not show obsolete data
            if (NetMonsterFactory.getTelephony(context, id).getTelephonyManager()?.simState == TelephonyManager.SIM_STATE_READY) {
                id
            } else {
                null
            }
        }

        return if (subscriptions.isNullOrEmpty()) {
            super.getActiveSubscriptionIds()
        } else {
            subscriptions
        }
    }

}