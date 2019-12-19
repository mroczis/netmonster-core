@file:Suppress("DEPRECATION")

package cz.mroczis.netmonster.core.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.telephony.CellSignalStrengthLte
import android.telephony.NeighboringCellInfo
import android.telephony.SignalStrength
import android.telephony.gsm.GsmCellLocation
import android.view.Display

private val ASU_RANGE = 0..31

val NeighboringCellInfo.RSSI_ASU_RANGE
    get() = ASU_RANGE
val SignalStrength.RSSI_ASU_RANGE
    get() = ASU_RANGE
val CellSignalStrengthLte.RSSI_ASU_RANGE
    get() = ASU_RANGE
val GsmCellLocation.RSSI_ASU_RANGE
    get() = ASU_RANGE

/**
 * Determines whether is display on
 *
 * @return true if display is on
 */
@Suppress("DEPRECATION")
fun Context.isDisplayOn(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        dm?.displays?.firstOrNull { it.state == Display.STATE_ON } != null
    } else {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isScreenOn == true // deprecated but there is no other way to determine display state
    }
}

/**
 * Android documentation says that data are in ASU,
 * some devices do not agree at all.
 */
@Suppress("DEPRECATION")
fun SignalStrength.getGsmRssi() : Int? =
    if (gsmSignalStrength in (RSSI_ASU_RANGE)) {
        gsmSignalStrength.toDbm()
    } else gsmSignalStrength

/**
 * Android documentation says that data are in ASU,
 * some devices do not agree at all.
 */
@Suppress("DEPRECATION")
fun NeighboringCellInfo.getGsmRssi() : Int? =
    if (rssi in (RSSI_ASU_RANGE)) {
        rssi.toDbm()
    } else rssi

/**
 * Converts ASU to dBm
 */
fun Int.toDbm() = -113 + 2 * this

fun isHuawei() = Build.MANUFACTURER.equals("huawei", ignoreCase = true)
fun isSamsung() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)