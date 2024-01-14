package cz.mroczis.netmonster.core.model.cell

import android.os.Build
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.model.Network
import cz.mroczis.netmonster.core.model.annotation.SinceSdk
import cz.mroczis.netmonster.core.model.band.BandNr
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.CID_MAX
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.CID_MIN
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.PCI_MAX
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.PCI_MIN
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.TAC_MAX
import cz.mroczis.netmonster.core.model.cell.CellNr.Companion.TAC_MIN
import cz.mroczis.netmonster.core.model.connection.IConnection
import cz.mroczis.netmonster.core.model.signal.SignalNr

@SinceSdk(Build.VERSION_CODES.Q)
data class CellNr(
    override val network: Network?,

    /**
     * 36-bit NR Cell Identity
     * in range from [CID_MIN] to [CID_MAX], null if unavailable
     */
    val nci: Long?,

    /**
     * 24-bit Tracking Area Code
     * in range from [TAC_MIN] to [TAC_MAX], null if unavailable
     */
    val tac: Int?,

    /**
     * 10-bit Physical Cell Id
     * in range from [PCI_MIN] to [PCI_MAX], null if unavailable
     */
    val pci: Int?,

    override val band: BandNr?,
    override val signal: SignalNr,
    override val connectionStatus: IConnection,
    override val subscriptionId: Int,
    override val timestamp: Milliseconds?,
) : ICell {

    /**
     * NCI can be split into two identifiers - gNodeB ID and cell local id. Depending of the network configuration
     * length of gNB can vary. This method will extract gNB from NCI using provided [gnbBitLength] which must
     * be in range from [GNB_LENGTH_MIN] to [GNB_LENGTH_MAX]. Length is currently not present in Android APIs and you
     * need to obtain it directly from the carrier or other sources.
     *
     * @see <a href ="https://issuetracker.google.com/issues/319966463">AOSP feature request</a>
     * @return gNodeB id (22 to 32 bits)
     */
    fun gnbId(gnbBitLength: Int) = nci?.let { nci ->
        if (gnbBitLength !in GNB_LENGTH_RANGE) return@let null
        val l = 36 - gnbBitLength
        nci shr l
    }

    /**
     * NCI can be split into two identifiers - gNodeB ID and cell local id. Depending of the network configuration
     * length of gNB can vary. This method will extract cell local id from NCI using provided [gnbBitLength] which must
     * be in range from [GNB_LENGTH_MIN] to [GNB_LENGTH_MAX]. Length is currently not present in Android APIs and you
     * need to obtain it directly from the carrier or other sources.
     *
     * @see <a href ="https://issuetracker.google.com/issues/319966463">AOSP feature request</a>
     * @return cell local id id (4 to 14 bits)
     */
    fun clId(gnbBitLength: Int) = nci?.let { nci ->
        if (gnbBitLength !in GNB_LENGTH_RANGE) return@let null
        val l = 36 - gnbBitLength
        nci and ((1 shl l) - 1).toLong()
    }

    override fun <T> let(processor: ICellProcessor<T>): T = processor.processNr(this)

    companion object {

        /**
         * Correct min CID value is 0. Some Samsung phones use it as N/A value.
         */
        const val CID_MIN = 1L
        const val CID_MAX = 68_719_476_735L


        /**
         * Correct min TAC value is 0. Some Samsung phones use it as N/A value.
         */
        const val TAC_MIN = 1L
        /**
         * Correct max TAC value is 16777215. Pixel 6 use it as N/A value.
         */
        const val TAC_MAX = 16_777_214L

        const val PCI_MIN = 0L
        const val PCI_MAX = 1007L

        const val GNB_LENGTH_MIN = 22
        const val GNB_LENGTH_MAX = 32

        internal val CID_RANGE = CID_MIN..CID_MAX
        internal val TAC_RANGE = TAC_MIN..TAC_MAX
        internal val PCI_RANGE = PCI_MIN..PCI_MAX
        internal val GNB_LENGTH_RANGE = GNB_LENGTH_MIN..GNB_LENGTH_MAX
    }

}