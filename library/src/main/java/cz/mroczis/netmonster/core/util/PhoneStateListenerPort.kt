package cz.mroczis.netmonster.core.util

import android.os.Build
import android.telephony.PhoneStateListener
import java.util.concurrent.Executor

/**
 * Phone state listener whose subscription id might be modified so we can listen
 * for Dual SIM's data.
 *
 * Does not work well for Samsung phones -> random data are reported so this function is blocked for them.
 */
open class PhoneStateListenerPort : PhoneStateListener {

    constructor(executor: Executor, subId: Int?) : super(executor) {
        init(subId)
    }

    constructor(subId: Int?) : super() {
        init(subId)
    }

    private fun init(subId: Int?) {
        if (subId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            !Build.MANUFACTURER.equals("samsung", ignoreCase = true)
        ) {
            try {
                PhoneStateListener::class.java.getDeclaredField("mSubId").apply {
                    isAccessible = true
                    set(this, subId)
                }
            } catch (ignored: Exception) {
                // When it does not work, it does not work...
            }
        }
    }


}