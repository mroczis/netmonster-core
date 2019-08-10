package cz.mroczis.netmonster.core.util

import java.util.concurrent.Executor

/**
 * Executes [Runnable] directly on current thread.
 */
class DirectExecutor : Executor {
    override fun execute(runnable: Runnable) = runnable.run()
}