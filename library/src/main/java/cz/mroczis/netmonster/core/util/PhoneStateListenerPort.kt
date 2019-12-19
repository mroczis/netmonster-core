package cz.mroczis.netmonster.core.util

import android.os.Build
import android.telephony.PhoneStateListener

/**
 * Phone state listener whose subscription id might be modified so we can listen
 * for Dual SIM's data.
 *
 * Does not work well for Samsung phones -> random data are reported so this function is blocked for them.
 */
open class PhoneStateListenerPort(subId: Int?) : PhoneStateListener() {

    init {
        init(subId)
    }

    private fun init(subId: Int?) {
        if (subId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isSamsung()) {
            try {
                PhoneStateListener::class.java.getDeclaredField("mSubId").apply {
                    isAccessible = true
                    set(this, subId)
                }
            } catch (ignored: Throwable) {
                // When it does not work, it does not work...
            }
        }
    }


    /**
     * This function exists in AOSP but it's hidden ^_^
     */
    open fun onPhysicalChannelConfigurationChanged(configs: List<Any?>) {

    }
}