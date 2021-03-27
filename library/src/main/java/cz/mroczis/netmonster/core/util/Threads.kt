package cz.mroczis.netmonster.core.util

import android.os.Handler
import android.os.HandlerThread

/**
 * Threading stuff
 * NetMonster Core does not use any framework to deal with threading, only the good old Android way
 */
internal object Threads {

    /**
     * Thread / handler for PhoneStateListenerStuff
     */
    val phoneStateListener by lazy {
        val thread = HandlerThread("PhoneStateListener").apply {
            start()
        }
        
        Handler(thread.looper)
    }
    
}