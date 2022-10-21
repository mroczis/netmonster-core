package cz.mroczis.netmonster.core.feature.config

import android.annotation.TargetApi
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import cz.mroczis.netmonster.core.cache.TelephonyCache
import cz.mroczis.netmonster.core.util.SingleEventPhoneStateListener

/**
 * Attempts to fetch fresh [SignalStrength] using signal strength listener. If this
 * approach fails then looks to cache in [TelephonyManager].
 *
 * Cache is available since [Build.VERSION_CODES.P].
 */
class SignalStrengthsSource {

    /**
     * Registers a signal strength listener and awaits data. After 1000 milliseconds time outs if
     * nothing is delivered.
     *
     * On Android O and newer directly grabs [ServiceState] from [TelephonyManager].
     */
    fun get(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? =
        getFresh(telephonyManager, subId) ?: getCached(telephonyManager)

    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? =
        TelephonyCache.getOrUpdate(subId, TelephonyCache.Event.SIGNAL_STRENGTHS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.requestSingleUpdate { signalStrengthListener(it) }
            } else {
                telephonyManager.requestPhoneStateUpdate { signalStrengthListener(subId, it) }
            }
        }

    /**
     * Since Android P we can ask [TelephonyManager] directly
     */
    private fun getCached(telephonyManager: TelephonyManager) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephonyManager.signalStrength
        } else {
            null
        }

    /**
     * Kotlin friendly PhoneStateListener that grabs [SignalStrength]
     */
    @TargetApi(Build.VERSION_CODES.R)
    private fun signalStrengthListener(
        subId: Int?,
        onChanged: UpdateResult<SingleEventPhoneStateListener, SignalStrength>
    ) = object : SingleEventPhoneStateListener(LISTEN_SIGNAL_STRENGTHS, subId) {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            if (signalStrength != null) {
                onChanged.invoke(this, signalStrength)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun signalStrengthListener(onChanged: UpdateResult<TelephonyCallback, SignalStrength>) =
        object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                onChanged(this, signalStrength)
            }
        }
}



