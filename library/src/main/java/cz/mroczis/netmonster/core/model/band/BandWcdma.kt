package cz.mroczis.netmonster.core.model.band

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.db.BandTableWcdma
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
         * @see BandTableWcdma.DOWNLINK_MIN
         */
        const val DOWNLINK_UARFCN_MIN = BandTableWcdma.DOWNLINK_MIN.toLong()

        /**
         * @see BandTableWcdma.DOWNLINK_MAX
         */
        const val DOWNLINK_UARFCN_MAX = BandTableWcdma.DOWNLINK_MAX.toLong()

        internal val DOWNLINK_UARFCN_RANGE = DOWNLINK_UARFCN_MIN..DOWNLINK_UARFCN_MAX
    }
}