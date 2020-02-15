package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.os.Build
import android.telephony.ServiceState
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.SubscribedNetwork
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
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getActiveSubscriptionIds() : List<Int>


    /**
     * Retrieves all unique ids of active subscriptions and PLMNs that are currently in use
     *
     * By active is meant that:
     *  - SIM is inserted and ready to be used
     *  - eSIM is activated
     *
     * Resulting list is never empty, in case if subscription is unknown or unsupported
     * on current Android version one [SubscribedNetwork] with [SubscribedNetwork.subscriptionId]
     * equal to [Int.MAX_VALUE] and [SubscribedNetwork.network] == null is the only value in the list.
     */
    @SinceSdk(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getActiveSubscriptions() : List<SubscribedNetwork>

}