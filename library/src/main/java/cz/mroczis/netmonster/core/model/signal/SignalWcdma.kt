package cz.mroczis.netmonster.core.model.signal

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk


data class SignalWcdma(
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
    @SinceSdk(Build.VERSION_CODES.Q)
    @IntRange(from = BIT_ERROR_RATE_MIN, to = BIT_ERROR_RATE_MAX)
    val bitErrorRate: Int?,

    /**
     * Energy per Bit to Noise Power Density Ratio
     *
     * Unit: dB
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    @IntRange(from = ECNO_MIN, to = ECNO_MAX)
    val ecno: Int?,

    /**
     * Received Signal Code Power
     *
     * Unit: dBm
     */
    @SinceSdk(Build.VERSION_CODES.Q)
    @IntRange(from = RSCP_MIN, to = RSCP_MAX)
    val rscp: Int?,

    /**
     * Energy per Chip to Interference Power Ratio
     * Huawei exclusive feature.
     *
     * Unit: dB
     */
    @SinceSdk(Build.VERSION_CODES.M)
    @IntRange(from = ECNO_MIN, to = ECIO_MAX)
    val ecio: Int?
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
    fun merge(other : SignalWcdma) =
        copy(
            rssi = rssi ?: other.rssi,
            bitErrorRate = bitErrorRate ?: other.bitErrorRate,
            ecno = ecno ?: other.ecno,
            rscp = rscp ?: other.rscp,
            ecio = ecio ?: other.ecio
        )

    companion object {
        const val RSSI_MAX = -51L
        const val RSSI_MIN = -113L

        const val BIT_ERROR_RATE_MAX = 7L
        const val BIT_ERROR_RATE_MIN = 0L

        const val ECNO_MAX = 1L
        const val ECNO_MIN = -23L

        const val RSCP_MAX = -25L
        const val RSCP_MIN = -119L

        const val ECIO_MAX = 0L
        const val ECIO_MIN = -20L

        internal val RSSI_RANGE = RSSI_MIN..RSSI_MAX
        internal val BIT_ERROR_RATE_RANGE = BIT_ERROR_RATE_MIN..BIT_ERROR_RATE_MAX
        internal val ECNO_RANGE = ECNO_MIN..ECNO_MAX
        internal val RSCP_RANGE = RSCP_MIN..RSCP_MAX
        internal val ECIO_RANGE = ECIO_MIN..ECIO_MAX
    }

}