package cz.mroczis.netmonster.core.model.signal

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.util.minOrNotnull

data class SignalGsm(
    /**
     * Received signal strength indication
     *
     * Unit: dBm
     */
    @IntRange(from = RSSI_MIN, to = RSSI_MAX)
    val rssi: Int?,

    /**
     * Bit error rate
     * Zero is best here
     *
     * Unit: None
     */
    @IntRange(from = BIT_ERROR_RATE_MIN, to = BIT_ERROR_RATE_MAX)
    val bitErrorRate: Int?,

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
    @IntRange(from = 0, to = 31)
    val asu: Int? = rssi?.plus(113)?.div(2)

    /**
     * Calculates approximate distance to [cz.mroczis.netmonster.core.model.cell.CellGsm]
     * which is assigned to this object.
     */
    fun getDistanceToCell() = timingAdvance?.times(ONE_TA_IN_METERS)

    /**
     * Merges current instance with [other], keeping data that are valid and adding
     * other values that are valid in [other] instance but not here.
     *
     * For [rssi] we pick smaller value since Samsung devices used to return -51 dBm as
     * invalid RSSI.
     */
    fun merge(other: SignalGsm) = copy(
        rssi = minOrNotnull(rssi, other.rssi),
        bitErrorRate = bitErrorRate ?: other.bitErrorRate,
        timingAdvance = timingAdvance ?: other.timingAdvance
    )

    companion object {
        /**
         * 1 TA equals to 554 meters in GSM world
         * More at: [source](https://people.csail.mit.edu/bkph/cellular_repeater_TA.shtml)
         */
        internal const val ONE_TA_IN_METERS = 554

        /**
         * Limit is usually -51, Huawei CAM-L21 works up to -40
         */
        const val RSSI_MAX = -40L
        const val RSSI_MIN = -113L

        const val BIT_ERROR_RATE_MAX = 7L
        const val BIT_ERROR_RATE_MIN = 0L

        const val TIMING_ADVANCE_MAX = 219L
        const val TIMING_ADVANCE_MIN = 0L

        internal val RSSI_RANGE = RSSI_MIN..RSSI_MAX
        internal val BIT_ERROR_RATE_RANGE = BIT_ERROR_RATE_MIN..BIT_ERROR_RATE_MAX
        internal val TIMING_ADVANCE_RANGE = TIMING_ADVANCE_MIN..TIMING_ADVANCE_MAX

        internal val EMPTY = SignalGsm(null, null, null)
    }
}