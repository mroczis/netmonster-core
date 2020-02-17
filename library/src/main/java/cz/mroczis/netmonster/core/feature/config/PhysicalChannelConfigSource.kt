package cz.mroczis.netmonster.core.feature.config

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Fetches [PhysicalChannelConfig] from [PhysicalChannelListener]. Those data are currently not publicly
 * accessible however, some phones fill them (Pixel 3 XL, OnePlus 6T, Xiaomi Mi 9T).
 *
 * Data are obtained from system on each request, no caching is invlive here hence it might take a
 * few milliseconds.
 */
class PhysicalChannelConfigSource {

    companion object {
        // Copied from SDK, this field is currently hidden
        const val LISTEN_PHYSICAL_CHANNEL_CONFIGURATION = 0x00100000

        /**
         * Async executor so can await data from [PhysicalChannelListener]
         */
        private val asyncExecutor by lazy {
            val thread = HandlerThread("PhysicalChannelConfigSource").apply {
                start()
            }
            Handler(thread.looper)
        }
    }

    /**
     * Registers [PhysicalChannelListener] and awaits for data. After 500 milliseconds time outs if
     * nothing is delivered.
     */
    fun get(telephonyManager: TelephonyManager, subId: Int): List<PhysicalChannelConfig> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var config: List<PhysicalChannelConfig>? = null
            val asyncLock = CountDownLatch(1)

            asyncExecutor.post {
                // We'll receive callbacks on thread that created instance of [listener] by default.
                // Async processing is required otherwise deadlock would arise cause we block
                // original thread
                val listener = PhysicalChannelListener(subId) {
                    config = it
                    asyncLock.countDown()
                    telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
                }

                telephonyManager.listen(listener, LISTEN_PHYSICAL_CHANNEL_CONFIGURATION)
            }

            // And we also must block original thread
            // It'll get unblocked once we receive required data
            // This usually takes +/- 20 ms to complete
            try {
                asyncLock.await(500, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                // System was not able to deliver PhysicalChannelConfig in this time slot
            }

            config ?: emptyList()
        } else {
            emptyList()
        }


    /**
     * Kotlin friendly PhoneStateListener
     */
    private class PhysicalChannelListener(
        subId: Int?,
        private val physicalChannelCallback: PhysicalChannelListener.(config: List<PhysicalChannelConfig>) -> Unit
    ) : PhoneStateListenerPort(subId) {

        override fun onPhysicalChannelConfigurationChanged(configs: List<Any?>) {
            val mapped = configs
                .mapNotNull { it.toString() }
                .map { PhysicalChannelConfig.fromString(it) }

            physicalChannelCallback.invoke(this, mapped)
        }
    }
}