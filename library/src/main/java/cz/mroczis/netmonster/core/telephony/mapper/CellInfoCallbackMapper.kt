package cz.mroczis.netmonster.core.telephony.mapper

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.telephony.CellInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import cz.mroczis.netmonster.core.model.model.CellError

/**
 * Maps [TelephonyManager.CellInfoCallback] into our world.
 */
@TargetApi(Build.VERSION_CODES.Q)
class CellInfoCallbackMapper(
    private val success: (List<CellInfo>) -> Unit,
    private val error: ((CellError) -> Unit)? = null
) : TelephonyManager.CellInfoCallback() {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCellInfo(cells: MutableList<CellInfo>) {
        success.invoke(cells)
    }

    override fun onError(errorCode: Int, detail: Throwable?) {
        super.onError(errorCode, detail)
        error?.invoke(mapError(errorCode))
    }
    
    private fun mapError(errorCode: Int): CellError =
        when (errorCode) {
            ERROR_MODEM_ERROR -> CellError.MODEM_ERROR
            ERROR_TIMEOUT -> CellError.TIMEOUT
            else -> CellError.UNKNOWN
        }

}