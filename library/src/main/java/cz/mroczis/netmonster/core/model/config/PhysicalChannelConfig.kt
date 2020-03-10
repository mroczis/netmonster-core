package cz.mroczis.netmonster.core.model.config

import android.os.Build
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.BandLte
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.connection.NoneConnection
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.model.connection.SecondaryConnection
import cz.mroczis.netmonster.core.util.inRangeOrNull

/**
 * This model class is port of AOSP's PhysicalChannelConfig which is currently
 * hidden from public. Its contents are obtained using [toString] method cause
 * reflection does not work properly in newest Android versions.
 *
 * [connectionStatus] and [bandwidth] were essential part of this model when it was added to AOSP.
 * [channelNumber] and [pci] were added in Android Q, expect always null in older Android versions.
 *
 * See also:
 * [AOSP's PhysicalChannelConfig](https://android.googlesource.com/platform/frameworks/base.git/+/master/telephony/java/android/telephony/PhysicalChannelConfig.java)
 */
@SinceSdk(Build.VERSION_CODES.P)
data class PhysicalChannelConfig(
    /**
     * Current connection status
     */
    val connectionStatus: IConnection,

    /**
     * Bandwidth in kHz
     */
    val bandwidth: Int?,

    /**
     * Downlink EARFCN for LTE
     * @see BandLte.downlinkEarfcn
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    val channelNumber: Int?,

    /**
     * Physical channel id
     * @see CellLte.pci
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    val pci: Int?
) {

    companion object {

        private val REGEX_CONNECTION = "mConnectionStatus=([^,]*)".toRegex()
        private val REGEX_BANDWIDTH = "mCellBandwidthDownlinkKhz=([0-9]{4,10})".toRegex()
        private val REGEX_CHANNEL = "mChannelNumber=([0-9]{1,10})".toRegex()
        private val REGEX_PCI = "mPhysicalCellId=([0-9]{1,10})".toRegex()

        /**
         * Parses data from a string.
         */
        fun fromString(string: String): PhysicalChannelConfig {

            val connection = when (REGEX_CONNECTION.find(string)?.groupValues?.getOrNull(1)) {
                "PrimaryServing" -> PrimaryConnection()
                "SecondaryServing" -> SecondaryConnection(isGuess = false)
                else -> NoneConnection()
            }

            val bandwidth = REGEX_BANDWIDTH.find(string)?.groupValues?.getOrNull(1)
                ?.toIntOrNull()?.inRangeOrNull(CellLte.BANDWIDTH_RANGE)
            val channel = REGEX_CHANNEL.find(string)?.groupValues?.getOrNull(1)
                ?.toIntOrNull()?.inRangeOrNull(BandLte.DOWNLINK_EARFCN_RANGE)
            val pci = REGEX_PCI.find(string)?.groupValues?.getOrNull(1)
                ?.toIntOrNull()?.inRangeOrNull(CellLte.PCI_RANGE)

            return PhysicalChannelConfig(
                connection, bandwidth, channel, pci
            )
        }

    }

}