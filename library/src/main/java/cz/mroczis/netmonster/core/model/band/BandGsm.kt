package cz.mroczis.netmonster.core.model.band

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.N)
data class BandGsm(
    val arfcn: Int,
    override val name: String?,
    override val number: Int?
) : IBand {
    override val channelNumber: Int = arfcn

    companion object {
        const val ARFCN_MIN = 0
        const val ARFCN_MAX = 1023

        internal val ARFCN_RANGE = ARFCN_MIN..ARFCN_MAX
    }
}