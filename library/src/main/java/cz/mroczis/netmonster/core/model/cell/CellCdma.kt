package cz.mroczis.netmonster.core.model.cell

import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.DoubleRange
import cz.mroczis.netmonster.core.model.band.IBand
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalCdma

data class CellCdma(
    override val network: Network? = null,
    /**
     * System Id
     * in range from [BID_MIN] to [BID_MAX], null if unavailable
     * CDMA's equivalent to MCC. One country has usually [sid] range assigned.
     *
     * More at: [List of SIDs](http://ifast.force.com/sidrange)
     */
    @IntRange(from = SID_MIN, to = SID_MAX)
    val sid: Int,

    /**
     * Network Identity
     * in range from [NID_MIN] to [NID_MAX], null if unavailable.
     * CDMA's equivalent to MNC.
     */
    @IntRange(from = NID_MIN, to = NID_MAX)
    val nid: Int?,

    /**
     * Base Station Id
     * in range from [BID_MIN] to [BID_MAX], null if unavailable
     * CDMA's equivalent to CID
     */
    @IntRange(from = BID_MIN, to = BID_MAX)
    val bid: Int?,

    /**
     * Latitude in rage from -90.0 to 90.0, null if unavailable
     */
    @DoubleRange(from = -90.0, to = 90.0)
    val lat: Double?,

    /**
     * Longitude in rage from -180.0 to 180.0, null if unavailable
     */
    @DoubleRange(from = -180.0, to = 180.0)
    val lon: Double?,

    override val signal: SignalCdma,
    override val connectionStatus: IConnection,
    override val subscriptionId: Int,
    override val timestamp: Milliseconds?,
) : ICell {

    /**
     * Band is not supported for CDMA
     */
    override val band: IBand? = null

    override fun <T> let(processor: ICellProcessor<T>): T = processor.processCdma(this)

    companion object {

        const val NID_MIN = 0L
        const val NID_MAX = 65535L

        /**
         * samsung SM-J737V - Invalid = 0
         */
        const val BID_MIN = 1L
        const val BID_MAX = 65535L

        /**
         * Min valid value is 0 but not used.
         * First valid one is 1 for the USA.
         */
        const val SID_MIN = 1L
        /**
         * Last valid value, Honduras
         */
        const val SID_MAX = 32767L

        const val LAT_MIN = -1296000
        const val LAT_MAX = +1296000

        const val LON_MIN = -2592000
        const val LON_MAX = +2592000

        internal val NID_RANGE = NID_MIN..NID_MAX
        internal val BID_RANGE = BID_MIN..BID_MAX
        internal val SID_RANGE = SID_MIN..SID_MAX
        internal val LAT_RANGE = LAT_MIN..LAT_MAX
        internal val LON_RANGE = LON_MIN..LON_MAX

    }

}