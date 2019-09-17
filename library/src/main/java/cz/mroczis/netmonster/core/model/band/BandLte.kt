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

    override val number: Int?,
    override val name: String?
) : IBand {
    override val channelNumber: Int = downlinkEarfcn

    companion object {

        /**
         * LTE min, default is 0.
         * Changed to 1 cause Samsung phones report 0 when data are incorrect.
         *
         * Source: [3GPP 36.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=2411),
         * 5.7.3 Carrier frequency and EARFCN
         */
        const val DOWNLINK_EARFCN_MIN = 1L

        /**
         * Source: [3GPP 36.101](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=2411),
         * 5.7.3 Carrier frequency and EARFCN
         */
        const val DOWNLINK_EARFCN_MAX = 262_143L

        internal val DOWNLINK_EARFCN_RANGE = DOWNLINK_EARFCN_MIN..DOWNLINK_EARFCN_MAX
    }
}