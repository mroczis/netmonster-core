package cz.mroczis.netmonster.core.model.band

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.db.BandTableLte
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.N)
data class BandLte(
    /**
     * 18-bit Absolute RF Channel Number
     *
     * Unit: None
     */
    @IntRange(from = DOWNLINK_EARFCN_MIN, to = DOWNLINK_EARFCN_MAX)
    val downlinkEarfcn: Int,

    /**
     * Bandwidth in kHz or null if unavailable
     *
     * Unit: kHz
     */
    @SinceSdk(Build.VERSION_CODES.P)
    @IntRange(from = BANDWIDTH_MIN, to = BANDWIDTH_MAX)
    val bandwidth: Int?,

    override val number: Int?,
    override val name: String?
) : IBand {
    override val channelNumber: Int = downlinkEarfcn

    companion object {

        /**
         * Smallest possible bandwidth for LTE - 1.4 MHz
         */
        const val BANDWIDTH_MIN = 1_400L
        const val BANDWIDTH_MAX = 100_000L

        /**
         * @see BandTableLte.DOWNLINK_MIN
         */
        const val DOWNLINK_EARFCN_MIN = BandTableLte.DOWNLINK_MIN.toLong()

        /**
         * @see BandTableLte.DOWNLINK_MAX
         */
        const val DOWNLINK_EARFCN_MAX = BandTableLte.DOWNLINK_MAX.toLong()

        internal val DOWNLINK_EARFCN_RANGE = DOWNLINK_EARFCN_MIN..DOWNLINK_EARFCN_MAX
        internal val BANDWIDTH_RANGE = BANDWIDTH_MIN..BANDWIDTH_MAX
    }
}