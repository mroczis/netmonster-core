package cz.mroczis.netmonster.core.model.band

import android.os.Build
import cz.mroczis.netmonster.core.db.WcdmaBandTable
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.N)
data class BandWcdma(
    /**
     * 16-bit Downlink UMTS Absolute RF Channel Number
     *
     * Unit: None
     */
    val downlinkUarfcn: Int,

    override val number: Int?,
    override val name: String?
) : IBand {
    override val channelNumber: Int = downlinkUarfcn

    companion object {

        /**
         * @see WcdmaBandTable.DOWNLINK_MIN
         */
        const val DOWNLINK_UARFCN_MIN = WcdmaBandTable.DOWNLINK_MIN

        /**
         * @see WcdmaBandTable.DOWNLINK_MAX
         */
        const val DOWNLINK_UARFCN_MAX = WcdmaBandTable.DOWNLINK_MAX

        internal val DOWNLINK_UARFCN_RANGE = DOWNLINK_UARFCN_MIN..DOWNLINK_UARFCN_MAX
    }
}