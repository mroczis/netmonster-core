package cz.mroczis.netmonster.core.model.cell

import android.os.Build
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.BandTdscdma
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.CID_MAX
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.CID_MIN
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.CPID_MAX
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.CPID_MIN
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.LAC_MAX
import cz.mroczis.netmonster.core.model.cell.CellTdscdma.Companion.LAC_MIN
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalTdscdma

@SinceSdk(Build.VERSION_CODES.Q)
data class CellTdscdma(
    override val network: Network?,

    /**
     * 28-bit WCDMA Cell Identity
     * in range from [CID_MIN] to [CID_MAX], null if unavailable
     */
    val ci: Int?,

    /**
     * 16-bit Location Area Code
     * in range from [LAC_MIN] to [LAC_MAX], null if unavailable
     */
    val lac: Int?,

    /**
     * 8-bit Cell Parameters ID
     * in range from [CPID_MIN] to [CPID_MAX], null if unavailable
     */
    val cpid: Int?,

    @SinceSdk(Build.VERSION_CODES.N)
    override val band: BandTdscdma?,

    override val signal: SignalTdscdma,
    override val connectionStatus: IConnection,
    override val subscriptionId: Int,
    override val timestamp: Milliseconds?,
) : ICell {

    /**
     * 16-bit WCDMA Cell Identifier extracted from [ci]
     * in range from 0 to 65535, null if unavailable
     */
    val cid: Int?
        get() = ci?.and(0xFFFF)

    /**
     * 12-bit WCDMA Radio Network Controller extracted from [ci]
     * in range from 0 to 4095, null if unavailable
     */
    val rnc: Int?
        get() = ci?.shr(16)

    /**
     * 15-decimal digit code that contains MCC-MNC-LAC-CID
     */
    val cgi: String?
        get() = if (network != null && lac != null && ci != null) {
            "${network.toPlmn()}${lac.toString().padStart(5, '0')}${cid.toString().padStart(5, '0')}"
        } else null

    override fun <T> let(processor: ICellProcessor<T>): T = processor.processTdscdma(this)

    companion object {
        const val CID_MIN = 1L
        const val CID_MAX = 268_435_455L

        const val LAC_MIN = 1L
        const val LAC_MAX = 65_534L

        const val CPID_MIN = 0L
        const val CPID_MAX = 127L

        internal val CID_RANGE = CID_MIN..CID_MAX
        internal val LAC_RANGE = LAC_MIN..LAC_MAX
        internal val CPID_RANGE = CPID_MIN..CPID_MAX
    }

}