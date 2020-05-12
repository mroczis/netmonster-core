package cz.mroczis.netmonster.core.feature.config

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.feature.config.SignalStrengthsSource.SignalStrengthsListener
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Attempts to fetch fresh [SignalStrength] using [SignalStrengthsListener]. If this
 * approach fails then looks to cache in [TelephonyManager].
 *
 * Cache is available since [Build.VERSION_CODES.P].
 */
class SignalStrengthsSource {

    companion object {

        /**
         * Async executor so can await data from [SignalStrengthsListener]
         */
        private val asyncExecutor by lazy {
            val thread = HandlerThread("SignalStrengthsSource").apply {
                start()
            }
            Handler(thread.looper)
        }
    }

    /**
     * Registers [SignalStrengthsListener] and awaits data. After 100 milliseconds time outs if
     * nothing is delivered.
     *
     * On Android O and newer directly grabs [ServiceState] from [TelephonyManager].
     */
    fun get(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? =
        getFresh(telephonyManager, subId) ?: getCached(telephonyManager)

    private fun getFresh(telephonyManager: TelephonyManager, subId: Int?): SignalStrength? {
        var listener: SignalStrengthsListener? = null
        val asyncLock = CountDownLatch(1)
        var signal: SignalStrength? = null

        asyncExecutor.post {
            // We'll receive callbacks on thread that created instance of [listener] by default.
            // Async processing is required otherwise deadlock would arise cause we block
            // original thread
            listener = SignalStrengthsListener(subId) {
                signal = it
                asyncLock.countDown()
            }

            telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }

        // And we also must block original thread
        // It'll get unblocked once we receive required data
        // This usually takes +/- 20 ms to complete
        try {
            asyncLock.await(100, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            // System was not able to deliver PhysicalChannelConfig in this time slot
        }

        listener?.let { telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE) }

        return signal
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