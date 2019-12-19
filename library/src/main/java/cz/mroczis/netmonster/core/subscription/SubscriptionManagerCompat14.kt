package cz.mroczis.netmonster.core.subscription

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission

internal open class SubscriptionManagerCompat14(
    internal val context: Context
) : ISubscriptionManagerCompat {

    /**
     * No dual SIM support in Android SDK prior Lollipop 5.1
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun getActiveSubscriptionIds(): List<Int> = mutableListOf<Int>().apply {
        add(Integer.MAX_VALUE)
    }

}