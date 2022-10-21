package cz.mroczis.netmonster.core.feature.config

import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import cz.mroczis.netmonster.core.util.SingleEventPhoneStateListener
import cz.mroczis.netmonster.core.util.Threads
import java.util.concurrent.*

/**
 * Represents method that is invoked when new data are available.
 * In its parameters is instance of [PhoneStateListener] that is being used to listen
 * for data and current data.
 */
internal typealias UpdateResult<Listener, Model> = Listener.(data: Model?) -> Unit

/**
 * Creates a new instance of [PhoneStateListener] that will listen to homogeneous event.
 * Once update is available it must be delivered using [onSuccess] method to invoker.
 *
 * Model - represents output model
 * Invoker - represents instance of a class that will be handed to AOSP
 */
internal typealias CallbackCreator<Listener, Model> = (onSuccess: UpdateResult<Listener, Model>) -> Listener

/**
 * Requests *single* update from [TelephonyManager] blocking current thread until data are delivered
 * or timeouts after [timeout] ms returning null.
 *
 * Automatically registers and safely unregisters [PhoneStateListener] that is created using [getListener].
 * Make sure that instance of [PhoneStateListener] returned from [getListener] is unique per each call or cannot
 * be registered elsewhere.
 */
@AnyThread
@Deprecated("Use TelephonyCallback-based code if possible", ReplaceWith("requestSingleUpdate"))
internal fun <T> TelephonyManager.requestPhoneStateUpdate(
    timeout: Long = 1000,
    getListener: CallbackCreator<SingleEventPhoneStateListener, T>
): T? {
    val asyncLock = CountDownLatch(1)
    var listener: PhoneStateListener? = null
    var result: T? = null

    if (simState == TelephonyManager.SIM_STATE_ABSENT) {
        // When SIM is missing then all calls will timeout, so there's no need to even try
        return null
    }

    Threads.phoneStateListener.post {
        // We'll receive callbacks on thread that created instance of [listener] by default.
        // Async processing is required otherwise deadlock would arise cause we block
        // original thread
        val localListener = getListener { data ->
            // Stop listening
            listen(this, PhoneStateListener.LISTEN_NONE)
            // Rewrite reference to data and unblock original thread
            result = data
            asyncLock.countDown()
        }


        listener = localListener
        listen(localListener, localListener.event)
    }

    // And we also must block original thread
    // It'll get unblocked once we receive required data
    // This usually takes +/- 20 ms to complete
    try {
        asyncLock.await(timeout, TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
        // System was not able to deliver in this time slot
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

/**
 * Requests *single* update from [TelephonyManager] blocking current thread until data are delivered
 * or timeouts after [timeout] ms returning null.
 *
 * Automatically registers and safely unregisters [TelephonyCallback] that is created using [createCallback].
 * Make sure that instance of [TelephonyCallback] returned from [createCallback] is unique per each call or cannot
 * be registered elsewhere.
 */
@RequiresApi(Build.VERSION_CODES.S)
internal fun <T> TelephonyManager.requestSingleUpdate(
    timeout: Long = 1000,
    createCallback: CallbackCreator<TelephonyCallback, T>
) : T? {

    if (simState == TelephonyManager.SIM_STATE_ABSENT) {
        // When SIM is missing then all calls will timeout, so there's no need to even try
        return null
    }

    val asyncLock = CountDownLatch(1)
    var result: T? = null
    var registered = true

    val callback = createCallback { t: T? ->
        unregisterTelephonyCallback(this)
        registered = false
        result = t
        asyncLock.countDown()
    }

    registerTelephonyCallback(Threads.phoneStateListener::post, callback)

    // And we also must block original thread
    // It'll get unblocked once we receive required data
    // This usually takes +/- 20 ms to complete
    try {
        asyncLock.await(timeout, TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
        // System was not able to deliver PhysicalChannelConfig in this time slot
    }

    if (registered) {
        // Make sure that callback is unregistered
        unregisterTelephonyCallback(callback)
    }

    return result
}