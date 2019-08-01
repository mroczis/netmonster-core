package cz.mroczis.netmonster.core.model.band

import android.os.Build
import cz.mroczis.netmonster.core.db.BandTableTdscdma
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.Q)
data class BandTdscdma(
    val downlinkUarfcn: Int,
    override val number: Int?,
    override val name: String?
) : IBand {

    override val channelNumber: Int = downlinkUarfcn

    companion object {

        /**
         * @see BandTableTdscdma.DOWNLINK_MIN
         */
        const val DOWNLINK_UARFCN_MIN = BandTableTdscdma.DOWNLINK_MIN.toLong()

        /**
         * @see BandTableTdscdma.DOWNLINK_MAX
         */
        const val DOWNLINK_UARFCN_MAX = BandTableTdscdma.DOWNLINK_MAX.toLong()

        internal val DOWNLINK_UARFCN_RANGE = DOWNLINK_UARFCN_MIN..DOWNLINK_UARFCN_MAX
    }
}