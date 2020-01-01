package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

interface ISubscriptionManagerCompat {

    /**
     * Retrieves all unique ids of active subscriptions.
     *
     * By active is meant that:
     *  - SIM is inserted and ready to be used
     *  - eSIM is activated
     *
     * Resulting list is never empty, in case if subscription id is unknown
     * or unsupported on current Android version [Int.MAX_VALUE] is the only
     * value in the list.
     */
    @SinceSdk(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getActiveSubscriptionIds() : List<Int>

}