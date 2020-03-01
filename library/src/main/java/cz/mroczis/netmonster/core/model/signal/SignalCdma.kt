package cz.mroczis.netmonster.core.model.signal

import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.DoubleRange

data class SignalCdma(
    /**
     * Received Signal Strength Indicator for CDMA
     *
     * Unit: dBm
     */
    @IntRange(from = RSSI_MIN, to = RSSI_MAX)
    val cdmaRssi: Int?,

    /**
     * Energy per Chip to Interference Power Ratio for CDMA
     *
     * Unit: dB
     */
    @DoubleRange(from = -16.0, to = 0.0)
    val cdmaEcio: Double?,

    /**
     * Received Signal Strength Indicator for EVDO
     *
     * Unit: dBm
     */
    @IntRange(from = RSSI_MIN, to = RSSI_MAX)
    val evdoRssi: Int?,

    /**
     * Energy per Chip to Interference Power Ratio for EVDO
     *
     * Unit: dB
     */
    @DoubleRange(from = -16.0, to = 0.0)
    val evdoEcio: Double?,

    /**
     * Signal to Noise Ratio
     *
     * Unit: None
     */
    @IntRange(from = SNR_MIN, to = SNR_MAX)
    val evdoSnr: Int?
) : ISignal {

    override val dbm: Int? = cdmaRssi ?: evdoRssi

    /**
     * Merges current instance with [other], keeping data that are valid and adding
     * other values that are valid in [other] instance but not here.
     */
    fun merge(other: SignalCdma) = copy(
        cdmaRssi = cdmaRssi ?: other.cdmaRssi,
        cdmaEcio = cdmaEcio ?: other.cdmaEcio,
        evdoRssi = evdoRssi ?: other.evdoRssi,
        evdoEcio = evdoEcio ?: other.evdoEcio,
        evdoSnr = evdoSnr ?: other.evdoSnr
    )

    companion object {

        const val RSSI_MIN = -120L
        const val RSSI_MAX = 0L

        const val ECIO_MIN = -160L
        const val ECIO_MAX = 0L

        const val SNR_MIN = 0L
        const val SNR_MAX = 8L

        internal val RSSI_RANGE = RSSI_MIN..RSSI_MAX
        internal val ECIO_RANGE = ECIO_MIN..ECIO_MAX
        internal val SNR_RANGE = SNR_MIN..SNR_MAX

    }

}