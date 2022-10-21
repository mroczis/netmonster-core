package cz.mroczis.netmonster.core.util

import android.os.Build
import android.telephony.PhoneStateListener

/**
 * Phone state listener whose subscription id might be modified so we can listen
 * for Dual SIM's data.
 */
open class PhoneStateListenerPort(subId: Int?) : PhoneStateListener() {

    init {
        init(subId)
    }

    private fun init(subId: Int?) {
        if (subId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                PhoneStateListener::class.java.getDeclaredField("mSubId").also {
                    it.isAccessible = true
                    it.set(this, subId)
                }
            } catch (ignored: Throwable) {
                // When it does not work, it does not work...
            }
        }
    }


    /**
     * This function exists in AOSP but it's hidden ^_^
     */
    @Deprecated("Removed in Android R, no replacement available")
    open fun onPhysicalChannelConfigurationChanged(configs: List<Any?>) {

    }
}

open class SingleEventPhoneStateListener(
    val event: Int,
    subId: Int?,
) : PhoneStateListenerPort(subId)