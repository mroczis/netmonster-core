package cz.mroczis.netmonster.core.model.signal

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.Q)
data class SignalTdscdma(
    /**
     * Received Signal Strength Indicator
     *
     * Unit: dBm
     */
    @IntRange(from = RSSI_MIN, to = RSSI_MAX)
    val rssi: Int?,

    /**
     * Bit Error Rate
     * Zero is best here
     *
     * Unit: None
     */
    @IntRange(from = BIT_ERROR_RATE_MIN, to = BIT_ERROR_RATE_MAX)
    val bitErrorRate: Int?,

    /**
     * Received Signal Code Power
     *
     * Unit: dBm
     */
    @IntRange(from = RSCP_MIN, to = RSCP_MAX)
    val rscp: Int?
) : ISignal {

    override val dbm: Int?
        get() = rssi

    /**
     * Same as [rscp] just different unit.
     *
     * Unit: ASU
     */
    val rscpAsu = rscp?.plus(120)

    /**
     * Same as [rssi] just different unit.
     *
     * Unit: ASU
     */
    val rssiAsu = rssi?.plus(113)?.div(2)

    /**
     * Merges current instance with [other], keeping data that are valid and adding
     * other values that are valid in [other] instance but not here.
     */
    fun merge(other: SignalTdscdma) = copy(
        rssi = rssi ?: other.rssi,
        bitErrorRate = bitErrorRate ?: other.bitErrorRate,
        rscp = rscp ?: other.rscp
    )

    companion object {
        const val RSSI_MAX = -51L
        const val RSSI_MIN = -113L

        const val BIT_ERROR_RATE_MAX = 7L
        const val BIT_ERROR_RATE_MIN = 0L

        const val RSCP_MAX = -24L
        const val RSCP_MIN = -120L

        internal val RSSI_RANGE = RSSI_MIN..RSSI_MAX
        internal val BIT_ERROR_RATE_RANGE = BIT_ERROR_RATE_MIN..BIT_ERROR_RATE_MAX
        internal val RSCP_RANGE = RSCP_MIN..RSCP_MAX
    }

}