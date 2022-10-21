package cz.mroczis.netmonster.core.cache

import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.cache.TelephonyCache.LIFETIME
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Telephony cache helps us reduce calls to [TelephonyManager.listen] and speed up processing
 * Fetched results are stored for [LIFETIME] milliseconds.
 */
internal object TelephonyCache {

    private const val LIFETIME: Milliseconds = 1_000

    private val keys = ConcurrentLinkedQueue<Key>()
    private val cache = ConcurrentHashMap<Key, Value>()

    /**
     * Attempts to get stored value from a cache, if it is not present or it is already expired
     * invokes [update] to get fresh value and caches it.
     *
     * This method is safe to invoke from any thread.
     *
     * [subId] - subscription id
     * [event] - one of PhoneStateListener.LISTEN_* constants
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrUpdate(subId: Int?, event: Event, update: () -> T?): T? {
        // Make sure that there will be only one instance of a key
        val key = synchronized(this) {
            val modelKey = Key(subId = subId, event = event)
            keys.find { it == modelKey } ?: run {
                keys += modelKey
                modelKey
            }
        }

        // Try to get cached one without synchronised access
        val value = cache[key]?.takeIf { it.valid }
        if (value != null) {
            return value.any as? T
        }

        return synchronized(key) {
            // Let's try grab cached value once again since we are in a critical section
            val syncedValue = cache[key]?.takeIf { it.valid }
            if (syncedValue != null) {
                syncedValue.any as? T
            } else {
                cache.remove(key)
                update().let {
                    val newValue = Value(created = System.currentTimeMillis(), any = it)
                    cache[key] = newValue
                    it
                }
            }
        }
    }

    /**
     * Key here serves as a set of unique identifiers required to perform data update
     */
    private data class Key(
        val subId: Int?,
        val event: Event,
    )

    /**
     * Cached value, could be literally [any].
     */
    private data class Value(
        val created: Milliseconds,
        val any: Any?,
    ) {

        val valid
            get() = created + LIFETIME >= System.currentTimeMillis()

    }

    enum class Event {
        CELL_LOCATION,
        DISPLAY_INFO,
        PHYSICAL_CHANNEL,
        SERVICE_STATE,
        SIGNAL_STRENGTHS,
        ;
    }
}