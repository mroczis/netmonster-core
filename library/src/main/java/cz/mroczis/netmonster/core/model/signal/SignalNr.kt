package cz.mroczis.netmonster.core.model.signal

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

@SinceSdk(Build.VERSION_CODES.Q)
data class SignalNr(
    /**
     * Reference Signal Received Power for Channel State Information
     *
     * Unit: dBm
     */
    @IntRange(from = RSRP_MIN, to = RSRP_MAX)
    val csiRsrp: Int?,

    /**
     * Reference Signal Received Quality for Channel State Information
     *
     * Unit: dB
     */
    @IntRange(from = RSRQ_MIN, to = RSRQ_MAX)
    val csiRsrq: Int?,

    /**
     * Signal to Noise Ratio for Channel State Information
     *
     * Unit: None
     */
    @IntRange(from = SINR_MIN, to = SINR_MAX)
    val csiSinr: Int?,

    /**
     * Reference Signal Received Power for Synchronization Signal
     *
     * Unit: dBm
     */
    @IntRange(from = RSRP_MIN, to = RSRP_MAX)
    val ssRsrp: Int?,

    /**
     * Reference Signal Received Quality for Synchronization Signal
     *
     * Unit: dB
     */
    @IntRange(from = RSRQ_MIN, to = RSRQ_MAX)
    val ssRsrq: Int?,

    /**
     * Signal to Noise Ratio for Synchronization Signal
     *
     * Unit: None
     */
    @IntRange(from = SINR_MIN, to = SINR_MAX)
    val ssSinr: Int?
) : ISignal {

    internal constructor() : this(null, null, null, null, null, null)

    override val dbm: Int?
        get() = ssRsrp

    /**
     * Same as [csiRsrp] just different unit.
     *
     * Unit: ASU
     */
    val csiRsrpAsu
        get() = csiRsrp?.plus(140)

    /**
     * Same as [ssRsrp] just different unit.
     *
     * Unit: ASU
     */
    val ssRsrpAsu
        get() = ssRsrp?.plus(140)

    /**
     * Merges current instance with [other], keeping data that are valid and adding
     * other values that are valid in [other] instance but not here.
     */
    fun merge(other: SignalNr) = copy(
        csiRsrp = csiRsrp ?: other.csiRsrp,
        csiRsrq = csiRsrq ?: other.csiRsrq,
        csiSinr = csiSinr ?: other.csiSinr,
        ssRsrp = ssRsrp ?: other.ssRsrp,
        ssRsrq = ssRsrq ?: other.ssRsrq,
        ssSinr = ssSinr ?: other.ssSinr
    )

    companion object {

        const val RSRP_MAX = -44L
        const val RSRP_MIN = -140L

        const val RSRQ_MAX = -3L
        const val RSRQ_MIN = -20L

        const val SINR_MAX = 23L
        const val SINR_MIN = -23L

        internal val RSRP_RANGE = RSRP_MIN..RSRP_MAX
        internal val RSRQ_RANGE = RSRQ_MIN..RSRQ_MAX
        internal val SINR_RANGE = SINR_MIN..SINR_MAX

        internal val EMPTY = SignalNr(null, null, null, null, null, null)
    }
}