package cz.mroczis.netmonster.core.feature.config

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.AnyThread
import cz.mroczis.netmonster.core.util.Threads
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Represents method that is invoked when new data are available.
 * In its parameters is instance of [PhoneStateListener] that is being used to listen
 * for data and current data.
 */
internal typealias PhoneStateResult<T> = (listener: PhoneStateListener, data: T?) -> Unit

/**
 * Creates a new instance of [PhoneStateListener] that will listen to homogeneous event.
 * Once update is available it must be delivered using [onSuccess] method to invoker.
 *
 * Always invoked on worker thread
 */
internal typealias PhoneStateListenerCreator<T> = (onSuccess: PhoneStateResult<T>) -> PhoneStateListener

/**
 * Requests *single* update from [TelephonyManager] blocking current thread until data are delivered
 * or timeouts after [timeout] ms returning null.
 *
 * Automatically registers and safely unregisters [PhoneStateListener] that is created using [getListener].
 * Make sure that instance of [PhoneStateListener] returned from [getListener] is unique per each call or cannot
 * be registered elsewhere.
 *
 * @param event - [TelephonyManager]'s LISTEN_ constant to listen for
 */
@AnyThread
internal fun <T> TelephonyManager.requestSingleUpdate(
    event: Int,
    timeout: Long = 1000,
    getListener: PhoneStateListenerCreator<T>
): T? {
    val asyncLock = CountDownLatch(1)
    var listener: PhoneStateListener? = null
    var result: T? = null

    Threads.phoneStateListener.post {
        // We'll receive callbacks on thread that created instance of [listener] by default.
        // Async processing is required otherwise deadlock would arise cause we block
        // original thread
        val localListener = getListener { listener, data ->
            // Stop listening
            listen(listener, PhoneStateListener.LISTEN_NONE)
            // Rewrite reference to data and unblock original thread
            result = data
            asyncLock.countDown()
        }


        listener = localListener
        listen(localListener, event)
    }

    // And we also must block original thread
    // It'll get unblocked once we receive required data
    // This usually takes +/- 20 ms to complete
    try {
        asyncLock.await(timeout, TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
        // System was not able to deliver PhysicalChannelConfig in this time slot
    }

    // Make sure that listener is unregistered - required when it was created and registered
    // but no data were delivered
    listener?.let {
        // Same thread is required for unregistering
        Threads.phoneStateListener.post {
            listen(it, PhoneStateListener.LISTEN_NONE)
        }
    }

    return result
}