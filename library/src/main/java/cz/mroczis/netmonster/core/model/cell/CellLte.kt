package cz.mroczis.netmonster.core.model.cell

import android.os.Build
import androidx.annotation.IntRange
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.BandLte
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalLte

data class CellLte(
    override val network: Network?,

    /**
     * 28-bit E-UTRAN Cell Identifier
     * in range from [CID_MIN] to [CID_MAX], null if unavailable
     */
    val eci: Int?,

    /**
     * 16-bit Tracking Area Code
     * in range from [TAC_MIN] to [TAC_MAX], null if unavailable
     */
    val tac: Int?,

    /**
     * 9-bit Physical Cell Id
     * in range from [PCI_MIN] to [PCI_MAX], null if unavailable
     */
    @SinceSdk(Build.VERSION_CODES.JELLY_BEAN_MR1)
    val pci: Int?,

    @SinceSdk(Build.VERSION_CODES.N)
    override val band: BandLte?,

    /**
     * Bandwidth in kHz or null if unavailable
     *
     * Unit: kHz
     */
    @SinceSdk(Build.VERSION_CODES.P)
    @IntRange(from = BANDWIDTH_MIN, to = BANDWIDTH_MAX)
    val bandwidth: Int?,

    override val signal: SignalLte,
    override val connectionStatus: IConnection,
    override val subscriptionId: Int
) : ICell {

    /**
     * 20-bit LTE eNodeB extracted from [eci]
     * in range from 0 to 1 048 575, null if unavailable
     */
    val enb: Int?
        get() = eci?.shr(8)

    /**
     * 8-bit LTE Cell Identity extracted from [eci]
     * in range from 0 to 255, null if unavailable
     */
    val cid: Int?
        get() = eci?.and(0xFF)

    /**
     * 15-decimal digit code that contains MCC-MNC-ECI
     */
    val ecgi: String?
        get() = if (network != null && eci != null) {
            "${network.toPlmn()}${eci.toString().padStart(10, '0')}"
        } else null

    override fun <T> let(processor: ICellProcessor<T>): T = processor.processLte(this)

    companion object {

        /**
         * Correct min CID value is 0. Some Samsung phones use it as N/A value.
         */
        const val CID_MIN = 1L
        /**
         * Correct max CID is 268 435 455. MIUI phones use that value as N/A value.
         */
        const val CID_MAX = 268_435_454L

        /**
         * Correct min LAC value is 0. Some Samsung phones use it as N/A value.
         */
        const val TAC_MIN = 1L

        /**
         * Correct max LAC value is 65 535. Some terminals use it as N/A value.
         */
        const val TAC_MAX = 65_534L

        const val PCI_MIN = 0L
        const val PCI_MAX = 503L

        /**
         * Smallest possible bandwidth for LTE - 1.4 MHz
         */
        const val BANDWIDTH_MIN = 1_400L
        const val BANDWIDTH_MAX = 100_000L

        internal val CID_RANGE = CID_MIN..CID_MAX
        internal val TAC_RANGE = TAC_MIN..TAC_MAX
        internal val PCI_RANGE = PCI_MIN..PCI_MAX
        internal val BANDWIDTH_RANGE = BANDWIDTH_MIN..BANDWIDTH_MAX

    }

}