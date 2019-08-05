package cz.mroczis.netmonster.core.model.band

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.N)
data class BandWcdma(
    /**
     * 16-bit Downlink UMTS Absolute RF Channel Number
     *
     * Unit: None
     */
    @IntRange(from = DOWNLINK_UARFCN_MIN, to = DOWNLINK_UARFCN_MAX)
    val downlinkUarfcn: Int,

    override val number: Int?,
    override val name: String?
) : IBand {
    override val channelNumber: Int = downlinkUarfcn

    companion object {

        /**
         * Minimal UARFCN
         */
        const val DOWNLINK_UARFCN_MIN = 0L

        /**
         * UARFCN is 14-bit number. This value represents 2^14 - 1
         */
        const val DOWNLINK_UARFCN_MAX = 16_383L

        internal val DOWNLINK_UARFCN_RANGE = DOWNLINK_UARFCN_MIN..DOWNLINK_UARFCN_MAX
    }
}