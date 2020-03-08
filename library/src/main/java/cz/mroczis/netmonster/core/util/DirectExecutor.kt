package cz.mroczis.netmonster.core.util

import java.util.concurrent.Executor

/**
 * Executes [Runnable] directly on current thread.
 */
internal class DirectExecutor : Executor {
    override fun execute(runnable: Runnable) {
        try {
            runnable.run()
        } catch (e: NullPointerException) {
            // Bug in AOSP - TelephonyManager#requestCellInfoUpdate() throws NullPointerException
            // when an error during update occurs and native code does not pass an exception
            // -> Attempt to invoke virtual method 'java.lang.Throwable android.os.ParcelableException.getCause()' on a null object reference
        }
    }
}