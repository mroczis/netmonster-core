package cz.mroczis.netmonster.core.model.band

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.Q)
data class BandNr(
    val downlinkArfcn: Int,
    /**
     * Downlink frequency in kHz calculated from [downlinkArfcn]
     *
     * Unit: kHz
     */
    val downlinkFrequency: Int,
    override val number: Int?,
    override val name: String?
) : IBand {
    override val channelNumber: Int = downlinkArfcn


    companion object {

        /**
         * 0 would represent 0 kHz, people can hear up to 20 kHz. If we assume N_{ref} = 20 and delta F_{Raster} = 100
         * we can set lower bound to 1 MHz ~ 200.
         *
         * However [3GPP 38.101-1 specification for NR](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=3283)
         * (chapter 5.4.2.1 NR-ARFCN and channel raster) defaults to 0 as minimal ARFCN possible.
         */
        const val DOWNLINK_ARFCN_MIN = 200L

        /**
         * Source: [3GPP 38.101-1 specification for NR](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=3283)
         * 5.4.2.1 NR-ARFCN and channel raster
         */
        const val DOWNLINK_ARFCN_MAX = 2_016_666L


        internal val DOWNLINK_EARFCN_RANGE = DOWNLINK_ARFCN_MIN..DOWNLINK_ARFCN_MAX
    }
}