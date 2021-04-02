package cz.mroczis.netmonster.core.feature.config

import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.feature.config.SignalStrengthsSource.SignalStrengthsListener
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort

/**
 * Attempts to fetch fresh [SignalStrength] using [SignalStrengthsListener]. If this
 * approach fails then looks to cache in [TelephonyManager].
 *
 * Cache is available since [Build.VERSION_CODES.P].
 */
class SignalStrengthsSource {

    /**
     * Registers [SignalStrengthsListener] and awaits data. After 100 milliseconds time outs if
     * nothing is delivered.
     *
     * On Android O and newer directly grabs [ServiceState] from [TelephonyManager].
     */
    fun get(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? =
        getFresh(telephonyManager, subId) ?: getCached(telephonyManager)

    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? =
        telephonyManager.requestSingleUpdate<SignalStrength>(PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) { onData ->
            SignalStrengthsListener(subId, onData)
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
    private class SignalStrengthsListener(
        subId: Int?,
        private val simStateListener: SignalStrengthsListener.(state: SignalStrength) -> Unit
    ) : PhoneStateListenerPort(subId) {

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            if (signalStrength != null) {
                simStateListener.invoke(this, signalStrength)
            }
        }
    }
}