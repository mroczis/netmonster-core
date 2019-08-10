package cz.mroczis.netmonster.core.model.model

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

/**
 * Error as a result of cell information retrieval
 */
enum class CellError {
    /**
     * Operation timed out
     *
     * Copy of: [android.telephony.TelephonyManager.CellInfoCallback.ERROR_TIMEOUT]
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    TIMEOUT,

    /**
     * Modem returned a failure
     *
     * Copy of: [android.telephony.TelephonyManager.CellInfoCallback.ERROR_MODEM_ERROR]
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    MODEM_ERROR,

    /**
     * Minimal required version of OS is higher than current
     * on this terminal
     */
    UNSUPPORTED_AOSP_VERSION,

    /**
     * Error used when no other fits
     */
    UNKNOWN
}