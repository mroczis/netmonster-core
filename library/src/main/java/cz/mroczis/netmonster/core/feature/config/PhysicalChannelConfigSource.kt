package cz.mroczis.netmonster.core.feature.config

import android.os.Build
import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.feature.config.PhysicalChannelConfigSource.PhysicalChannelListener
import cz.mroczis.netmonster.core.model.config.PhysicalChannelConfig
import cz.mroczis.netmonster.core.util.PhoneStateListenerPort

/**
 * Fetches [PhysicalChannelConfig] from [PhysicalChannelListener]. Those data are currently not publicly
 * accessible however, some phones fill them (Pixel 3 XL, OnePlus 6T, Xiaomi Mi 9T).
 *
 * Data are obtained from system on each request, no caching is involved here hence it might take a
 * few milliseconds.
 */
class PhysicalChannelConfigSource {

    companion object {
        // Copied from SDK, this field is currently hidden
        const val LISTEN_PHYSICAL_CHANNEL_CONFIGURATION = 0x00100000
    }

    /**
     * Registers [PhysicalChannelListener] and awaits for data. After 500 milliseconds time outs if
     * nothing is delivered.
     */
    fun get(telephonyManager: TelephonyManager, subId: Int): List<PhysicalChannelConfig> =
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.P..Build.VERSION_CODES.Q) {
            telephonyManager.requestSingleUpdate<List<PhysicalChannelConfig>>(LISTEN_PHYSICAL_CHANNEL_CONFIGURATION) { onData ->
                PhysicalChannelListener(subId, onData)
            } ?: emptyList()
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