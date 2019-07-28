package cz.mroczis.netmonster.core.model.cell

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.BandGsm
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalGsm

data class CellGsm(
    /**
     * Current network operator or null if unknown
     */
    val network: Network?,

    /**
     * 16-bit GSM Cell Identity
     * in range from [CID_MIN] to [CID_MAX], null if unavailable
     */
    @IntRange(from = CID_MIN, to = CID_MAX)
    val cid: Int,

    /**
     * 16-bit Location Area Code
     * in range from [LAC_MIN] to [LAC_MAX], null if unavailable
     */
    @IntRange(from = LAC_MIN, to = LAC_MAX)
    val lac: Int,

    /**
     * 6-bit Base Station Identity Code
     * in range from [BSIC_MIN] to [BSIC_MAX].
     *
     * BSIC has always 2 digits (in decimal system)
     *  - right digit is [ncc]
     *  - left digit is [bcc]
     */
    @SinceSdk(Build.VERSION_CODES.N)
    @IntRange(from = BSIC_MIN, to = BSIC_MAX)
    val bsic: Int?,

    @SinceSdk(Build.VERSION_CODES.N)
    override val band: BandGsm?,

    override val signal: SignalGsm,
    override val connectionStatus: IConnection
) : ICell {

    /**
     * 4-bit Network Color Code
     * in range from 0 to 9
     */
    val ncc
        get() = bsic?.div(10)

    /**
     * 4-bit Base Station Identity Code
     * in range from 0 to 9
     */
    val bcc
        get() = bsic?.rem(10)

    companion object {

        const val CID_MIN = 1L
        const val CID_MAX = 65534L

        const val LAC_MIN = 1L
        const val LAC_MAX = 65534L

        const val BSIC_MIN = 0L
        const val BSIC_MAX = 63L

        internal val CID_RANGE = CID_MIN..CID_MAX
        internal val LAC_RANGE = LAC_MIN..LAC_MAX
        internal val BSIC_RANGE = BSIC_MIN..BSIC_MAX
    }

}