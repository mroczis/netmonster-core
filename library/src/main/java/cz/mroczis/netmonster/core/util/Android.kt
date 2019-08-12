package cz.mroczis.netmonster.core.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.view.Display

/**
 * Determines whether is display on
 *
 * @return true if display is on
 */
@Suppress("DEPRECATION")
fun Context.isDisplayOn(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        dm?.displays?.first { it.state == Display.STATE_ON } != null
    } else {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isScreenOn == true // deprecated but there is no other way to determine display state
    }
}