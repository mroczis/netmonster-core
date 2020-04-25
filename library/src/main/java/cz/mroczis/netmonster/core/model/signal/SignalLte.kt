package cz.mroczis.netmonster.core.model.signal

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.DoubleRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk

data class SignalLte(
    /**
     * Received Signal Strength Indicator
     *
     * Unit: dBm
     */
    @IntRange(from = RSSI_MIN, to = RSSI_MAX)
    val rssi: Int?,

    /**
     * Reference Signal Received Power
     *
     * Unit: dBm
     */
    @DoubleRange(from = RSRP_MIN, to = RSRP_MAX)
    val rsrp: Double?,

    /**
     * Reference Signal Received Quality
     *
     * Unit: dB
     */
    @DoubleRange(from = RSRQ_MIN, to = RSRQ_MAX)
    val rsrq: Double?,

    /**
     * Channel Quality Indicator
     *
     * Unit: None
     */
    @IntRange(from = CQI_MIN, to = CQI_MAX)
    val cqi: Int?,

    /**
     * Signal to Noise Ratio
     *
     * Unit: None
     */
    @DoubleRange(from = SNR_MIN, to = SNR_MAX)
    val snr: Double?,

    /**
     * Timing advance is normalized value that can tell you how far you are from signal source.
     * The bigger the value the farther you are.
     *
     * Unit: None
     */
    @SinceSdk(Build.VERSION_CODES.N)
    @IntRange(from = TIMING_ADVANCE_MIN, to = TIMING_ADVANCE_MAX)
    val timingAdvance: Int?
) : ISignal {

    override val dbm: Int?
        get() = rssi

    /**
     * Same as [rssi] just different unit.
     *
     * Unit: ASU
     */
    val rssiAsu
        get() = rssi?.plus(113)?.div(2)

    /**
     * Same as [rsrp] just different unit.
     *
     * Unit: ASU
     */
    val rsrpAsu
        get() = rsrp?.toInt()?.plus(140)

    /**
     * Calculates approximate distance to [cz.mroczis.netmonster.core.model.cell.CellLte]
     * which is assigned to this object.
     *
     * Before [Build.VERSION_CODES.O] [timingAdvance] was not publicly accessible for developers and
     * manufacturers did implement representation of TA it in two ways:
     *  - as one-way distance from source to terminal (78.07 meters per 1 TA),
     *  - as two-way distance from source back to source (156.14 meters per 1 TA).
     *
     * This brings ambiguous behaviour to calculation of distance from tower to cell and [oneTaInMeters] is required
     * as parameter of this method. You are advised to verify which implementation does terminal use.
     * In most cases [ONE_WAY_DISTANCE] is the correct one.
     *
     * @param oneTaInMeters how much is one TA in meters - use [ONE_WAY_DISTANCE] or [TWO_WAY_DISTANCE]
     * @return distance in meters or null if [timingAdvance] is null
     */
    fun getDistanceToCell(oneTaInMeters: Double): Double? =
        timingAdvance?.times(oneTaInMeters)

    /**
     * Merges current instance with [other], keeping data that are valid and adding
     * other values that are valid in [other] instance but not here.
     */
    fun merge(other: SignalLte) = copy(
        rssi = rssi ?: other.rssi,
        rsrp = rsrp ?: other.rsrp,
        rsrq = rsrq ?: other.rsrq,
        cqi = cqi ?: other.cqi,
        snr = snr ?: other.snr,
        timingAdvance = timingAdvance ?: other.timingAdvance
    )

    companion object {
        /**
         * One-way distance from source to terminal
         *
         * More at: [source](https://people.csail.mit.edu/bkph/cellular_repeater_TA.shtml)
         */
        const val ONE_WAY_DISTANCE = 78.12
        /**
         * Two-way distance from source to terminal
         *
         * More at: [source](https://people.csail.mit.edu/bkph/cellular_repeater_TA.shtml)
         */
        const val TWO_WAY_DISTANCE = 156.14

        const val RSSI_MAX = -51L
        const val RSSI_MIN = -113L

        const val RSRP_MAX = -40.0
        const val RSRP_MIN = -140.0

        const val RSRQ_MAX = -3.0
        const val RSRQ_MIN = -20.0

        const val CQI_MAX = 15L
        const val CQI_MIN = 1L

        const val SNR_MAX = 29.9
        const val SNR_MIN = -19.9

        const val TIMING_ADVANCE_MAX = 1282L
        const val TIMING_ADVANCE_MIN = 0L


        internal val RSSI_RANGE = RSSI_MIN..RSSI_MAX
        internal val RSRP_RANGE = RSRP_MIN..RSRP_MAX
        internal val RSRQ_RANGE = RSRQ_MIN..RSRQ_MAX
        internal val SNR_RANGE = SNR_MIN..SNR_MAX
        internal val CQI_RANGE = CQI_MIN..CQI_MAX
        internal val TIMING_ADVANCE_RANGE = TIMING_ADVANCE_MIN..TIMING_ADVANCE_MAX

    }
}