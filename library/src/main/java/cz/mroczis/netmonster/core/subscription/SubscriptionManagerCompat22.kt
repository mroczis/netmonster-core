package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
internal open class SubscriptionManagerCompat22(
    context: Context
) : SubscriptionManagerCompat14(context) {

    private val manager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun getActiveSubscriptionIds(): List<Int> =
        manager.activeSubscriptionInfoList?.map { it.subscriptionId } ?: emptyList()

}