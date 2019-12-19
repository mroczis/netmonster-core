package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

interface ISubscriptionManagerCompat {

    /**
     * Retries all unique ids of active subscriptions
     */
    @SinceSdk(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getActiveSubscriptionIds() : List<Int>

}